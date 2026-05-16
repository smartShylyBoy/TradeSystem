package ai.pp.trading.web.infrastructure.client;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.IndicatorRequest;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import ai.pp.trading.web.domain.port.IndicatorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * IndicatorPort 的基础设施实现。
 * 通过 WebClient 调用下游 indicator-service 的 REST API 计算技术指标。
 */
@Component
public class IndicatorClient implements IndicatorPort {

    private static final Logger log = LoggerFactory.getLogger(IndicatorClient.class);

    private final WebClient webClient;
    private final String indicatorServiceUrl;

    public IndicatorClient(WebClient webClient,
                           @Value("${service.indicator.url}") String indicatorServiceUrl) {
        this.webClient = webClient;
        this.indicatorServiceUrl = indicatorServiceUrl;
    }

    /**
     * 调用 indicator-service 的 POST /api/indicators/calculate 接口。
     * 将K线数据和股票标识传给下游服务，获取MACD、MA、RSI、BOLL计算结果。
     */
    @Override
    public IndicatorResponse calculate(List<KlineResponse> klines, String symbol, String market, String period) {
        log.info("调用indicator-service开始: symbol={}, market={}, period={}, klines={}", symbol, market, period, klines.size());

        IndicatorRequest request = new IndicatorRequest(klines, symbol, market, period);

        try {
            IndicatorResponse result = webClient.post()
                    .uri(indicatorServiceUrl + "/api/indicators/calculate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(IndicatorResponse.class)
                    .block();

            log.info("调用indicator-service成功: symbol={}", symbol);
            return result;
        } catch (WebClientResponseException e) {
            log.error("indicator-service返回错误: symbol={}, status={}, body={}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IndicatorClientException(
                    "indicator-service returned error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("调用indicator-service失败: symbol={}, error={}", symbol, e.getMessage(), e);
            throw new IndicatorClientException(
                    "Failed to connect to indicator-service", e);
        }
    }

    /**
     * 自定义异常：表示调用 indicator-service 失败。
     * 由 GlobalExceptionHandler 捕获并转换为 503 响应。
     */
    public static class IndicatorClientException extends RuntimeException {
        public IndicatorClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
