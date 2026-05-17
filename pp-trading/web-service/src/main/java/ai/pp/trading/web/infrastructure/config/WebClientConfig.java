package ai.pp.trading.web.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * WebClient 全局配置。
 * 提供默认的 WebClient bean，供各 Client 注入使用。
 * 通过 Java 系统属性禁用代理，避免系统 http_proxy 环境变量干扰 localhost 通信。
 */
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${service.market-data.url}")
    private String marketDataServiceUrl;

    @Value("${service.indicator.url}")
    private String indicatorServiceUrl;

    @Value("${service.backtest.url}")
    private String backtestServiceUrl;

    /** 在类加载时设置系统属性，禁用 HTTP 代理 */
    static {
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");
        System.setProperty("http.nonProxyHosts", "*");
        System.setProperty("https.nonProxyHosts", "*");
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    public WebClient marketDataWebClient() {
        return WebClient.builder()
                .baseUrl(marketDataServiceUrl)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    public WebClient indicatorWebClient() {
        return WebClient.builder()
                .baseUrl(indicatorServiceUrl)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    public WebClient backtestWebClient() {
        return WebClient.builder()
                .baseUrl(backtestServiceUrl)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("下游服务请求: {} {} headers={}", request.method(), request.url(), request.headers());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("下游服务响应: status={} headers={}", response.statusCode(), response.headers().asHttpHeaders());
            return Mono.just(response);
        });
    }
}
