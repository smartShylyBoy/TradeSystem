import { useState, useEffect, useCallback } from 'react'
import type { TranslationKey } from '../i18n/zh'
import type {
  StrategyInfo,
  BacktestParams,
  BacktestResponse,
  BacktestTradeDetail,
} from '../types'
import { fetchStrategies, runBacktest } from '../api'

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getDefaultStartDate() {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 10)
  return formatDate(date)
}

function getDefaultEndDate() {
  return formatDate(new Date())
}

/** 组件属性 */
interface Props {
  t: (key: TranslationKey, params?: Record<string, string | number>) => string
}

/**
 * 回测页面。
 * 提供策略选择、股票参数输入、执行回测、展示交易信号列表。
 */
export default function BacktestPage({ t }: Props) {
  /* ========== 状态 ========== */

  /** 策略列表 */
  const [strategies, setStrategies] = useState<StrategyInfo[]>([])
  /** 策略加载状态 */
  const [strategiesLoading, setStrategiesLoading] = useState<boolean>(true)

  /** 表单参数 */
  const [symbol, setSymbol] = useState<string>('AAPL')
  const [market, setMarket] = useState<string>('us')
  const [period, setPeriod] = useState<string>('daily')
  const [startDate, setStartDate] = useState<string>(getDefaultStartDate())
  const [endDate, setEndDate] = useState<string>(getDefaultEndDate())
  const [strategyId, setStrategyId] = useState<string>('')

  /** 回测执行状态 */
  const [loading, setLoading] = useState<boolean>(false)
  /** 错误信息 */
  const [error, setError] = useState<string | null>(null)
  /** 回测结果 */
  const [result, setResult] = useState<BacktestResponse | null>(null)

  /* ========== 副作用 ========== */

  /** 页面加载时获取策略列表 */
  useEffect(() => {
    let cancelled = false
    setStrategiesLoading(true)

    fetchStrategies()
      .then((list) => {
        if (cancelled) return
        setStrategies(list)
        // 默认选中第一个策略
        if (list.length > 0) {
          setStrategyId(list[0].id)
        }
      })
      .catch(() => {
        if (cancelled) return
        setError(t('backtest.error.loadStrategies'))
      })
      .finally(() => {
        if (!cancelled) {
          setStrategiesLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [t])

  /* ========== 事件处理 ========== */

  /** 执行回测 */
  const handleRunBacktest = useCallback(async () => {
    if (!strategyId) {
      setError(t('backtest.error.noStrategy'))
      return
    }
    if (!symbol.trim()) {
      setError(t('backtest.error.noSymbol'))
      return
    }

    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const params: BacktestParams = {
        symbol: symbol.trim().toUpperCase(),
        market,
        period,
        startDate,
        endDate,
        strategyId,
      }
      const response = await runBacktest(params)
      setResult(response)
    } catch (err) {
      const message = err instanceof Error ? err.message : String(err)
      setError(t('backtest.error.runFailed'))
      console.error('回测执行失败:', message)
    } finally {
      setLoading(false)
    }
  }, [symbol, market, period, startDate, endDate, strategyId, t])

  /* ========== 渲染 ========== */

  return (
    <div className="container-fluid px-4 py-3">
      {/* 标题 */}
      <h4 className="text-light mb-3">{t('backtest.title')}</h4>

      {/* 参数表单 */}
      <div className="card bg-dark border-secondary mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            {/* 股票代码 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.symbol')}
              </label>
              <input
                type="text"
                className="form-control form-control-sm bg-dark text-light border-secondary"
                placeholder={t('backtest.symbolPlaceholder')}
                value={symbol}
                onChange={(e) => setSymbol(e.target.value)}
              />
            </div>

            {/* 市场 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.market')}
              </label>
              <select
                className="form-select form-select-sm bg-dark text-light border-secondary"
                value={market}
                onChange={(e) => setMarket(e.target.value)}
              >
                <option value="us">{t('common.us')}</option>
              </select>
            </div>

            {/* 周期 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.period')}
              </label>
              <select
                className="form-select form-select-sm bg-dark text-light border-secondary"
                value={period}
                onChange={(e) => setPeriod(e.target.value)}
              >
                <option value="daily">{t('search.period.daily')}</option>
                <option value="weekly">{t('search.period.weekly')}</option>
                <option value="monthly">{t('search.period.monthly')}</option>
              </select>
            </div>

            {/* 开始日期 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.startDate')}
              </label>
              <input
                type="date"
                className="form-control form-control-sm bg-dark text-light border-secondary"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>

            {/* 结束日期 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.endDate')}
              </label>
              <input
                type="date"
                className="form-control form-control-sm bg-dark text-light border-secondary"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>

            {/* 策略选择 */}
            <div className="col-auto">
              <label className="form-label text-light small">
                {t('backtest.strategy')}
              </label>
              <select
                className="form-select form-select-sm bg-dark text-light border-secondary"
                value={strategyId}
                onChange={(e) => setStrategyId(e.target.value)}
                disabled={strategiesLoading}
              >
                {strategiesLoading ? (
                  <option value="">{t('status.loading')}</option>
                ) : (
                  strategies.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))
                )}
              </select>
            </div>

            {/* 执行按钮 */}
            <div className="col-auto">
              <button
                type="button"
                className="btn btn-sm btn-primary px-4"
                onClick={handleRunBacktest}
                disabled={loading || strategiesLoading}
              >
                {loading ? t('backtest.running') : t('backtest.run')}
              </button>
            </div>
          </div>

          {/* 策略描述 */}
          {strategyId && (
            <div className="mt-2">
              <small className="text-muted">
                {strategies.find((s) => s.id === strategyId)?.description ?? ''}
              </small>
            </div>
          )}
        </div>
      </div>

      {/* 错误提示 */}
      {error && (
        <div className="alert alert-danger py-2" role="alert">
          {error}
        </div>
      )}

      {/* 回测结果 */}
      {result && (
        <div className="card bg-dark border-secondary">
          <div className="card-header d-flex justify-content-between align-items-center">
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
              <div className="table-responsive">
                <table className="table table-dark table-hover table-sm mb-0">
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
                    {result.trades.map((trade, idx) => (
                      <TradeRow key={idx} index={idx + 1} trade={trade} t={t} />
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

/* ========== 子组件 ========== */

/** 交易行组件属性 */
interface TradeRowProps {
  index: number
  trade: BacktestTradeDetail
  t: (key: TranslationKey) => string
}

/**
 * 单行交易记录。
 * 根据方向和盈亏显示不同颜色。
 */
function TradeRow({ index, trade, t }: TradeRowProps) {
  /** 计算盈亏百分比 */
  const pnlPercent = trade.closed
    ? ((trade.closePrice - trade.openPrice) / trade.openPrice * 100).toFixed(2)
    : '-'

  /** 方向标签颜色 */
  const directionBadgeClass = trade.direction === 'LONG'
    ? 'bg-danger'
    : 'bg-success'

  /** 状态标签 */
  const statusLabel = trade.closed
    ? t('backtest.result.closed')
    : t('backtest.result.open')

  return (
    <tr>
      <td>{index}</td>
      <td>
        <span className={`badge ${directionBadgeClass}`}>
          {trade.direction === 'LONG' ? t('backtest.direction.long') : t('backtest.direction.short')}
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
