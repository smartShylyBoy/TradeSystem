import Navbar from './components/Navbar'
import KlinePage from './pages/KlinePage'

/**
 * 应用根组件。
 * 第1期只包含导航栏和 K 线页面，后续可扩展路由。
 */
export default function App() {
  return (
    <>
      <Navbar />
      <KlinePage />
    </>
  )
}
