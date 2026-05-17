package ai.pp.trading.web.application.usecase.backtest;

import ai.pp.trading.common.dto.backtest.BacktestRequest;
import ai.pp.trading.web.domain.port.BacktestPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 回测用例
 * 编排回测请求，转发到 backtest-service
 */
@Service
public class RunBacktestUseCase {

    private final BacktestPort backtestPort;

    public RunBacktestUseCase(BacktestPort backtestPort) {
        this.backtestPort = backtestPort;
    }

    /**
     * 执行回测
     *
     * @param request 回测请求
     * @return 回测结果
     */
    public Map<String, Object> execute(BacktestRequest request) {
        return backtestPort.runBacktest(request);
    }

    /**
     * 获取所有可用策略
     *
     * @return 策略列表
     */
    public List<Map<String, String>> getStrategies() {
        return backtestPort.getStrategies();
    }
}
