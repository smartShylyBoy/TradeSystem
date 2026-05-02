/**
 * 顶部导航栏。
 * 左侧品牌名，右侧菜单项。第1期只有"K线回测"可用，其余灰显占位。
 */
export default function Navbar() {
  return (
    <nav className="navbar navbar-expand navbar-dark px-3">
      <span className="navbar-brand fw-bold mb-0">pp Trading</span>
      <ul className="navbar-nav">
        {/* 当前可用 */}
        <li className="nav-item">
          <span className="nav-link active">K线回测</span>
        </li>
        {/* 后续功能，暂不可点击 */}
        <li className="nav-item">
          <span className="nav-link disabled">策略管理</span>
        </li>
        <li className="nav-item">
          <span className="nav-link disabled">回测报告</span>
        </li>
      </ul>
    </nav>
  )
}
