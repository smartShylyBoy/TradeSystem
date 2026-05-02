package ai.pp.trading.marketdata.domain.port;

import ai.pp.trading.marketdata.domain.model.Kline;

import java.time.LocalDate;
import java.util.List;

/**
 * 市场数据提供者端口
 * 定义从外部市场数据源获取K线数据的接口（六边形架构中的端口）
 */
public interface
 MarketDataProvider {

    /**
     * 获取K线数据
     * @param symbol 股票代码
     * @param market 市场
     * @param period 周期（daily/weekly/monthly）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线列表
     */
    List<Kline> getKlines(String symbol, String market, String period, LocalDate startDate, LocalDate endDate);
}
