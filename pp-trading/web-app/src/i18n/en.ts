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
}

export default en
