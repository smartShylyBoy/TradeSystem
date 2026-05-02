/**
 * K 线数据项，与后端 KlineResponse DTO 一一对应。
 * date 格式为 yyyy-MM-dd。
 */
export interface Kline {
  date: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

/**
 * 查询 K 线的请求参数。
 * 与后端 GET /api/web/kline 的 @RequestParam 对齐。
 */
export interface KlineParams {
  symbol: string    // 股票代码，如 TSLA
  market: string    // 市场，如 us / hk
  period: string    // 周期：daily / weekly / monthly
  startDate: string // 起始日期，yyyy-MM-dd
  endDate: string   // 结束日期，yyyy-MM-dd
}

/**
 * 股票标签页数据，用于 StockTabs 组件。
 * 每次查询成功后生成一个标签，用户可切换或关闭。
 */
export interface StockTab {
  symbol: string
  market: string
  period: string
  startDate: string
  endDate: string
}
