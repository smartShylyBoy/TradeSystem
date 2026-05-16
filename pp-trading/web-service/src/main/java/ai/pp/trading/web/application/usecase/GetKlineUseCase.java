package ai.pp.trading.web.application.usecase;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.KlineWithIndicatorsResponse;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import ai.pp.trading.web.domain.port.IndicatorPort;
import ai.pp.trading.web.domain.port.MarketDataPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 获取 K 线数据及技术指标的用例。
 * 聚合 market-data-service 和 indicator-service 的结果，
 * 将K线数据与技术指标合并返回给前端。
 */
@Service
public class GetKlineUseCase {

    private final MarketDataPort marketDataPort;
    private final IndicatorPort indicatorPort;

    public GetKlineUseCase(MarketDataPort marketDataPort, IndicatorPort indicatorPort) {
        this.marketDataPort = marketDataPort;
        this.indicatorPort = indicatorPort;
    }

    /**
     * 执行 K 线查询并计算技术指标
     * 1. 从 market-data-service 获取 K 线数据
     * 2. 将 K 线数据传给 indicator-service 计算指标
     * 3. 聚合为 KlineWithIndicatorsResponse 返回
     */
    public KlineWithIndicatorsResponse execute(String symbol, String market, String period,
                                               LocalDate startDate, LocalDate endDate) {
        List<KlineResponse> klines = marketDataPort.getKlines(symbol, market, period, startDate, endDate);
        IndicatorResponse indicators = indicatorPort.calculate(klines, symbol, market, period);
        return new KlineWithIndicatorsResponse(klines, indicators, klines.size());
    }
}
