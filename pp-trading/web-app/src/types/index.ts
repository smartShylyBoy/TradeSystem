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

/** MACD 指标结果 */
export interface MacdResult {
  difList: (number | null)[]
  deaList: (number | null)[]
  macdList: (number | null)[]
}

/** MA 均线结果 */
export interface MaResult {
  ma5List: (number | null)[]
  ma10List: (number | null)[]
  ma20List: (number | null)[]
  ma30List: (number | null)[]
  ma60List: (number | null)[]
}

/** RSI 指标结果 */
export interface RsiResult {
  rsi6List: (number | null)[]
  rsi12List: (number | null)[]
  rsi24List: (number | null)[]
}

/** 布林带指标结果 */
export interface BollResult {
  upperList: (number | null)[]
  middleList: (number | null)[]
  lowerList: (number | null)[]
}

/** 技术指标响应，包含四类指标 */
export interface IndicatorResponse {
  macd: MacdResult
  ma: MaResult
  rsi: RsiResult
  boll: BollResult
}

/** K 线 + 技术指标聚合响应 */
export interface KlineWithIndicatorsResponse {
  klines: Kline[]
  indicators: IndicatorResponse
  totalCount: number
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

/**
 * 指标子项可见性配置。
 * 控制具体哪些子指标线显示在图表上。
 * 由左上角配置面板管理，持久化到 localStorage。
 */
export interface IndicatorVisibility {
  ma5: boolean
  ma10: boolean
  ma20: boolean
  ma30: boolean
  ma60: boolean
  boll: boolean
  dif: boolean
  dea: boolean
  macdBar: boolean
  rsi6: boolean
}

/**
 * 图例大类开关。
 * 控制 ECharts legend 中各大类指标的整体显示/隐藏。
 */
export interface ChartLegendState {
  kline: boolean
  ma: boolean
  boll: boolean
  volume: boolean
  macd: boolean
  rsi: boolean
}

// ========== 回测相关类型 ==========

/** 策略信息 */
export interface StrategyInfo {
  id: string
  name: string
  description: string
}

/** 回测请求参数 */
export interface BacktestParams {
  symbol: string
  market: string
  period: string
  startDate: string
  endDate: string
  strategyId: string
}

/** 回测交易明细 */
export interface BacktestTradeDetail {
  openIndex: number
  closeIndex: number
  openDate: string
  closeDate: string | null
  openPrice: number
  closePrice: number
  direction: string
  openReason: string
  closeReason: string | null
  closed: boolean
}

/** 回测响应 */
export interface BacktestResponse {
  symbol: string
  strategyId: string
  strategyName: string
  totalTrades: number
  trades: BacktestTradeDetail[]
}

/** 当前激活的页面 */
export type ActivePage = 'kline' | 'backtest'
