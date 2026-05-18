import { useEffect, useState } from 'react'
import { fetchKlines, fetchStrategies, runBacktest } from '../api'
import SearchBar from '../components/SearchBar'
import StockTabs from '../components/StockTabs'
import KlineChart from '../components/KlineChart'
import type {
  BacktestResponse,
  BacktestTradeDetail,
  ChartLegendState,
  IndicatorResponse,
  IndicatorVisibility,
  Kline,
  KlineParams,
  StockTab,
  StrategyInfo,
} from '../types'
import type { TranslationKey } from '../i18n/zh'

type TranslateFn = (key: TranslationKey, params?: Record<string, string | number>) => string

interface Props {
  t: TranslateFn
}

const CONFIG_KEY = 'pp-indicator-config'

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

const DEFAULT_LEGEND_STATE: ChartLegendState = {
  kline: true,
  ma: true,
  boll: true,
  volume: true,
  macd: true,
  rsi: true,
}

function loadIndicatorConfig(): IndicatorVisibility {
  try {
    const saved = localStorage.getItem(CONFIG_KEY)
    if (saved) {
      return { ...DEFAULT_INDICATOR_VISIBLE, ...JSON.parse(saved) }
    }
  } catch {
    // ignore invalid saved config
  }
  return DEFAULT_INDICATOR_VISIBLE
}

function buildTabKey(tab: StockTab) {
  return `${tab.symbol}_${tab.market}_${tab.period}`
}

export default function KlinePage({ t }: Props) {
  const [tabs, setTabs] = useState<StockTab[]>([])
  const [activeKey, setActiveKey] = useState('')
  const [dataMap, setDataMap] = useState<Record<string, Kline[]>>({})
  const [indicatorMap, setIndicatorMap] = useState<Record<string, IndicatorResponse>>({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [indicatorVisible, setIndicatorVisible] = useState<IndicatorVisibility>(loadIndicatorConfig)
  const [legendState, setLegendState] = useState<ChartLegendState>(DEFAULT_LEGEND_STATE)
  const [strategies, setStrategies] = useState<StrategyInfo[]>([])
  const [strategiesLoading, setStrategiesLoading] = useState(false)
  const [selectedStrategyId, setSelectedStrategyId] = useState('')
  const [backtestResult, setBacktestResult] = useState<BacktestResponse | null>(null)
  const [backtestLoading, setBacktestLoading] = useState(false)
  const [backtestError, setBacktestError] = useState('')
  const [selectedTradeIndex, setSelectedTradeIndex] = useState<number | null>(null)

  useEffect(() => {
    localStorage.setItem(CONFIG_KEY, JSON.stringify(indicatorVisible))
  }, [indicatorVisible])

  useEffect(() => {
    let cancelled = false
    setStrategiesLoading(true)

    fetchStrategies()
      .then((list) => {
        if (cancelled) return
        setStrategies(list)
        setSelectedStrategyId((current) => {
          if (current && list.some((strategy) => strategy.id === current)) {
            return current
          }
          return list[0]?.id ?? ''
        })
      })
      .catch(() => {
        if (cancelled) return
        setStrategies([])
        setSelectedStrategyId('')
      })
      .finally(() => {
        if (!cancelled) {
          setStrategiesLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    setBacktestResult(null)
    setBacktestError('')
    setSelectedTradeIndex(null)
  }, [activeKey, selectedStrategyId])

  const handleSearch = async (params: KlineParams) => {
    const key = `${params.symbol}_${params.market}_${params.period}`
    setLoading(true)
    setError('')

    try {
      const resp = await fetchKlines(params)
      setDataMap((prev) => ({ ...prev, [key]: resp.klines }))
      setIndicatorMap((prev) => ({ ...prev, [key]: resp.indicators }))
      setTabs((prev) => {
        const exists = prev.some((tab) => buildTabKey(tab) === key)
        if (exists) {
          return prev.map((tab) => (buildTabKey(tab) === key ? params : tab))
        }
        return [...prev, params]
      })
      setActiveKey(key)
      setBacktestResult(null)
      setBacktestError('')
      setSelectedTradeIndex(null)
    } catch (err) {
      const message = err instanceof Error ? err.message : t('status.error')
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  const handleTabClose = (key: string) => {
    setTabs((prev) => prev.filter((tab) => buildTabKey(tab) !== key))
    setDataMap((prev) => {
      const next = { ...prev }
      delete next[key]
      return next
    })
    setIndicatorMap((prev) => {
      const next = { ...prev }
      delete next[key]
      return next
    })

    if (key === activeKey) {
      const remaining = tabs.filter((tab) => buildTabKey(tab) !== key)
      setActiveKey(remaining.length > 0 ? buildTabKey(remaining[remaining.length - 1]) : '')
    }
  }

  const handleRunBacktest = async () => {
    if (!currentTab) {
      return
    }
    if (!selectedStrategyId) {
      setBacktestError(t('backtest.error.noStrategy'))
      return
    }

    setBacktestLoading(true)
    setBacktestError('')
    setBacktestResult(null)
    setSelectedTradeIndex(null)

    try {
      const response = await runBacktest({
        symbol: currentTab.symbol.trim().toUpperCase(),
        market: currentTab.market,
        period: currentTab.period,
        startDate: currentTab.startDate,
        endDate: currentTab.endDate,
        strategyId: selectedStrategyId,
      })
      setBacktestResult(response)
      setSelectedTradeIndex(null)
    } catch (err) {
      const message = err instanceof Error ? err.message : t('backtest.error.runFailed')
      setBacktestError(message)
    } finally {
      setBacktestLoading(false)
    }
  }

  const handleClearBacktest = () => {
    setBacktestResult(null)
    setBacktestError('')
    setSelectedTradeIndex(null)
  }

  const currentTab = tabs.find((tab) => buildTabKey(tab) === activeKey)
  const currentData = dataMap[activeKey] ?? []
  const currentIndicators = indicatorMap[activeKey] ?? null
  const selectedStrategy = strategies.find((strategy) => strategy.id === selectedStrategyId) ?? null

  return (
    <div className="container-fluid px-3 py-3">
      <SearchBar onSearch={handleSearch} loading={loading} t={t} />

      {error && (
        <div className="alert alert-danger mt-3 py-2" role="alert">
          {error}
        </div>
      )}

      <StockTabs
        tabs={tabs}
        activeKey={activeKey}
        onSelect={setActiveKey}
        onClose={handleTabClose}
        t={t}
      />

      {currentTab && (
        <section className="backtest-control-card mt-2">
          <div className="backtest-control-top">
            <span className="text-muted small">
              {t('toolbar.totalData', { count: currentData.length })}
            </span>
            <div className="backtest-control-badges">
              {backtestResult && (
                <span className="badge bg-warning text-dark">
                  {t('backtest.result.totalTrades', { count: backtestResult.totalTrades })}
                </span>
              )}
            </div>
          </div>

          <div className="backtest-control-row">
            <span className="backtest-control-label">{t('backtest.strategy')}:</span>
            <select
              className="form-select form-select-sm bg-dark text-light border-secondary backtest-control-select"
              value={selectedStrategyId}
              onChange={(e) => setSelectedStrategyId(e.target.value)}
              disabled={strategiesLoading || strategies.length === 0}
            >
              {strategiesLoading ? (
                <option value="">{t('status.loading')}</option>
              ) : strategies.length === 0 ? (
                <option value="">{t('backtest.error.loadStrategies')}</option>
              ) : (
                strategies.map((strategy) => (
                  <option key={strategy.id} value={strategy.id}>
                    {strategy.name}
                  </option>
                ))
              )}
            </select>
            <button
              type="button"
              className="btn btn-sm btn-warning backtest-run-btn"
              onClick={handleRunBacktest}
              disabled={loading || backtestLoading || strategiesLoading || !selectedStrategyId}
            >
              {backtestLoading ? t('backtest.running') : t('toolbar.runBacktest')}
            </button>
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary backtest-clear-btn"
              onClick={handleClearBacktest}
              disabled={backtestLoading && !backtestResult && !backtestError}
            >
              {t('toolbar.clear')}
            </button>
          </div>

          <div className="backtest-control-description">
            {selectedStrategy?.description ?? ''}
          </div>
        </section>
      )}

      {backtestError && !currentTab && (
        <div className="alert alert-danger mt-3 py-2" role="alert">
          {backtestError}
        </div>
      )}

      {!loading && currentTab && backtestError && (
        <div className="alert alert-danger mt-3 py-2" role="alert">
          {backtestError}
        </div>
      )}

      {!loading && currentTab && backtestResult && (
        <BacktestResultCard
          result={backtestResult}
          t={t}
          selectedTradeIndex={selectedTradeIndex}
          onSelectTrade={setSelectedTradeIndex}
        />
      )}

      {loading && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">{t('status.loading')}</span>
          </div>
        </div>
      )}

      {!loading && currentTab && (
        <div className="chart-area">
          <KlineChart
            symbol={currentTab.symbol}
            klines={currentData}
            indicators={currentIndicators}
            backtestResult={backtestResult}
            selectedTradeIndex={selectedTradeIndex}
            indicatorVisible={indicatorVisible}
            legendState={legendState}
            onLegendChange={setLegendState}
          />
        </div>
      )}

      {!loading && !currentTab && (
        <div className="text-center text-muted py-5">
          {t('status.empty')}
        </div>
      )}
    </div>
  )
}

function BacktestResultCard({
  result,
  t,
  selectedTradeIndex,
  onSelectTrade,
}: {
  result: BacktestResponse
  t: TranslateFn
  selectedTradeIndex: number | null
  onSelectTrade: (index: number | null) => void
}) {
  return (
    <div className="card bg-dark border-secondary mt-3 mb-2 backtest-result-card">
      <div className="card-header d-flex justify-content-between align-items-center gap-3 flex-wrap">
        <span className="text-light">
          {t('backtest.result.title', {
            symbol: result.symbol,
            strategy: result.strategyName,
          })}
        </span>
        <span className="badge bg-info">
          {t('backtest.result.totalTrades', { count: result.totalTrades })}
        </span>
      </div>
      <div className="card-body p-0">
        {result.trades.length === 0 ? (
          <div className="text-center text-muted py-4">
            {t('backtest.result.noTrades')}
          </div>
        ) : (
          <div className="table-responsive backtest-result-table-wrap">
            <table className="table table-dark table-hover table-sm mb-0 backtest-result-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>{t('backtest.result.direction')}</th>
                  <th>{t('backtest.result.openDate')}</th>
                  <th>{t('backtest.result.openPrice')}</th>
                  <th>{t('backtest.result.openReason')}</th>
                  <th>{t('backtest.result.closeDate')}</th>
                  <th>{t('backtest.result.closePrice')}</th>
                  <th>{t('backtest.result.closeReason')}</th>
                  <th>{t('backtest.result.status')}</th>
                </tr>
              </thead>
              <tbody>
                {result.trades.map((trade, idx) => {
                  const isActive = selectedTradeIndex === idx
                  return (
                    <TradeRow
                      key={idx}
                      index={idx + 1}
                      trade={trade}
                      t={t}
                      active={isActive}
                      onClick={() => onSelectTrade(isActive ? null : idx)}
                    />
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

function TradeRow({
  index,
  trade,
  t,
  active,
  onClick,
}: {
  index: number
  trade: BacktestTradeDetail
  t: TranslateFn
  active: boolean
  onClick: () => void
}) {
  const pnlPercent = trade.closed
    ? ((trade.closePrice - trade.openPrice) / trade.openPrice * 100).toFixed(2)
    : '-'

  const directionBadgeClass = trade.direction === 'LONG'
    ? 'bg-danger'
    : 'bg-success'

  const statusLabel = trade.closed
    ? t('backtest.result.closed')
    : t('backtest.result.open')

  return (
    <tr
      role="button"
      tabIndex={0}
      className={active ? 'table-active backtest-trade-row-active' : 'backtest-trade-row'}
      onClick={onClick}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault()
          onClick()
        }
      }}
    >
      <td>{index}</td>
      <td>
        <span className={`badge ${directionBadgeClass}`}>
          {trade.direction === 'LONG'
            ? t('backtest.direction.long')
            : t('backtest.direction.short')}
        </span>
      </td>
      <td>{trade.openDate}</td>
      <td>{trade.openPrice.toFixed(2)}</td>
      <td>
        <small className="backtest-reason-text">{trade.openReason}</small>
      </td>
      <td>{trade.closed ? trade.closeDate : '-'}</td>
      <td>{trade.closed ? trade.closePrice.toFixed(2) : '-'}</td>
      <td>
        <small className="backtest-reason-text">{trade.closed ? trade.closeReason : '-'}</small>
      </td>
      <td>
        {trade.closed ? (
          <span className={Number(pnlPercent) >= 0 ? 'text-danger' : 'text-success'}>
            {pnlPercent}%
          </span>
        ) : (
          <span className="badge bg-warning text-dark">{statusLabel}</span>
        )}
      </td>
    </tr>
  )
}
