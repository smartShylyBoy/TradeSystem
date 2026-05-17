import { useState, useCallback } from 'react'
import Navbar from './components/Navbar'
import KlinePage from './pages/KlinePage'
import BacktestPage from './pages/BacktestPage'
import { useI18n } from './i18n/useI18n'
import type { ActivePage } from './types'

/**
 * 应用根组件。
 * 管理页面切换、i18n 实例，向下传递翻译函数。
 */
export default function App() {
  const { t, lang, toggleLang } = useI18n()

  /** 当前激活的页面 */
  const [activePage, setActivePage] = useState<ActivePage>('kline')

  /** 页面切换回调 */
  const handleNavigate = useCallback((page: ActivePage) => {
    setActivePage(page)
  }, [])

  return (
    <>
      <Navbar
        t={t}
        lang={lang}
        toggleLang={toggleLang}
        activePage={activePage}
        onNavigate={handleNavigate}
      />
      {activePage === 'kline' && <KlinePage t={t} />}
      {activePage === 'backtest' && <BacktestPage t={t} />}
    </>
  )
}
