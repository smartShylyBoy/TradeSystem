package ai.pp.trading.backtest.domain.port;

import ai.pp.trading.common.dto.KlineResponse;

import java.util.List;

/**
 * 市场数据端口
 * 获取K线历史数据
 */
public interface MarketDataPort {

    /**
     * 获取K线数据
     *
     * @param symbol    股票代码
     * @param market    市场
     * @param period    周期
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return K线数据列表
     */
    List<KlineResponse> getKlines(String symbol, String market, String period,
                                  String startDate, String endDate);
}
