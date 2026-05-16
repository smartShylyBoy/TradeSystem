package ai.pp.trading.web.domain.port;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;

import java.util.List;

/**
 * 出站端口：定义计算技术指标的抽象接口。
 * 基础设施层负责实现此接口，具体通过 WebClient 调用 indicator-service。
 */
public interface IndicatorPort {

    /**
     * 计算技术指标
     *
     * @param klines  K线数据列表
     * @param symbol  股票代码
     * @param market  市场
     * @param period  周期
     * @return 技术指标结果（MACD、MA、RSI、BOLL）
     */
    IndicatorResponse calculate(List<KlineResponse> klines, String symbol, String market, String period);
}
