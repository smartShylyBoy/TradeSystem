import axios from 'axios'
import type {
  KlineParams,
  KlineWithIndicatorsResponse,
  BacktestParams,
  BacktestResponse,
  StrategyInfo,
} from '../types'

/** Axios 实例，baseURL 指向 web-service（BFF 层，端口 8181） */
const api = axios.create({
  baseURL: 'http://localhost:8181/api/web',
})

/**
 * 查询 K 线数据及技术指标。
 * 调用 web-service 的 GET /api/web/kline，返回 K 线 + 指标聚合数据。
 */
export async function fetchKlines(params: KlineParams): Promise<KlineWithIndicatorsResponse> {
  const { data } = await api.get<KlineWithIndicatorsResponse>('/kline', { params })
  return data
}

/**
 * 获取所有可用策略列表。
 * 调用 web-service 的 GET /api/web/strategies。
 */
export async function fetchStrategies(): Promise<StrategyInfo[]> {
  const { data } = await api.get<StrategyInfo[]>('/strategies')
  return data
}

/**
 * 执行回测。
 * 调用 web-service 的 POST /api/web/backtest/run，返回交易信号列表。
 */
export async function runBacktest(params: BacktestParams): Promise<BacktestResponse> {
  const { data } = await api.post<BacktestResponse>('/backtest/run', params)
  return data
}
