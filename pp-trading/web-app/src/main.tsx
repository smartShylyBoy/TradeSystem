import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css' // Bootstrap 5 样式
import './styles/global.css'                    // 深色主题 + 自定义样式
import App from './App'

// 挂载 React 应用到 index.html 中的 #root 元素
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
