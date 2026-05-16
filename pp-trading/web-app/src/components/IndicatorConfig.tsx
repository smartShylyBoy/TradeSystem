import type { IndicatorVisibility } from '../types'

/** 组件属性 */
interface Props {
  visible: IndicatorVisibility
  onChange: (next: IndicatorVisibility) => void
  onClose: () => void
}

/** MA 线选项配置 */
const MA_OPTIONS = [
  { key: 'ma5', label: 'MA5', color: '#f5c842' },
  { key: 'ma10', label: 'MA10', color: '#4a90d9' },
  { key: 'ma20', label: 'MA20', color: '#9b59b6' },
  { key: 'ma30', label: 'MA30', color: '#2ecc71' },
  { key: 'ma60', label: 'MA60', color: '#f97316' },
] as const

/** MACD 子指标选项 */
const MACD_OPTIONS = [
  { key: 'dif', label: 'DIF', color: '#4a90d9' },
  { key: 'dea', label: 'DEA', color: '#f5c842' },
  { key: 'macdBar', label: 'MACD柱', color: '#888' },
] as const

/** RSI 子指标选项 */
const RSI_OPTIONS = [
  { key: 'rsi6', label: 'RSI6', color: '#f5c842' },
] as const

/**
 * 指标配置面板。
 * 左上角浮层面板，控制各指标的子项显示配置。
 * 只负责"配置哪些子指标"，不控制大类的显示/隐藏（那是图例的事）。
 */
export default function IndicatorConfig({ visible, onChange, onClose }: Props) {
  /** 切换单个子指标的勾选状态 */
  const toggle = (key: keyof IndicatorVisibility) => {
    onChange({ ...visible, [key]: !visible[key] })
  }

  return (
    <div className="indicator-config-panel">
      <div className="indicator-config-header">
        <span>指标配置</span>
        <button type="button" className="btn-close btn-close-white" onClick={onClose} />
      </div>

      {/* MA 均线配置 */}
      <div className="indicator-config-section">
        <div className="indicator-config-title">MA均线</div>
        {MA_OPTIONS.map(opt => (
          <label key={opt.key} className="indicator-config-item">
            <input
              type="checkbox"
              checked={visible[opt.key]}
              onChange={() => toggle(opt.key)}
            />
            <span style={{ color: opt.color }}>{opt.label}</span>
          </label>
        ))}
      </div>

      {/* 布林带配置 */}
      <div className="indicator-config-section">
        <div className="indicator-config-title">布林带</div>
        <label className="indicator-config-item">
          <input
            type="checkbox"
            checked={visible.boll}
            onChange={() => toggle('boll')}
          />
          <span style={{ color: '#8b949e' }}>BOLL (20, 2)</span>
        </label>
      </div>

      {/* MACD 配置 */}
      <div className="indicator-config-section">
        <div className="indicator-config-title">MACD</div>
        {MACD_OPTIONS.map(opt => (
          <label key={opt.key} className="indicator-config-item">
            <input
              type="checkbox"
              checked={visible[opt.key]}
              onChange={() => toggle(opt.key)}
            />
            <span style={{ color: opt.color }}>{opt.label}</span>
          </label>
        ))}
      </div>

      {/* RSI 配置 */}
      <div className="indicator-config-section">
        <div className="indicator-config-title">RSI</div>
        {RSI_OPTIONS.map(opt => (
          <label key={opt.key} className="indicator-config-item">
            <input
              type="checkbox"
              checked={visible[opt.key]}
              onChange={() => toggle(opt.key)}
            />
            <span style={{ color: opt.color }}>{opt.label}</span>
          </label>
        ))}
      </div>
    </div>
  )
}
