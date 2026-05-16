package ai.pp.trading.web.presentation.controller;

import ai.pp.trading.common.dto.KlineWithIndicatorsResponse;
import ai.pp.trading.web.application.usecase.GetKlineUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * K 线数据控制器。
 * 提供给前端的 REST API，聚合 K 线数据与技术指标后返回。
 */
@Tag(name = "K线数据", description = "前端BFF层K线查询接口")
@RestController
@RequestMapping("/api/web")
public class KlineController {

    private final GetKlineUseCase getKlineUseCase;

    public KlineController(GetKlineUseCase getKlineUseCase) {
        this.getKlineUseCase = getKlineUseCase;
    }

    /**
     * 查询 K 线数据并计算技术指标
     *
     * @param symbol    股票代码（必填）
     * @param market    市场，默认 us
     * @param period    周期，默认 daily
     * @param startDate 起始日期，可选，ISO 格式（yyyy-MM-dd）
     * @param endDate   结束日期，可选，ISO 格式（yyyy-MM-dd）
     * @return K 线数据 + 技术指标
     */
    @Operation(summary = "查询K线数据及技术指标", description = "聚合 market-data-service 和 indicator-service，返回K线数据及MACD、MA、RSI、BOLL技术指标")
    @GetMapping("/kline")
    public KlineWithIndicatorsResponse getKline(
            @Parameter(description = "股票代码，如 AAPL、TSLA") @RequestParam String symbol,
            @Parameter(description = "市场类型") @RequestParam(defaultValue = "us") String market,
            @Parameter(description = "K线周期") @RequestParam(defaultValue = "daily") String period,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return getKlineUseCase.execute(symbol, market, period, startDate, endDate);
    }
}
