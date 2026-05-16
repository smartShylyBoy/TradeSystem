package ai.pp.trading.indicator.domain.port;

import ai.pp.trading.indicator.domain.model.IndicatorValues;

import java.time.LocalDate;
import java.util.List;

/**
 * 技术指标数据仓储端口
 * 定义指标数据的持久化操作接口（六边形架构中的出站端口）
 */
public interface IndicatorRepository {

    /**
     * 按日期范围查询已缓存的指标数据
     * 通过JOIN四张指标表（ma_daily、macd_daily、rsi_daily、boll_daily）返回完整指标
     * @param symbol 股票代码
     * @param market 市场
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指标值列表
     */
    List<IndicatorValues> findByRange(String symbol, String market, LocalDate startDate, LocalDate endDate);

    /**
     * 批量保存指标数据到四张表
     * 使用ON CONFLICT DO NOTHING避免重复插入
     * @param symbol 股票代码
     * @param market 市场
     * @param values 指标值列表
     */
    void saveAll(String symbol, String market, List<IndicatorValues> values);
}
