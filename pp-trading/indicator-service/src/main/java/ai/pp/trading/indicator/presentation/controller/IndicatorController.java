package ai.pp.trading.indicator.presentation.controller;

import ai.pp.trading.common.dto.indicator.IndicatorRequest;
import ai.pp.trading.common.dto.indicator.IndicatorResponse;
import ai.pp.trading.indicator.application.usecase.CalculateIndicatorsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技术指标控制器
 * 提供技术指标计算的REST API接口
 */
@Tag(name = "技术指标", description = "技术指标计算接口")
@RestController
@RequestMapping("/api/indicators")
public class IndicatorController {

    private final CalculateIndicatorsUseCase calculateIndicatorsUseCase;

    public IndicatorController(CalculateIndicatorsUseCase calculateIndicatorsUseCase) {
        this.calculateIndicatorsUseCase = calculateIndicatorsUseCase;
    }

    @Operation(summary = "计算技术指标", description = "接收K线数据，计算MACD、MA、RSI、BOLL四类技术指标")
    @PostMapping("/calculate")
    public IndicatorResponse calculate(@RequestBody IndicatorRequest request) {
        return calculateIndicatorsUseCase.calculate(request);
    }
}
