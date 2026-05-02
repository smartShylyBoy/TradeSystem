package ai.pp.trading.marketdata.presentation.controller;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.marketdata.application.usecase.GetKlineUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 行情数据控制器
 * 提供K线数据的REST API接口
 */
@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final GetKlineUseCase getKlineUseCase;

    public MarketDataController(GetKlineUseCase getKlineUseCase) {
        this.getKlineUseCase = getKlineUseCase;
    }

    /**
     * 获取K线数据
     * @param symbol 股票代码（必填）
     * @param market 市场，默认us
     * @param period 周期，默认daily（daily/weekly/monthly）
     * @param startDate 开始日期，默认一年前
     * @param endDate 结束日期，默认今天
     * @return K线数据列表
     */
    @GetMapping("/kline")
    public List<KlineResponse> getKline(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "us") String market,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 默认结束日期为今天
        LocalDate resolvedEndDate = endDate == null ? LocalDate.now() : endDate;
        // 默认开始日期为一年前
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusYears(1) : startDate;
        return getKlineUseCase.getKlines(symbol, market, period, resolvedStartDate, resolvedEndDate);
    }
}
