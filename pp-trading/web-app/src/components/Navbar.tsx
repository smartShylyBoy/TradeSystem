import type { TranslationKey } from '../i18n/zh'
import type { Lang } from '../i18n/useI18n'

interface Props {
  t: (key: TranslationKey) => string
  lang: Lang
  toggleLang: () => void
}

export default function Navbar({ t, lang, toggleLang }: Props) {
  return (
    <nav className="navbar navbar-expand navbar-dark px-3">
      <span className="navbar-brand fw-bold mb-0">{t('nav.brand')}</span>
      <ul className="navbar-nav me-auto">
        <li className="nav-item">
          <span className="nav-link active">{t('nav.kline')}</span>
        </li>
      </ul>
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
