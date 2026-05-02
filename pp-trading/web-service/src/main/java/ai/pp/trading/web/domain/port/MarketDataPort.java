package ai.pp.trading.web.domain.port;

import ai.pp.trading.common.dto.KlineResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 出站端口：定义获取行情数据的抽象接口。
 * 基础设施层负责实现此接口，具体通过 WebClient 调用 market-data-service。
 */
public interface MarketDataPort {

    /**
     * 查询 K 线数据
     *
     * @param symbol    股票代码
     * @param market    市场（us/hk 等）
     * @param period    周期（daily/weekly/monthly）
     * @param startDate 起始日期，可选
     * @param endDate   结束日期，可选
     * @return K 线数据列表
     */
    List<KlineResponse> getKlines(String symbol, String market, String period,
                                  LocalDate startDate, LocalDate endDate);
}
