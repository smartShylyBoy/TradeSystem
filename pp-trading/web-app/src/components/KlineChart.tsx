import { useEffect, useRef } from 'react'
import * as echarts from 'echarts'
import type {
  BacktestResponse,
  ChartLegendState,
  IndicatorResponse,
  IndicatorVisibility,
  Kline,
} from '../types'

interface Props {
  symbol: string
  klines: Kline[]
  indicators: IndicatorResponse | null
  backtestResult: BacktestResponse | null
  selectedTradeIndex: number | null
  indicatorVisible: IndicatorVisibility
  legendState: ChartLegendState
  onLegendChange: (next: ChartLegendState) => void
}

const MA_COLORS: Record<string, string> = {
  ma5: '#f5c842',
  ma10: '#4a90d9',
  ma20: '#9b59b6',
  ma30: '#2ecc71',
  ma60: '#f97316',
}

const SIGNAL_COLOR = '#f5c842'
const SIGNAL_LABEL_BG = 'rgba(22, 27, 34, 0.94)'
const PROFIT_AREA_COLOR = 'rgba(245, 200, 66, 0.12)'
const LOSS_AREA_COLOR = 'rgba(74, 144, 217, 0.16)'

export default function KlineChart({
  symbol,
  klines,
  indicators,
  backtestResult,
  selectedTradeIndex,
  indicatorVisible,
  legendState,
  onLegendChange,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<echarts.ECharts | null>(null)

  useEffect(() => {
    if (!containerRef.current) return
    const chart = echarts.init(containerRef.current)
    chartRef.current = chart

    const onResize = () => chart.resize()
    window.addEventListener('resize', onResize)

    return () => {
      window.removeEventListener('resize', onResize)
      chart.dispose()
      chartRef.current = null
    }
  }, [])

  useEffect(() => {
    const chart = chartRef.current
    if (!chart) return

    const handler = (...args: unknown[]) => {
      const params = args[0] as { selected?: Record<string, boolean> } | undefined
      const selected = params?.selected ?? {}
      const next: ChartLegendState = {
        kline: selected['K线'] ?? true,
        ma: selected['MA'] ?? true,
        boll: selected['BOLL'] ?? true,
        volume: selected['成交量'] ?? true,
        macd: selected['MACD'] ?? true,
        rsi: selected['RSI'] ?? true,
      }
      onLegendChange(next)
    }

    chart.on('legendselectchanged', handler)
    return () => {
      chart.off('legendselectchanged', handler)
    }
  }, [onLegendChange])

  useEffect(() => {
    const chart = chartRef.current
    if (!chart) return
    if (klines.length === 0) {
      chart.clear()
      return
    }

    const dates = klines.map((kline) => kline.date)
    const candleData = klines.map((kline) => [kline.open, kline.close, kline.low, kline.high])
    const volumeData = klines.map((kline) => ({
      value: kline.volume,
      itemStyle: { color: kline.close >= kline.open ? '#ef4444' : '#22c55e' },
    }))

    const isShown = (category: keyof ChartLegendState, subKey?: keyof IndicatorVisibility) => {
      if (!legendState[category]) return false
      if (subKey !== undefined && !indicatorVisible[subKey]) return false
      return true
    }

    const maDefs = [
      { key: 'ma5' as const, label: 'MA5', data: indicators?.ma.ma5List },
      { key: 'ma10' as const, label: 'MA10', data: indicators?.ma.ma10List },
      { key: 'ma20' as const, label: 'MA20', data: indicators?.ma.ma20List },
      { key: 'ma30' as const, label: 'MA30', data: indicators?.ma.ma30List },
      { key: 'ma60' as const, label: 'MA60', data: indicators?.ma.ma60List },
    ]

    const maSeries: echarts.SeriesOption[] = maDefs
      .filter((def) => isShown('ma', def.key) && def.data)
      .map((def) => ({
        name: def.label,
        type: 'line' as const,
        data: def.data as (number | null)[],
        xAxisIndex: 0,
        yAxisIndex: 0,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 1, color: MA_COLORS[def.key] },
        z: 2,
      }))

    const bollSeries: echarts.SeriesOption[] = []
    if (isShown('boll', 'boll') && indicators) {
      bollSeries.push(
        {
          name: 'BOLL上轨',
          type: 'line',
          data: indicators.boll.upperList as (number | null)[],
          xAxisIndex: 0,
          yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
        {
          name: 'BOLL中轨',
          type: 'line',
          data: indicators.boll.middleList as (number | null)[],
          xAxisIndex: 0,
          yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
        {
          name: 'BOLL下轨',
          type: 'line',
          data: indicators.boll.lowerList as (number | null)[],
          xAxisIndex: 0,
          yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
      )
    }

    const macdSeries: echarts.SeriesOption[] = []
    if (indicators) {
      const { difList, deaList, macdList } = indicators.macd
      if (isShown('macd', 'dif')) {
        macdSeries.push({
          name: 'DIF',
          type: 'line',
          data: difList as (number | null)[],
          xAxisIndex: 2,
          yAxisIndex: 2,
          symbol: 'none',
          lineStyle: { width: 1, color: '#4a90d9' },
        })
      }
      if (isShown('macd', 'dea')) {
        macdSeries.push({
          name: 'DEA',
          type: 'line',
          data: deaList as (number | null)[],
          xAxisIndex: 2,
          yAxisIndex: 2,
          symbol: 'none',
          lineStyle: { width: 1, color: '#f5c842' },
        })
      }
      if (isShown('macd', 'macdBar')) {
        macdSeries.push({
          name: 'MACD柱',
          type: 'bar',
          data: (macdList as (number | null)[]).map((value) => ({
            value,
            itemStyle: { color: value != null && value >= 0 ? '#ef4444' : '#22c55e' },
          })),
          xAxisIndex: 2,
          yAxisIndex: 2,
        })
      }
    }

    const rsiSeries: echarts.SeriesOption[] = []
    if (indicators && isShown('rsi', 'rsi6')) {
      rsiSeries.push({
        name: 'RSI6',
        type: 'line',
        data: indicators.rsi.rsi6List as (number | null)[],
        xAxisIndex: 3,
        yAxisIndex: 3,
        symbol: 'none',
        lineStyle: { width: 1, color: '#f5c842' },
      })
    }

    const rsiRefLines: echarts.SeriesOption[] = legendState.rsi
      ? [
          {
            name: '超买',
            type: 'line',
            data: dates.map(() => 80),
            xAxisIndex: 3,
            yAxisIndex: 3,
            symbol: 'none',
            lineStyle: { width: 1, color: '#484f58', type: 'dashed' },
            silent: true,
          },
          {
            name: '超卖',
            type: 'line',
            data: dates.map(() => 20),
            xAxisIndex: 3,
            yAxisIndex: 3,
            symbol: 'none',
            lineStyle: { width: 1, color: '#484f58', type: 'dashed' },
            silent: true,
          },
        ]
      : []

    const { markPoint, markArea, focusRange } = buildBacktestMarks(klines, backtestResult, selectedTradeIndex)

    const legendData = ['K线', 'MA', 'BOLL', '成交量', 'MACD', 'RSI']
    const legendSelected: Record<string, boolean> = {
      'K线': legendState.kline,
      MA: legendState.ma,
      BOLL: legendState.boll,
      成交量: legendState.volume,
      MACD: legendState.macd,
      RSI: legendState.rsi,
    }

    const candleSeries = {
      name: 'K线',
      type: 'candlestick' as const,
      data: candleData,
      xAxisIndex: 0,
      yAxisIndex: 0,
      itemStyle: {
        color: '#ef4444',
        color0: '#22c55e',
        borderColor: '#ef4444',
        borderColor0: '#22c55e',
      },
      markPoint,
      markArea,
    } as echarts.SeriesOption

    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      animation: false,
      legend: {
        data: legendData,
        selected: legendSelected,
        top: 4,
        left: 80,
        right: 100,
        textStyle: { color: '#8b949e', fontSize: 11 },
        inactiveColor: '#484f58',
        itemWidth: 14,
        itemHeight: 10,
        itemGap: 12,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
        backgroundColor: '#161b22',
        borderColor: '#30363d',
        textStyle: { color: '#e6edf3', fontSize: 12 },
      },
      grid: [
        { left: 60, right: 20, top: 40, bottom: '52%' },
        { left: 60, right: 20, top: '52%', bottom: '36%' },
        { left: 60, right: 20, top: '68%', bottom: '16%' },
        { left: 60, right: 20, top: '88%', bottom: 40 },
      ],
      xAxis: [
        { type: 'category', data: dates, gridIndex: 0, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { show: false }, splitLine: { show: false } },
        { type: 'category', data: dates, gridIndex: 1, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { show: false }, splitLine: { show: false } },
        { type: 'category', data: dates, gridIndex: 2, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { show: false }, splitLine: { show: false } },
        { type: 'category', data: dates, gridIndex: 3, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e' }, splitLine: { show: false } },
      ],
      yAxis: [
        { type: 'value', gridIndex: 0, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e' }, splitLine: { lineStyle: { color: '#30363d' } }, scale: true },
        { type: 'value', gridIndex: 1, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e', fontSize: 10 }, splitLine: { show: false } },
        { type: 'value', gridIndex: 2, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e', fontSize: 10 }, splitLine: { lineStyle: { color: '#30363d' } } },
        { type: 'value', gridIndex: 3, min: 0, max: 100, axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e', fontSize: 10 }, splitLine: { lineStyle: { color: '#30363d' } } },
      ],
      dataZoom: [
        { type: 'inside', xAxisIndex: [0, 1, 2, 3], start: 70, end: 100 },
        {
          type: 'slider',
          xAxisIndex: [0, 1, 2, 3],
          top: '94%',
          height: 20,
          borderColor: '#30363d',
          backgroundColor: '#161b22',
          fillerColor: 'rgba(37, 99, 235, 0.15)',
          handleStyle: { color: '#2563eb' },
          textStyle: { color: '#8b949e' },
          start: 70,
          end: 100,
        },
      ],
      series: [
        candleSeries,
        ...maSeries,
        ...bollSeries,
        {
          name: '成交量',
          type: 'bar',
          data: volumeData,
          xAxisIndex: 1,
          yAxisIndex: 1,
        },
        ...macdSeries,
        ...rsiSeries,
        ...rsiRefLines,
      ],
    }

    chart.setOption(option, true)

    if (focusRange) {
      const startIndex = Math.max(0, focusRange.startIndex - 10)
      const endIndex = Math.min(dates.length - 1, focusRange.endIndex + 10)
      chart.dispatchAction({
        type: 'dataZoom',
        startValue: dates[startIndex],
        endValue: dates[endIndex],
      })
    }
  }, [symbol, klines, indicators, backtestResult, selectedTradeIndex, indicatorVisible, legendState])

  useEffect(() => {
    chartRef.current?.resize()
  }, [klines])

  return <div ref={containerRef} className="kline-chart" />
}

type TradeFocusRange = { startIndex: number; endIndex: number }

type BacktestMarks = {
  markPoint?: any
  markArea?: any
  focusRange: TradeFocusRange | null
}

function buildBacktestMarks(
  klines: Kline[],
  backtestResult: BacktestResponse | null,
  selectedTradeIndex: number | null,
): BacktestMarks {
  const dates = klines.map((kline) => kline.date)
  const markPointData: any[] = []
  const markAreaData: any[] = []
  let focusRange: { startIndex: number; endIndex: number } | null = null

  if (!backtestResult) {
    return { markPoint: undefined, markArea: undefined, focusRange }
  }

  const trades = selectedTradeIndex == null
    ? backtestResult.trades.map((trade, index) => ({ trade, index }))
    : backtestResult.trades
      .map((trade, index) => ({ trade, index }))
      .filter((item) => item.index === selectedTradeIndex)

  trades.forEach(({ trade, index }) => {
    if (trade.direction !== 'LONG') {
      return
    }

    const openDate = dates[trade.openIndex]
    if (!openDate) {
      return
    }

    if (selectedTradeIndex === index) {
      focusRange = {
        startIndex: trade.openIndex,
        endIndex: trade.closed ? trade.closeIndex : trade.openIndex,
      }
    }

    markPointData.push(createSignalPoint('开多', openDate, trade.openPrice, 'up'))

    if (!trade.closed) {
      return
    }

    const closeDate = dates[trade.closeIndex]
    if (!closeDate) {
      return
    }

    const areaColor = trade.closePrice < trade.openPrice ? LOSS_AREA_COLOR : PROFIT_AREA_COLOR
    markPointData.push(createSignalPoint('平多', closeDate, trade.closePrice, 'down'))
    markAreaData.push([
      { xAxis: openDate, itemStyle: { color: areaColor } },
      { xAxis: closeDate },
    ])
  })

  return {
    markPoint: markPointData.length > 0 ? { data: markPointData } : undefined,
    markArea: markAreaData.length > 0
      ? {
          silent: true,
          data: markAreaData,
        }
      : undefined,
    focusRange,
  }
}

function createSignalPoint(
  name: string,
  date: string,
  price: number,
  direction: 'up' | 'down',
) {
  const isDown = direction === 'down'
  return {
    name,
    coord: [date, price],
    value: price.toFixed(2),
    symbol: 'triangle',
    symbolRotate: isDown ? 180 : 0,
    symbolSize: 14,
    symbolOffset: isDown ? [0, -12] : [0, 12],
    itemStyle: {
      color: SIGNAL_COLOR,
      borderColor: SIGNAL_COLOR,
    },
    label: {
      show: true,
      position: isDown ? 'top' : 'bottom',
      color: '#f8fafc',
      backgroundColor: SIGNAL_LABEL_BG,
      borderColor: SIGNAL_COLOR,
      borderWidth: 1,
      borderRadius: 4,
      padding: [2, 6],
      formatter: '{b} {c}',
    },
    emphasis: {
      disabled: true,
    },
  }
}
