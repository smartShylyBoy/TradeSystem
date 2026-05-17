package ai.pp.trading.backtest.infrastructure.client;

import ai.pp.trading.backtest.domain.port.IndicatorPort;
import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * IndicatorPort 实现
 * 调用 indicator-service 计算技术指标
 */
@Component
public class IndicatorClient implements IndicatorPort {

    private final WebClient indicatorWebClient;

    public IndicatorClient(WebClient indicatorWebClient) {
        this.indicatorWebClient = indicatorWebClient;
    }

    @Override
    public IndicatorResponse calculateIndicators(List<KlineResponse> klines) {
        return indicatorWebClient.post()
            .uri("/api/indicator/calculate")
            .bodyValue(klines)
            .retrieve()
            .bodyToMono(IndicatorResponse.class)
            .block();
    }
}
