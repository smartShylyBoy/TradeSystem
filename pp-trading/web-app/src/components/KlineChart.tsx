import { useEffect, useRef } from 'react'
import * as echarts from 'echarts'
import type { Kline, IndicatorResponse, IndicatorVisibility, ChartLegendState } from '../types'

/** 组件属性 */
interface Props {
  symbol: string
  klines: Kline[]
  indicators: IndicatorResponse | null
  indicatorVisible: IndicatorVisibility
  legendState: ChartLegendState
  onLegendChange: (next: ChartLegendState) => void
}

/** MA 线颜色 */
const MA_COLORS: Record<string, string> = {
  ma5: '#f5c842',
  ma10: '#4a90d9',
  ma20: '#9b59b6',
  ma30: '#2ecc71',
  ma60: '#f97316',
}

/**
 * K 线图表组件，使用 ECharts 绘制。
 *
 * 布局分四层（从上到下）：
 * - grid0: K 线主图（50%）— 蜡烛图 + MA 均线 + 布林带
 * - grid1: 成交量副图（12%）
 * - grid2: MACD 副图（18%）
 * - grid3: RSI 副图（12%）
 * - 底部: dataZoom 滑动条，四个图联动
 *
 * 图例(Legend)只控制大类显示/隐藏，子指标配置由外部 IndicatorConfig 面板控制。
 * 一个系列只有同时满足"大类开启"且"子指标开启"时才显示。
 */
export default function KlineChart({
  symbol, klines, indicators, indicatorVisible, legendState, onLegendChange,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<echarts.ECharts | null>(null)

  /** 初始化 ECharts 实例 */
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

  /** 监听图例点击事件，同步到 React 状态 */
  useEffect(() => {
    const chart = chartRef.current
    if (!chart) return

    const handler = (params: Record<string, boolean>) => {
      const next: ChartLegendState = {
        kline: params.selected?.['K线'] ?? true,
        ma: params.selected?.['MA'] ?? true,
        boll: params.selected?.['BOLL'] ?? true,
        volume: params.selected?.['成交量'] ?? true,
        macd: params.selected?.['MACD'] ?? true,
        rsi: params.selected?.['RSI'] ?? true,
      }
      onLegendChange(next)
    }

    chart.on('legendselectchanged', handler)
    return () => { chart.off('legendselectchanged', handler) }
  }, [onLegendChange])

  /** 数据或配置变化时重新渲染 */
  useEffect(() => {
    const chart = chartRef.current
    if (!chart) return
    if (klines.length === 0) {
      chart.clear()
      return
    }

    const dates = klines.map(k => k.date)
    const candleData = klines.map(k => [k.open, k.close, k.low, k.high])
    const volumeData = klines.map(k => ({
      value: k.volume,
      itemStyle: { color: k.close >= k.open ? '#ef4444' : '#22c55e' },
    }))

    // --- 辅助函数：一个系列是否应显示 ---
    // 需要同时满足：大类图例开启 + 子指标配置开启
    const isShown = (category: keyof ChartLegendState, subKey?: keyof IndicatorVisibility) => {
      if (!legendState[category]) return false
      if (subKey !== undefined && !indicatorVisible[subKey]) return false
      return true
    }

    // --- MA 均线系列 ---
    const maDefs = [
      { key: 'ma5' as const, label: 'MA5', data: indicators?.ma.ma5List },
      { key: 'ma10' as const, label: 'MA10', data: indicators?.ma.ma10List },
      { key: 'ma20' as const, label: 'MA20', data: indicators?.ma.ma20List },
      { key: 'ma30' as const, label: 'MA30', data: indicators?.ma.ma30List },
      { key: 'ma60' as const, label: 'MA60', data: indicators?.ma.ma60List },
    ]
    const maSeries: echarts.SeriesOption[] = maDefs
      .filter(d => isShown('ma', d.key) && d.data)
      .map(d => ({
        name: d.label,
        type: 'line' as const,
        data: d.data as (number | null)[],
        xAxisIndex: 0,
        yAxisIndex: 0,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 1, color: MA_COLORS[d.key] },
        z: 2,
      }))

    // --- 布林带系列 ---
    const bollSeries: echarts.SeriesOption[] = []
    if (isShown('boll', 'boll') && indicators) {
      bollSeries.push(
        {
          name: 'BOLL上轨',
          type: 'line',
          data: indicators.boll.upperList as (number | null)[],
          xAxisIndex: 0, yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
        {
          name: 'BOLL中轨',
          type: 'line',
          data: indicators.boll.middleList as (number | null)[],
          xAxisIndex: 0, yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
        {
          name: 'BOLL下轨',
          type: 'line',
          data: indicators.boll.lowerList as (number | null)[],
          xAxisIndex: 0, yAxisIndex: 0,
          symbol: 'none',
          lineStyle: { width: 1, color: '#8b949e', type: 'dashed' },
          z: 1,
        },
      )
    }

    // --- MACD 系列 ---
    const macdSeries: echarts.SeriesOption[] = []
    if (indicators) {
      const { difList, deaList, macdList } = indicators.macd
      if (isShown('macd', 'dif')) {
        macdSeries.push({
          name: 'DIF',
          type: 'line',
          data: difList as (number | null)[],
          xAxisIndex: 2, yAxisIndex: 2,
          symbol: 'none',
          lineStyle: { width: 1, color: '#4a90d9' },
        })
      }
      if (isShown('macd', 'dea')) {
        macdSeries.push({
          name: 'DEA',
          type: 'line',
          data: deaList as (number | null)[],
          xAxisIndex: 2, yAxisIndex: 2,
          symbol: 'none',
          lineStyle: { width: 1, color: '#f5c842' },
        })
      }
      if (isShown('macd', 'macdBar')) {
        macdSeries.push({
          name: 'MACD柱',
          type: 'bar',
          data: (macdList as (number | null)[]).map(v => ({
            value: v,
            itemStyle: { color: v != null && v >= 0 ? '#ef4444' : '#22c55e' },
          })),
          xAxisIndex: 2, yAxisIndex: 2,
        })
      }
    }

    // --- RSI 系列 ---
    const rsiSeries: echarts.SeriesOption[] = []
    if (indicators && isShown('rsi', 'rsi6')) {
      rsiSeries.push({
        name: 'RSI6',
        type: 'line',
        data: indicators.rsi.rsi6List as (number | null)[],
        xAxisIndex: 3, yAxisIndex: 3,
        symbol: 'none',
        lineStyle: { width: 1, color: '#f5c842' },
      })
    }

    // --- RSI 参考线（大类开启时显示） ---
    const rsiRefLines: echarts.SeriesOption[] = legendState.rsi ? [
      {
        name: '超买',
        type: 'line',
        data: dates.map(() => 80),
        xAxisIndex: 3, yAxisIndex: 3,
        symbol: 'none',
        lineStyle: { width: 1, color: '#484f58', type: 'dashed' },
        silent: true,
      },
      {
        name: '超卖',
        type: 'line',
        data: dates.map(() => 20),
        xAxisIndex: 3, yAxisIndex: 3,
        symbol: 'none',
        lineStyle: { width: 1, color: '#484f58', type: 'dashed' },
        silent: true,
      },
    ] : []

    // --- 图例：只显示大类名 ---
    const legendData = ['K线', 'MA', 'BOLL', '成交量', 'MACD', 'RSI']
    // 用 legendState 构建 selected 映射
    const legendSelected: Record<string, boolean> = {
      'K线': legendState.kline,
      'MA': legendState.ma,
      'BOLL': legendState.boll,
      '成交量': legendState.volume,
      'MACD': legendState.macd,
      'RSI': legendState.rsi,
    }

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
          type: 'slider', xAxisIndex: [0, 1, 2, 3],
          top: '94%', height: 20,
          borderColor: '#30363d', backgroundColor: '#161b22',
          fillerColor: 'rgba(37, 99, 235, 0.15)',
          handleStyle: { color: '#2563eb' },
          textStyle: { color: '#8b949e' },
          start: 70, end: 100,
        },
      ],

      series: [
        // K 线（图例名: K线）
        {
          name: 'K线',
          type: 'candlestick',
          data: candleData,
          xAxisIndex: 0, yAxisIndex: 0,
          itemStyle: {
            color: '#ef4444', color0: '#22c55e',
            borderColor: '#ef4444', borderColor0: '#22c55e',
          },
        },
        ...maSeries,
        ...bollSeries,
        // 成交量（图例名: 成交量）
        {
          name: '成交量',
          type: 'bar',
          data: volumeData,
          xAxisIndex: 1, yAxisIndex: 1,
        },
        ...macdSeries,
        ...rsiSeries,
        ...rsiRefLines,
      ],
    }

    chart.setOption(option, true)
  }, [symbol, klines, indicators, indicatorVisible, legendState])

  /** 数据变化后 resize */
  useEffect(() => {
    chartRef.current?.resize()
  }, [klines])

  return <div ref={containerRef} className="kline-chart" />
}
