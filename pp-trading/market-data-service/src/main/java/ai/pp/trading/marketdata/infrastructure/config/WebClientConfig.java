package ai.pp.trading.marketdata.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置类
 * 配置用于调用FMP API的WebClient实例
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建FMP API的WebClient Bean
     * @param baseUrl FMP API基础地址（从配置文件读取）
     * @param builder WebClient构建器
     * @return 配置好的WebClient实例
     */
    @Bean
    public WebClient fmpWebClient(@Value("${fmp.base-url}") String baseUrl, WebClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}
