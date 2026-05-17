package ai.pp.trading.web.infrastructure.client;

import ai.pp.trading.common.dto.backtest.BacktestRequest;
import ai.pp.trading.web.domain.port.BacktestPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * BacktestPort 实现
 * 调用 backtest-service 的回测 API
 */
@Component
public class BacktestClient implements BacktestPort {

    private static final Logger log = LoggerFactory.getLogger(BacktestClient.class);

    private final WebClient backtestWebClient;

    public BacktestClient(@Qualifier("backtestWebClient") WebClient backtestWebClient) {
        this.backtestWebClient = backtestWebClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> runBacktest(BacktestRequest request) {
        log.info("调用回测服务: symbol={}, strategyId={}", request.getSymbol(), request.getStrategyId());

        Map<String, Object> response = backtestWebClient.post()
            .uri("/api/backtest/run")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        log.info("回测完成: trades={}", response != null ? response.get("totalTrades") : "null");
        return response;
    }

    @Override
    public List<Map<String, String>> getStrategies() {
        log.info("获取策略列表");

        List<Map<String, String>> response = backtestWebClient.get()
            .uri("/api/backtest/strategies")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, String>>>() {})
            .block();

        log.info("获取到 {} 个策略", response != null ? response.size() : 0);
        return response;
    }
}
