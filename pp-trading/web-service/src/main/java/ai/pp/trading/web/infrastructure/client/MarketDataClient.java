package ai.pp.trading.web.infrastructure.client;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.web.domain.port.MarketDataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * MarketDataPort 的基础设施实现。
 * 通过 WebClient 调用下游 market-data-service 的 REST API 获取 K 线数据。
 */
@Component
public class MarketDataClient implements MarketDataPort {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final WebClient webClient;
    private final String marketDataServiceUrl;

    public MarketDataClient(WebClient webClient,
                            @Value("${service.market-data.url}") String marketDataServiceUrl) {
        this.webClient = webClient;
        this.marketDataServiceUrl = marketDataServiceUrl;
    }

    /**
     * 调用 market-data-service 的 GET /api/market-data/kline 接口。
     * 将所有查询参数透传给下游服务，startDate/endDate 为可选参数。
     */
    @Override
    public List<KlineResponse> getKlines(String symbol, String market, String period,
                                         LocalDate startDate, LocalDate endDate) {
        log.info("调用market-data-service开始: symbol={}, market={}, period={}, startDate={}, endDate={}", symbol, market, period, startDate, endDate);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(marketDataServiceUrl)
                .path("/api/market-data/kline")
                .queryParam("symbol", symbol)
                .queryParam("market", market)
                .queryParam("period", period);

        if (startDate != null) {
            uriBuilder.queryParam("startDate", startDate);
        }
        if (endDate != null) {
            uriBuilder.queryParam("endDate", endDate);
        }

        try {
            List<KlineResponse> result = webClient.get()
                    .uri(uriBuilder.toUriString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<KlineResponse>>() {})
                    .block();

            int count = result == null ? 0 : result.size();
            log.info("调用market-data-service成功: symbol={}, 返回K线条数={}", symbol, count);
            return result;
        } catch (WebClientResponseException e) {
            log.error("market-data-service返回错误: symbol={}, status={}, body={}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            throw new MarketDataClientException(
                    "market-data-service returned error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("调用market-data-service失败: symbol={}, error={}", symbol, e.getMessage(), e);
            throw new MarketDataClientException(
                    "Failed to connect to market-data-service", e);
        }
    }

    /**
     * 自定义异常：表示调用 market-data-service 失败。
     * 由 GlobalExceptionHandler 捕获并转换为 503 响应。
     */
    public static class MarketDataClientException extends RuntimeException {
        public MarketDataClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
