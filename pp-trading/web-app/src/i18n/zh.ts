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
  'toolbar.strategyConfig': '策略配置',
  'toolbar.runBacktest': '运行回测',
  'toolbar.clear': '清除',
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

  // 回测页面
  'backtest.title': '策略回测',
  'backtest.symbol': '股票代码',
  'backtest.symbolPlaceholder': '输入代码，如 AAPL',
  'backtest.market': '市场',
  'backtest.period': '周期',
  'backtest.startDate': '开始日期',
  'backtest.endDate': '结束日期',
  'backtest.strategy': '策略',
  'backtest.run': '执行回测',
  'backtest.running': '回测中...',

  // 回测错误
  'backtest.error.loadStrategies': '加载策略列表失败',
  'backtest.error.noStrategy': '请选择策略',
  'backtest.error.noSymbol': '请输入股票代码',
  'backtest.error.runFailed': '回测执行失败，请检查参数',

  // 回测结果
  'backtest.result.title': '{symbol} - {strategy} 回测结果',
  'backtest.result.totalTrades': '共 {count} 笔交易',
  'backtest.result.noTrades': '未产生交易信号',
  'backtest.result.direction': '方向',
  'backtest.result.openDate': '开仓日期',
  'backtest.result.openPrice': '开仓价格',
  'backtest.result.openReason': '开仓原因',
  'backtest.result.closeDate': '平仓日期',
  'backtest.result.closePrice': '平仓价格',
  'backtest.result.closeReason': '平仓原因',
  'backtest.result.status': '盈亏',
  'backtest.result.closed': '已平仓',
  'backtest.result.open': '持仓中',

  // 交易方向
  'backtest.direction.long': '做多',
  'backtest.direction.short': '做空',
} as const

export type TranslationKey = keyof typeof zh
export default zh
