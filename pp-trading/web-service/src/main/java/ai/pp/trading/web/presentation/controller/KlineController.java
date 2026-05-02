package ai.pp.trading.web.presentation.controller;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.web.application.usecase.GetKlineUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * K 线数据控制器。
 * 提供给前端的 REST API，参数与 market-data-service 保持一致，
 * 由 GetKlineUseCase 透传调用下游服务。
 */
@RestController
@RequestMapping("/api/web")
public class KlineController {

    private final GetKlineUseCase getKlineUseCase;

    public KlineController(GetKlineUseCase getKlineUseCase) {
        this.getKlineUseCase = getKlineUseCase;
    }

    /**
     * 查询 K 线数据
     *
     * @param symbol    股票代码（必填）
     * @param market    市场，默认 us
     * @param period    周期，默认 daily
     * @param startDate 起始日期，可选，ISO 格式（yyyy-MM-dd）
     * @param endDate   结束日期，可选，ISO 格式（yyyy-MM-dd）
     * @return K 线数据列表
     */
    @GetMapping("/kline")
    public List<KlineResponse> getKline(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "us") String market,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return getKlineUseCase.execute(symbol, market, period, startDate, endDate);
    }
}
