import { useEffect, useRef } from 'react'
import * as echarts from 'echarts'
import type { Kline } from '../types'

interface Props {
  symbol: string     // 股票代码，用于图表标题
  klines: Kline[]    // K 线数据数组
}

/**
 * K 线图表组件，使用 ECharts 绘制。
 *
 * 布局分三层：
 * - 主图（70%）：candlestick K 线
 * - 副图（20%）：成交量柱状图
 * - 底部（10%）：dataZoom 滑动条
 *
 * 两个图的 X 轴通过 dataZoom 联动缩放。
 * 配色遵循中国习惯：阳线红、阴线绿。
 */
export default function KlineChart({ symbol, klines }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<echarts.ECharts | null>(null)

  /** 初始化 ECharts 实例，监听窗口 resize */
  useEffect(() => {
    if (!containerRef.current) return
    const chart = echarts.init(containerRef.current)
    chartRef.current = chart

    // 窗口大小变化时自动调整图表尺寸
    const onResize = () => chart.resize()
    window.addEventListener('resize', onResize)

    return () => {
      window.removeEventListener('resize', onResize)
      chart.dispose()
      chartRef.current = null
    }
  }, [])

  /** 数据变化时重新渲染图表 */
  useEffect(() => {
    const chart = chartRef.current
    if (!chart) return
    if (klines.length === 0) {
      chart.clear()
      return
    }

    // X 轴日期数据
    const dates = klines.map(k => k.date)

    // candlestick 数据格式: [开盘, 收盘, 最低, 最高]
    const candleData = klines.map(k => [k.open, k.close, k.low, k.high])

    // 成交量柱状图，根据涨跌设置颜色
    const volumeData = klines.map(k => ({
      value: k.volume,
      itemStyle: { color: k.close >= k.open ? '#ef4444' : '#22c55e' },
    }))

    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent', // 继承页面深色背景
      animation: false,               // 数据量大时关闭动画提升性能

      // 图表标题：显示股票代码
      title: {
        text: symbol,
        left: 16,
        top: 8,
        textStyle: { color: '#e6edf3', fontSize: 16 },
      },

      // 提示框：鼠标悬停时显示 OHLCV 数据
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
        backgroundColor: '#161b22',
        borderColor: '#30363d',
        textStyle: { color: '#e6edf3' },
      },

      // 图例（后续可扩展 MA、BOLL 等指标）
      legend: {
        data: ['K线'],
        top: 8,
        right: 16,
        textStyle: { color: '#8b949e' },
      },

      // 双 grid 布局：主图 + 副图
      grid: [
        // 主图 K 线区域，占 70% 高度
        { left: 60, right: 20, top: 50, bottom: '32%' },
        // 副图成交量区域，占 20% 高度
        { left: 60, right: 20, top: '72%', bottom: 40 },
      ],

      // 双 X 轴：分别绑定主图和副图，dataZoom 联动
      xAxis: [
        {
          type: 'category',
          data: dates,
          gridIndex: 0,
          axisLine: { lineStyle: { color: '#30363d' } },
          axisLabel: { color: '#8b949e' },
          splitLine: { show: false },
        },
        {
          type: 'category',
          data: dates,
          gridIndex: 1,
          axisLine: { lineStyle: { color: '#30363d' } },
          axisLabel: { show: false }, // 副图不显示日期标签，避免重复
          splitLine: { show: false },
        },
      ],

      // 双 Y 轴：价格轴 + 成交量轴
      yAxis: [
        {
          type: 'value',
          gridIndex: 0,
          axisLine: { lineStyle: { color: '#30363d' } },
          axisLabel: { color: '#8b949e' },
          splitLine: { lineStyle: { color: '#30363d' } },
          scale: true, // Y 轴不从 0 开始，让 K 线显示更饱满
        },
        {
          type: 'value',
          gridIndex: 1,
          axisLine: { lineStyle: { color: '#30363d' } },
          axisLabel: { color: '#8b949e' },
          splitLine: { show: false },
        },
      ],

      // dataZoom：鼠标滚轮缩放 + 底部滑动条，两个图的 X 轴联动
      dataZoom: [
        {
          type: 'inside',       // 鼠标滚轮缩放
          xAxisIndex: [0, 1],   // 同时作用于两个 X 轴
          start: 70,            // 默认显示后 30% 数据
          end: 100,
        },
        {
          type: 'slider',       // 底部滑动条
          xAxisIndex: [0, 1],
          top: '92%',
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
        // 主图：K 线蜡烛图
        {
          name: 'K线',
          type: 'candlestick',
          data: candleData,
          xAxisIndex: 0,
          yAxisIndex: 0,
          itemStyle: {
            color: '#ef4444',       // 阳线填充（红）
            color0: '#22c55e',      // 阴线填充（绿）
            borderColor: '#ef4444', // 阳线边框（红）
            borderColor0: '#22c55e',// 阴线边框（绿）
          },
        },
        // 副图：成交量柱状图
        {
          name: '成交量',
          type: 'bar',
          data: volumeData,
          xAxisIndex: 1,
          yAxisIndex: 1,
        },
      ],
    }

    // true 表示完全替换 option（而非合并）
    chart.setOption(option, true)
  }, [symbol, klines])

  /** 数据变化后触发 resize，确保图表适配容器 */
  useEffect(() => {
    chartRef.current?.resize()
  }, [klines])

  return <div ref={containerRef} className="kline-chart" />
}
