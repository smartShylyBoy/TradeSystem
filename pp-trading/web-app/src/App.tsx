import Navbar from './components/Navbar'
import KlinePage from './pages/KlinePage'
import { useI18n } from './i18n/useI18n'

/**
 * 应用根组件。
 * 管理 i18n 实例，向下传递翻译函数。
 */
export default function App() {
  const { t, lang, toggleLang } = useI18n()

  return (
    <>
      <Navbar t={t} lang={lang} toggleLang={toggleLang} />
      <KlinePage t={t} />
    </>
  )
}
