package ai.pp.trading.backtest.presentation.controller;

import ai.pp.trading.backtest.application.usecase.RunBacktestUseCase;
import ai.pp.trading.backtest.domain.model.BacktestTradeDetail;
import ai.pp.trading.backtest.presentation.dto.SimpleBacktestResponse;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import ai.pp.trading.common.dto.backtest.BacktestRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回测控制器
 */
@RestController
@RequestMapping("/api/backtest")
public class BacktestController {

    private final RunBacktestUseCase runBacktestUseCase;

    public BacktestController(RunBacktestUseCase runBacktestUseCase) {
        this.runBacktestUseCase = runBacktestUseCase;
    }

    /**
     * 执行回测
     */
    @PostMapping("/run")
    public ResponseEntity<SimpleBacktestResponse> runBacktest(@RequestBody BacktestRequest request) {
        List<BacktestTradeDetail> trades = runBacktestUseCase.execute(request);

        // 查找策略名称
        String strategyName = runBacktestUseCase.getAvailableStrategies().stream()
            .filter(s -> s.getId().equals(request.getStrategyId()))
            .findFirst()
            .map(TradingStrategy::getName)
            .orElse(request.getStrategyId());

        SimpleBacktestResponse response = new SimpleBacktestResponse(
            request.getSymbol(),
            request.getStrategyId(),
            strategyName,
            trades.size(),
            trades
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有可用策略
     */
    @GetMapping("/strategies")
    public ResponseEntity<List<Map<String, String>>> getStrategies() {
        List<Map<String, String>> strategies = runBacktestUseCase.getAvailableStrategies().stream()
            .map(s -> {
                Map<String, String> info = new HashMap<>();
                info.put("id", s.getId());
                info.put("name", s.getName());
                info.put("description", s.getDescription());
                return info;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(strategies);
    }
}
