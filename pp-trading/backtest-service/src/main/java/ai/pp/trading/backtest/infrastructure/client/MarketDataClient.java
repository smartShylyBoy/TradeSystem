package ai.pp.trading.backtest.infrastructure.client;

import ai.pp.trading.backtest.domain.port.MarketDataPort;
import ai.pp.trading.common.dto.KlineResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MarketDataPort 实现
 * 调用 market-data-service 获取K线数据
 */
@Component
public class MarketDataClient implements MarketDataPort {

    private final WebClient marketDataWebClient;

    public MarketDataClient(WebClient marketDataWebClient) {
        this.marketDataWebClient = marketDataWebClient;
    }

    @Override
    public List<KlineResponse> getKlines(String symbol, String market, String period,
                                         String startDate, String endDate) {
        KlineResponse[] response = marketDataWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/market-data/kline")
                .queryParam("symbol", symbol)
                .queryParam("market", market)
                .queryParam("period", period)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .build())
            .retrieve()
            .bodyToMono(KlineResponse[].class)
            .block();

        return response != null ? Arrays.asList(response) : Collections.emptyList();
    }
}
