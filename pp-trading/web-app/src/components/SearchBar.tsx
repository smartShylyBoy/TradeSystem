import { useState } from 'react'
import type { KlineParams } from '../types'
import type { TranslationKey } from '../i18n/zh'

/** 组件属性 */
interface Props {
  onSearch: (params: KlineParams) => void
  loading: boolean
  t: (key: TranslationKey) => string
}

/** 计算默认日期范围：开始 = 2年前，结束 = 今天 */
function getDefaultDates() {
  const end = new Date()
  const start = new Date()
  start.setFullYear(start.getFullYear() - 2)
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  return { startDate: fmt(start), endDate: fmt(end) }
}

/**
 * 搜索栏组件。
 * 包含股票代码、市场、周期、日期范围输入，以及查询按钮。
 */
export default function SearchBar({ onSearch, loading, t }: Props) {
  const defaults = getDefaultDates()
  const [symbol, setSymbol] = useState('')
  const [market, setMarket] = useState('us')
  const [period, setPeriod] = useState('daily')
  const [startDate, setStartDate] = useState(defaults.startDate)
  const [endDate, setEndDate] = useState(defaults.endDate)

  /** 表单提交 */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!symbol.trim()) return
    onSearch({ symbol: symbol.trim().toUpperCase(), market, period, startDate, endDate })
  }

  return (
    <form onSubmit={handleSubmit} className="row g-2 align-items-end search-bar">
      <div className="col-auto">
        <label className="form-label">{t('search.symbol')}</label>
        <input
          type="text"
          className="form-control"
          placeholder={t('search.symbolPlaceholder')}
          value={symbol}
          onChange={e => setSymbol(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <label className="form-label">{t('search.market')}</label>
        <select className="form-select" value={market} onChange={e => setMarket(e.target.value)}>
          <option value="us">{t('search.market.us')}</option>
        </select>
      </div>
      <div className="col-auto">
        <label className="form-label">{t('search.period')}</label>
        <select className="form-select" value={period} onChange={e => setPeriod(e.target.value)}>
          <option value="daily">{t('search.period.daily')}</option>
          <option value="weekly">{t('search.period.weekly')}</option>
          <option value="monthly">{t('search.period.monthly')}</option>
        </select>
      </div>
      <div className="col-auto">
        <label className="form-label">{t('search.startDate')}</label>
        <input
          type="date"
          className="form-control"
          value={startDate}
          onChange={e => setStartDate(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <label className="form-label">{t('search.endDate')}</label>
        <input
          type="date"
          className="form-control"
          value={endDate}
          onChange={e => setEndDate(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? t('search.loading') : t('search.submit')}
        </button>
      </div>
    </form>
  )
}
