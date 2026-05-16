package ai.pp.trading.indicator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 技术指标服务启动类
 * 提供MACD、MA、RSI、BOLL四类技术指标的计算与缓存服务（端口8183）
 */
@SpringBootApplication
public class IndicatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndicatorServiceApplication.class, args);
    }
}
