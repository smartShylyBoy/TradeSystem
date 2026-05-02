package ai.pp.trading.marketdata.domain.port;

import ai.pp.trading.marketdata.domain.model.Kline;

import java.time.LocalDate;
import java.util.List;

/**
 * K线数据仓储端口
 * 定义K线数据的持久化操作接口（六边形架构中的端口）
 */
public interface KlineRepository {

    /**
     * 按日期范围查询K线数据
     * @param tableName 表名（kline_daily/kline_weekly/kline_monthly）
     * @param symbol 股票代码
     * @param market 市场
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线列表
     */
    List<Kline> findByRange(String tableName, String symbol, String market, LocalDate startDate, LocalDate endDate);

    /**
     * 批量保存K线数据
     * @param tableName 表名
     * @param symbol 股票代码
     * @param market 市场
     * @param klines K线列表
     */
    void saveAll(String tableName, String symbol, String market, List<Kline> klines);
}
