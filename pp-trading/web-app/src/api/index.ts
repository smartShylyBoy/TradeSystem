import axios from 'axios'
import type { Kline, KlineParams } from '../types'

// Axios 实例，baseURL 指向 web-service（BFF 层，端口 8181）
const api = axios.create({
  baseURL: 'http://localhost:8181/api/web',
})

/**
 * 查询 K 线数据。
 * 调用 web-service 的 GET /api/web/kline，由后者透传到 market-data-service。
 */
export async function fetchKlines(params: KlineParams): Promise<Kline[]> {
  const { data } = await api.get<Kline[]>('/kline', { params })
  return data
}
