package ai.pp.trading.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 行情数据服务启动类
 * 提供K线数据的获取、缓存和查询功能
 */
@SpringBootApplication
public class MarketDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataServiceApplication.class, args);
    }
}
