package ai.pp.trading.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 回测服务启动类
 * ComponentScan 扫描 backtest-service 和 strategy-core 中的组件
 */
@SpringBootApplication
@org.springframework.context.annotation.ComponentScan(basePackages = {"ai.pp.trading.backtest", "ai.pp.strategy"})
public class BacktestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BacktestServiceApplication.class, args);
    }
}
