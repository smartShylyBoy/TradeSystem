import type { StockTab } from '../types'

interface Props {
  tabs: StockTab[]          // 所有已查询的股票标签
  activeKey: string         // 当前选中标签的 key
  onSelect: (key: string) => void  // 点击标签切换
  onClose: (key: string) => void   // 点击 × 关闭标签
}

/**
 * 股票标签栏。
 * 查询过的股票显示为可切换的标签，支持关闭。
 * 标签 key 格式: symbol_market_period，保证唯一性。
 */
export default function StockTabs({ tabs, activeKey, onSelect, onClose }: Props) {
  if (tabs.length === 0) return null

  // 市场代码 → 中文显示名
  const marketLabel: Record<string, string> = { us: '美股', hk: '港股' }

  return (
    <div className="stock-tabs">
      {tabs.map(tab => {
        const key = `${tab.symbol}_${tab.market}_${tab.period}`
        const isActive = key === activeKey
        return (
          <span
            key={key}
            className={`stock-tab ${isActive ? 'active' : ''}`}
            onClick={() => onSelect(key)}
          >
            {/* 显示: "TSLA 美股" */}
            {tab.symbol} {marketLabel[tab.market] || tab.market}
            {/* 关闭按钮，阻止事件冒泡避免触发标签切换 */}
            <span
              className="stock-tab-close"
              onClick={e => {
                e.stopPropagation()
                onClose(key)
              }}
            >
              ×
            </span>
          </span>
        )
      })}
    </div>
  )
}
