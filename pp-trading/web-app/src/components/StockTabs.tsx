import type { StockTab } from '../types'
import type { TranslationKey } from '../i18n/zh'

/** 组件属性 */
interface Props {
  tabs: StockTab[]
  activeKey: string
  onSelect: (key: string) => void
  onClose: (key: string) => void
  t: (key: TranslationKey) => string
}

/**
 * 股票标签栏。
 * 查询过的股票显示为可切换的标签，支持关闭。
 */
export default function StockTabs({ tabs, activeKey, onSelect, onClose, t }: Props) {
  if (tabs.length === 0) return null

  /** 市场代码 → 翻译 key */
  const marketKeyMap: Record<string, TranslationKey> = {
    us: 'common.us',
    hk: 'common.hk',
  }

  return (
    <div className="stock-tabs">
      {tabs.map(tab => {
        const key = `${tab.symbol}_${tab.market}_${tab.period}`
        const isActive = key === activeKey
        const marketLabel = t(marketKeyMap[tab.market] ?? 'common.us')
        return (
          <span
            key={key}
            className={`stock-tab ${isActive ? 'active' : ''}`}
            onClick={() => onSelect(key)}
          >
            {tab.symbol} {marketLabel}
            <span
              className="stock-tab-close"
              onClick={e => {
                e.stopPropagation()
                onClose(key)
              }}
            >
              &times;
            </span>
          </span>
        )
      })}
    </div>
  )
}
