package ai.pp.trading.backtest.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 配置
 */
@Configuration
public class WebClientConfig {

    @Value("${service.market-data.url}")
    private String marketDataServiceUrl;

    @Value("${service.indicator.url}")
    private String indicatorServiceUrl;

    @Bean
    public WebClient marketDataWebClient() {
        return WebClient.builder()
            .baseUrl(marketDataServiceUrl)
            .build();
    }

    @Bean
    public WebClient indicatorWebClient() {
        return WebClient.builder()
            .baseUrl(indicatorServiceUrl)
            .build();
    }
}
