package ai.pp.trading.backtest.domain.port;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;

import java.util.List;

/**
 * 技术指标端口
 * 计算技术指标数据
 */
public interface IndicatorPort {

    /**
     * 根据K线数据计算技术指标
     *
     * @param klines K线数据列表
     * @return 指标计算结果
     */
    IndicatorResponse calculateIndicators(List<KlineResponse> klines, String symbol, String market, String period);
}
