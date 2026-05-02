import { useState } from 'react'
import { fetchKlines } from '../api'
import SearchBar from '../components/SearchBar'
import StockTabs from '../components/StockTabs'
import KlineChart from '../components/KlineChart'
import type { Kline, KlineParams, StockTab } from '../types'

/**
 * K 线主页面。
 * 整合搜索栏、股票标签栏、K 线图表，管理全局状态。
 *
 * 状态说明：
 * - tabs: 已查询的股票标签列表
 * - activeKey: 当前选中标签的 key（symbol_market_period）
 * - dataMap: 每只股票对应的 K 线数据缓存（key → Kline[]）
 * - loading: 是否正在请求数据
 * - error: 错误信息
 */
export default function KlinePage() {
  const [tabs, setTabs] = useState<StockTab[]>([])
  const [activeKey, setActiveKey] = useState('')
  const [dataMap, setDataMap] = useState<Record<string, Kline[]>>({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  /** 生成标签唯一 key */
  const buildKey = (p: StockTab) => `${p.symbol}_${p.market}_${p.period}`

  /** 查询处理：调用 API → 更新数据 → 更新标签 */
  const handleSearch = async (params: KlineParams) => {
    const key = `${params.symbol}_${params.market}_${params.period}`
    setLoading(true)
    setError('')
    try {
      const klines = await fetchKlines(params)
      // 缓存该股票的 K 线数据
      setDataMap(prev => ({ ...prev, [key]: klines }))
      // 添加标签（已存在则不重复添加）
      setTabs(prev => {
        const exists = prev.some(t => buildKey(t) === key)
        if (exists) return prev
        return [...prev, params]
      })
      // 自动切换到新查询的标签
      setActiveKey(key)
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : '查询失败'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  /** 关闭标签：移除标签和对应数据，切换到前一个标签 */
  const handleTabClose = (key: string) => {
    setTabs(prev => prev.filter(t => buildKey(t) !== key))
    setDataMap(prev => {
      const next = { ...prev }
      delete next[key]
      return next
    })
    // 如果关闭的是当前标签，切换到最后一个剩余标签
    if (key === activeKey) {
      const remaining = tabs.filter(t => buildKey(t) !== key)
      setActiveKey(remaining.length > 0 ? buildKey(remaining[remaining.length - 1]) : '')
    }
  }

  // 当前选中的标签和对应数据
  const currentTab = tabs.find(t => buildKey(t) === activeKey)
  const currentData = dataMap[activeKey] ?? []

  return (
    <div className="container-fluid px-3 py-3">
      {/* 搜索栏 */}
      <SearchBar onSearch={handleSearch} loading={loading} />

      {/* 错误提示 */}
      {error && (
        <div className="alert alert-danger mt-3 py-2" role="alert">
          {error}
        </div>
      )}

      {/* 股票标签栏 */}
      <StockTabs
        tabs={tabs}
        activeKey={activeKey}
        onSelect={setActiveKey}
        onClose={handleTabClose}
      />

      {/* 数据统计 */}
      {currentTab && (
        <div className="text-muted small mt-2">
          共 {currentData.length} 条数据
        </div>
      )}

      {/* 加载动画 */}
      {loading && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      )}

      {/* K 线图表 */}
      {!loading && currentTab && (
        <KlineChart symbol={currentTab.symbol} klines={currentData} />
      )}

      {/* 空状态提示 */}
      {!loading && !currentTab && (
        <div className="text-center text-muted py-5">
          请输入股票代码开始查询
        </div>
      )}
    </div>
  )
}
