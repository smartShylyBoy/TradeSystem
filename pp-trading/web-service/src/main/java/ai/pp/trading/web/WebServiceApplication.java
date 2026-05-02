package ai.pp.trading.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * web-service 启动入口。
 * 作为 BFF 层，前端只与本服务交互，由本服务转发请求到下游微服务。
 */
@SpringBootApplication
public class WebServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebServiceApplication.class, args);
    }
}
