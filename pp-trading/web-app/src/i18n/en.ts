import type { TranslationKey } from './zh'

/** English language pack */
const en: Record<TranslationKey, string> = {
  // Navbar
  'nav.brand': 'pp Trading',
  'nav.kline': 'K-Line Backtest',
  'nav.strategy': 'Strategy',
  'nav.report': 'Reports',

  // Search bar
  'search.symbol': 'Symbol',
  'search.symbolPlaceholder': 'Enter symbol',
  'search.market': 'Market',
  'search.market.us': 'US',
  'search.period': 'Period',
  'search.period.daily': 'Daily',
  'search.period.weekly': 'Weekly',
  'search.period.monthly': 'Monthly',
  'search.startDate': 'Start Date',
  'search.endDate': 'End Date',
  'search.submit': 'Search',
  'search.loading': 'Searching...',

  // Chart toolbar
  'toolbar.totalData': '{count} records',
  'toolbar.config': 'Indicator Config',
  'toolbar.strategyConfig': 'Strategy Config',
  'toolbar.runBacktest': 'Run Backtest',
  'toolbar.clear': 'Clear',
  'toolbar.backtest': 'Backtest',

  // Chart legend
  'legend.kline': 'K-Line',
  'legend.ma': 'MA',
  'legend.boll': 'BOLL',
  'legend.volume': 'Volume',
  'legend.macd': 'MACD',
  'legend.rsi': 'RSI',

  // Indicator config panel
  'config.title': 'Indicator Config',
  'config.ma': 'MA',
  'config.boll': 'BOLL',
  'config.macd': 'MACD',
  'config.rsi': 'RSI',
  'config.close': 'Close',

  // Status
  'status.empty': 'Enter a symbol to start',
  'status.loading': 'Loading...',
  'status.error': 'Search failed',

  // Common
  'common.us': 'US',
  'common.hk': 'HK',

  // Backtest page
  'backtest.title': 'Strategy Backtest',
  'backtest.symbol': 'Symbol',
  'backtest.symbolPlaceholder': 'Enter symbol, e.g. AAPL',
  'backtest.market': 'Market',
  'backtest.period': 'Period',
  'backtest.startDate': 'Start Date',
  'backtest.endDate': 'End Date',
  'backtest.strategy': 'Strategy',
  'backtest.run': 'Run Backtest',
  'backtest.running': 'Running...',

  // Backtest errors
  'backtest.error.loadStrategies': 'Failed to load strategies',
  'backtest.error.noStrategy': 'Please select a strategy',
  'backtest.error.noSymbol': 'Please enter a symbol',
  'backtest.error.runFailed': 'Backtest failed, please check parameters',

  // Backtest results
  'backtest.result.title': '{symbol} - {strategy} Backtest Results',
  'backtest.result.totalTrades': '{count} trades total',
  'backtest.result.noTrades': 'No trade signals generated',
  'backtest.result.direction': 'Direction',
  'backtest.result.openDate': 'Open Date',
  'backtest.result.openPrice': 'Open Price',
  'backtest.result.openReason': 'Open Reason',
  'backtest.result.closeDate': 'Close Date',
  'backtest.result.closePrice': 'Close Price',
  'backtest.result.closeReason': 'Close Reason',
  'backtest.result.status': 'P&L',
  'backtest.result.closed': 'Closed',
  'backtest.result.open': 'Open',

  // Trade direction
  'backtest.direction.long': 'Long',
  'backtest.direction.short': 'Short',
}

export default en
