package ai.pp.trading.marketdata.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);
    private static final int MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024;

    @Bean
    public WebClient fmpWebClient(@Value("${fmp.base-url}") String baseUrl, WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("FMP Request: {} {} headers={}", request.method(), request.url(), request.headers());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("FMP Response: status={} headers={}", response.statusCode(), response.headers().asHttpHeaders());
            return Mono.just(response);
        });
    }
}
