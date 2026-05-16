import { useState, useEffect } from 'react'
import { fetchKlines } from '../api'
import SearchBar from '../components/SearchBar'
import StockTabs from '../components/StockTabs'
import KlineChart from '../components/KlineChart'
import IndicatorConfig from '../components/IndicatorConfig'
import type {
  Kline, KlineParams, IndicatorResponse, StockTab,
  IndicatorVisibility, ChartLegendState,
} from '../types'
import type { TranslationKey } from '../i18n/zh'

/** 组件属性 */
interface Props {
  t: (key: TranslationKey, params?: Record<string, string | number>) => string
}

/** localStorage key：指标子项配置 */
const CONFIG_KEY = 'pp-indicator-config'

/** 默认指标子项配置：全部开启 */
const DEFAULT_INDICATOR_VISIBLE: IndicatorVisibility = {
  ma5: true,
  ma10: true,
  ma20: true,
  ma30: false,
  ma60: false,
  boll: true,
  dif: true,
  dea: true,
  macdBar: true,
  rsi6: true,
}

/** 默认图例大类开关：全部开启 */
const DEFAULT_LEGEND_STATE: ChartLegendState = {
  kline: true,
  ma: true,
  boll: true,
  volume: true,
  macd: true,
  rsi: true,
}

/** 从 localStorage 加载指标配置 */
function loadIndicatorConfig(): IndicatorVisibility {
  try {
    const saved = localStorage.getItem(CONFIG_KEY)
    if (saved) {
      return { ...DEFAULT_INDICATOR_VISIBLE, ...JSON.parse(saved) }
    }
  } catch {
    // 忽略解析错误
  }
  return DEFAULT_INDICATOR_VISIBLE
}

/**
 * K 线主页面。
 * 整合搜索栏、标签栏、工具栏、指标配置面板、K 线图表。
 */
export default function KlinePage({ t }: Props) {
  const [tabs, setTabs] = useState<StockTab[]>([])
  const [activeKey, setActiveKey] = useState('')
  const [dataMap, setDataMap] = useState<Record<string, Kline[]>>({})
  const [indicatorMap, setIndicatorMap] = useState<Record<string, IndicatorResponse>>({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // 指标子项配置（持久化到 localStorage）
  const [indicatorVisible, setIndicatorVisible] = useState<IndicatorVisibility>(loadIndicatorConfig)

  // 图例大类开关（会话级，不持久化）
  const [legendState, setLegendState] = useState<ChartLegendState>(DEFAULT_LEGEND_STATE)

  // 配置面板是否展开
  const [showConfig, setShowConfig] = useState(false)

  /** 指标配置变化时持久化到 localStorage */
  useEffect(() => {
    localStorage.setItem(CONFIG_KEY, JSON.stringify(indicatorVisible))
  }, [indicatorVisible])

  const buildKey = (p: StockTab) => `${p.symbol}_${p.market}_${p.period}`

  /** 查询处理 */
  const handleSearch = async (params: KlineParams) => {
    const key = `${params.symbol}_${params.market}_${params.period}`
    setLoading(true)
    setError('')
    try {
      const resp = await fetchKlines(params)
      setDataMap(prev => ({ ...prev, [key]: resp.klines }))
      setIndicatorMap(prev => ({ ...prev, [key]: resp.indicators }))
      setTabs(prev => {
        const exists = prev.some(t => buildKey(t) === key)
        if (exists) return prev
        return [...prev, params]
      })
      setActiveKey(key)
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : t('status.error')
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  /** 关闭标签 */
  const handleTabClose = (key: string) => {
    setTabs(prev => prev.filter(t => buildKey(t) !== key))
    setDataMap(prev => { const n = { ...prev }; delete n[key]; return n })
    setIndicatorMap(prev => { const n = { ...prev }; delete n[key]; return n })
    if (key === activeKey) {
      const remaining = tabs.filter(t => buildKey(t) !== key)
      setActiveKey(remaining.length > 0 ? buildKey(remaining[remaining.length - 1]) : '')
    }
  }

  const currentTab = tabs.find(t => buildKey(t) === activeKey)
  const currentData = dataMap[activeKey] ?? []
  const currentIndicators = indicatorMap[activeKey] ?? null

  return (
    <div className="container-fluid px-3 py-3">
      <SearchBar onSearch={handleSearch} loading={loading} t={t} />

      {error && (
        <div className="alert alert-danger mt-3 py-2" role="alert">{error}</div>
      )}

      <StockTabs
        tabs={tabs}
        activeKey={activeKey}
        onSelect={setActiveKey}
        onClose={handleTabClose}
        t={t}
      />

      {/* 工具栏 */}
      {currentTab && (
        <div className="d-flex align-items-center gap-2 mt-2">
          <span className="text-muted small">
            {t('toolbar.totalData', { count: currentData.length })}
          </span>
          <button
            type="button"
            className="btn btn-sm btn-outline-secondary"
            onClick={() => setShowConfig(!showConfig)}
          >
            {t('toolbar.config')}
          </button>
          <button type="button" className="btn btn-sm btn-outline-warning" disabled>
            {t('toolbar.backtest')}
          </button>
        </div>
      )}

      {/* 加载动画 */}
      {loading && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">{t('status.loading')}</span>
          </div>
        </div>
      )}

      {/* 图表区域：配置面板 + K 线图 */}
      {!loading && currentTab && (
        <div className="chart-area">
          {showConfig && (
            <IndicatorConfig
              visible={indicatorVisible}
              onChange={setIndicatorVisible}
              onClose={() => setShowConfig(false)}
            />
          )}
          <KlineChart
            symbol={currentTab.symbol}
            klines={currentData}
            indicators={currentIndicators}
            indicatorVisible={indicatorVisible}
            legendState={legendState}
            onLegendChange={setLegendState}
          />
        </div>
      )}

      {/* 空状态 */}
      {!loading && !currentTab && (
        <div className="text-center text-muted py-5">{t('status.empty')}</div>
      )}
    </div>
  )
}
