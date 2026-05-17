package ai.pp.trading.web.presentation.controller;

import ai.pp.trading.common.dto.backtest.BacktestRequest;
import ai.pp.trading.web.application.usecase.backtest.RunBacktestUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 回测控制器
 * 转发前端回测请求到 backtest-service
 */
@Tag(name = "回测服务", description = "策略回测相关接口")
@RestController
@RequestMapping("/api/web")
public class BacktestController {

    private final RunBacktestUseCase runBacktestUseCase;

    public BacktestController(RunBacktestUseCase runBacktestUseCase) {
        this.runBacktestUseCase = runBacktestUseCase;
    }

    /**
     * 执行回测
     */
    @Operation(summary = "执行策略回测", description = "指定股票和策略，返回交易信号列表")
    @PostMapping("/backtest/run")
    public Map<String, Object> runBacktest(@RequestBody BacktestRequest request) {
        return runBacktestUseCase.execute(request);
    }

    /**
     * 获取所有可用策略
     */
    @Operation(summary = "获取策略列表", description = "返回所有可用的交易策略")
    @GetMapping("/strategies")
    public List<Map<String, String>> getStrategies() {
        return runBacktestUseCase.getStrategies();
    }
}
