import { useState } from 'react'
import type { KlineParams } from '../types'

interface Props {
  onSearch: (params: KlineParams) => void // 查询回调
  loading: boolean                        // 是否正在查询
}

/** 计算默认日期范围：开始 = 5年前，结束 = 今天 */
function getDefaultDates() {
  const end = new Date()
  const start = new Date()
  start.setFullYear(start.getFullYear() - 5)
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  return { startDate: fmt(start), endDate: fmt(end) }
}

/**
 * 搜索栏组件。
 * 包含股票代码、市场、周期、日期范围输入，以及查询按钮。
 * 布局使用 Bootstrap grid，一行排列，响应式自动换行。
 */
export default function SearchBar({ onSearch, loading }: Props) {
  const defaults = getDefaultDates()
  const [symbol, setSymbol] = useState('')
  const [market, setMarket] = useState('us')
  const [period, setPeriod] = useState('daily')
  const [startDate, setStartDate] = useState(defaults.startDate)
  const [endDate, setEndDate] = useState(defaults.endDate)

  /** 表单提交：校验非空后触发 onSearch 回调 */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!symbol.trim()) return
    // 代码自动转大写
    onSearch({ symbol: symbol.trim().toUpperCase(), market, period, startDate, endDate })
  }

  return (
    <form onSubmit={handleSubmit} className="row g-2 align-items-end search-bar">
      <div className="col-auto">
        <label className="form-label">股票代码</label>
        <input
          type="text"
          className="form-control"
          placeholder="输入代码"
          value={symbol}
          onChange={e => setSymbol(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <label className="form-label">市场</label>
        <select className="form-select" value={market} onChange={e => setMarket(e.target.value)}>
          <option value="us">美股</option>
        </select>
      </div>
      <div className="col-auto">
        <label className="form-label">周期</label>
        <select className="form-select" value={period} onChange={e => setPeriod(e.target.value)}>
          <option value="daily">日K</option>
          <option value="weekly">周K</option>
          <option value="monthly">月K</option>
        </select>
      </div>
      <div className="col-auto">
        <label className="form-label">开始日期</label>
        <input
          type="date"
          className="form-control"
          value={startDate}
          onChange={e => setStartDate(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <label className="form-label">结束日期</label>
        <input
          type="date"
          className="form-control"
          value={endDate}
          onChange={e => setEndDate(e.target.value)}
        />
      </div>
      <div className="col-auto">
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? '查询中...' : '查询'}
        </button>
      </div>
    </form>
  )
}
