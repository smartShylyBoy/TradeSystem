import axios from 'axios'
import type { KlineParams, KlineWithIndicatorsResponse } from '../types'

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
