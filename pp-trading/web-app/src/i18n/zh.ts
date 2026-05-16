/** 中文语言包 */
const zh = {
  // 导航栏
  'nav.brand': 'pp Trading',
  'nav.kline': 'K线回测',
  'nav.strategy': '策略管理',
  'nav.report': '回测报告',

  // 搜索栏
  'search.symbol': '股票代码',
  'search.symbolPlaceholder': '输入代码',
  'search.market': '市场',
  'search.market.us': '美股',
  'search.period': '周期',
  'search.period.daily': '日K',
  'search.period.weekly': '周K',
  'search.period.monthly': '月K',
  'search.startDate': '开始日期',
  'search.endDate': '结束日期',
  'search.submit': '查询',
  'search.loading': '查询中...',

  // 图表工具栏
  'toolbar.totalData': '共 {count} 条数据',
  'toolbar.config': '指标配置',
  'toolbar.backtest': '回测',

  // 图表图例
  'legend.kline': 'K线',
  'legend.ma': 'MA',
  'legend.boll': 'BOLL',
  'legend.volume': '成交量',
  'legend.macd': 'MACD',
  'legend.rsi': 'RSI',

  // 指标配置面板
  'config.title': '指标配置',
  'config.ma': 'MA均线',
  'config.boll': '布林带',
  'config.macd': 'MACD',
  'config.rsi': 'RSI',
  'config.close': '关闭',

  // 状态
  'status.empty': '请输入股票代码开始查询',
  'status.loading': 'Loading...',
  'status.error': '查询失败',

  // 通用
  'common.us': '美股',
  'common.hk': '港股',
} as const

export type TranslationKey = keyof typeof zh
export default zh
