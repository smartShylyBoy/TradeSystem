import type { TranslationKey } from '../i18n/zh'
import type { Lang } from '../i18n/useI18n'
import type { ActivePage } from '../types'

/** 组件属性 */
interface Props {
  t: (key: TranslationKey) => string
  lang: Lang
  toggleLang: () => void
  activePage: ActivePage
  onNavigate: (page: ActivePage) => void
}

/**
 * 顶部导航栏。
 * 左侧品牌名，中间菜单项，右侧语言切换按钮。
 * 支持 K线回测 和 策略回测 两个页面的切换。
 */
export default function Navbar({ t, lang, toggleLang, activePage, onNavigate }: Props) {
  return (
    <nav className="navbar navbar-expand navbar-dark px-3">
      <span className="navbar-brand fw-bold mb-0">{t('nav.brand')}</span>
      <ul className="navbar-nav me-auto">
        <li className="nav-item">
          <span
            className={`nav-link ${activePage === 'kline' ? 'active' : ''}`}
            style={{ cursor: 'pointer' }}
            onClick={() => onNavigate('kline')}
          >
            {t('nav.kline')}
          </span>
        </li>
        <li className="nav-item">
          <span
            className={`nav-link ${activePage === 'backtest' ? 'active' : ''}`}
            style={{ cursor: 'pointer' }}
            onClick={() => onNavigate('backtest')}
          >
            {t('nav.backtest')}
          </span>
        </li>
      </ul>
      {/* 中英文切换按钮 */}
      <button
        type="button"
        className="btn btn-sm btn-outline-light lang-toggle"
        onClick={toggleLang}
      >
        {lang === 'zh' ? 'EN' : '中'}
      </button>
    </nav>
  )
}
