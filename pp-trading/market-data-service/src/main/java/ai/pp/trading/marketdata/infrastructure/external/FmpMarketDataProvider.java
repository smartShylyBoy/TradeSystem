package ai.pp.trading.marketdata.infrastructure.external;

import ai.pp.trading.marketdata.application.exception.RateLimitExceededException;
import ai.pp.trading.marketdata.domain.model.Kline;
import ai.pp.trading.marketdata.domain.port.MarketDataProvider;
import ai.pp.trading.marketdata.infrastructure.external.dto.FmpHistoricalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FMP市场数据提供者实现
 * 通过FMP API获取美股历史K线数据，支持日/周/月周期聚合
 */
@Component
public class FmpMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(FmpMarketDataProvider.class);

    /** FMP API WebClient */
    private final WebClient fmpWebClient;
    /** FMP API密钥 */
    private final String apiKey;

    public FmpMarketDataProvider(WebClient fmpWebClient, @Value("${fmp.api-key}") String apiKey) {
        this.fmpWebClient = fmpWebClient;
        this.apiKey = apiKey;
    }

    @Override
    public List<Kline> getKlines(String symbol, String market, String period, LocalDate startDate, LocalDate endDate) {
        // 目前仅支持美股市场
        if (!"us".equalsIgnoreCase(market)) {
            log.warn("不支持的市场类型: symbol={}, market={}", symbol, market);
            return new ArrayList<>();
        }

        log.info("FMP请求开始: symbol={}, market={}, period={}, startDate={}, endDate={}", symbol, market, period, startDate, endDate);

        try {
            // 调用FMP历史价格API (stable/historical-price-eod/full)
            // 该端点返回扁平数组 [{symbol, date, open, high, low, close, volume, ...}, ...]
            List<FmpHistoricalResponse.FmpDailyPrice> priceList = fmpWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/historical-price-eod/full")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .queryParam("from", startDate)
                            .queryParam("to", endDate)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, this::mapClientError)
                    .bodyToMono(new ParameterizedTypeReference<List<FmpHistoricalResponse.FmpDailyPrice>>() {})
                    .block();

            if (priceList == null || priceList.isEmpty()) {
                log.warn("FMP返回空数据: symbol={}, period={}, dateRange=[{}, {}]", symbol, period, startDate, endDate);
                return new ArrayList<>();
            }

            log.info("FMP响应成功: symbol={}, 原始数据条数={}", symbol, priceList.size());

            // 将API响应转换为领域模型，按日期排序
            List<Kline> dailyKlines = priceList.stream()
                    .filter(item -> item.getDate() != null)
                    .map(item -> new Kline(
                            LocalDate.parse(item.getDate()),
                            valueOrZero(item.getOpen()),
                            valueOrZero(item.getHigh()),
                            valueOrZero(item.getLow()),
                            valueOrZero(item.getClose()),
                            item.getVolume() == null ? 0L : item.getVolume()
                    ))
                    .sorted(Comparator.comparing(Kline::date))
                    .collect(Collectors.toList());

            // 根据周期进行聚合
            List<Kline> result;
            switch (period.toLowerCase(Locale.ROOT)) {
                case "daily":
                    result = dailyKlines;
                    break;
                case "weekly":
                    result = aggregateWeekly(dailyKlines);
                    log.info("周K聚合完成: symbol={}, 日K条数={}, 周K条数={}", symbol, dailyKlines.size(), result.size());
                    break;
                case "monthly":
                    result = aggregateMonthly(dailyKlines);
                    log.info("月K聚合完成: symbol={}, 日K条数={}, 月K条数={}", symbol, dailyKlines.size(), result.size());
                    break;
                default:
                    log.warn("不支持的K线周期: period={}", period);
                    return new ArrayList<>();
            }

            log.info("FMP请求完成: symbol={}, period={}, 返回K线条数={}", symbol, period, result.size());
            return result;
        } catch (RateLimitExceededException ex) {
            log.error("FMP API速率限制: symbol={}, error={}", symbol, ex.getMessage());
            throw ex;
        } catch (WebClientResponseException ex) {
            log.error("FMP API响应异常: symbol={}, status={}, body={}", symbol, ex.getStatusCode(), ex.getResponseBodyAsString());
            // 处理429速率限制错误
            if (ex.getStatusCode().value() == 429 || containsRateLimitMessage(ex.getResponseBodyAsString())) {
                throw new RateLimitExceededException("FMP API速率限制超出，请稍后重试。");
            }
            // 其他4xx错误返回空列表
            if (ex.getStatusCode().is4xxClientError()) {
                return new ArrayList<>();
            }
            throw ex;
        } catch (Exception ex) {
            log.error("FMP API调用失败: symbol={}, error={}", symbol, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * 映射4xx客户端错误
     */
    private Mono<? extends Throwable> mapClientError(ClientResponse clientResponse) {
        if (clientResponse.statusCode().value() == 429) {
            return clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("FMP API速率限制超出")
                    .map(RateLimitExceededException::new);
        }
        return clientResponse.createException();
    }

    /**
     * 将日K线聚合为周K线
     * 按周一为周起始日进行分组
     */
    private List<Kline> aggregateWeekly(List<Kline> dailyKlines) {
        Map<LocalDate, List<Kline>> grouped = new LinkedHashMap<>();
        for (Kline kline : dailyKlines) {
            LocalDate weekStart = kline.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            grouped.computeIfAbsent(weekStart, ignored -> new ArrayList<>()).add(kline);
        }
        return grouped.values().stream().map(this::mergeGroup).collect(Collectors.toList());
    }

    /**
     * 将日K线聚合为月K线
     * 按月份第一天进行分组
     */
    private List<Kline> aggregateMonthly(List<Kline> dailyKlines) {
        Map<LocalDate, List<Kline>> grouped = new LinkedHashMap<>();
        for (Kline kline : dailyKlines) {
            LocalDate monthStart = kline.date().withDayOfMonth(1);
            grouped.computeIfAbsent(monthStart, ignored -> new ArrayList<>()).add(kline);
        }
        return grouped.values().stream().map(this::mergeGroup).collect(Collectors.toList());
    }

    /**
     * 合并一组K线为单根K线
     * 开盘价取第一根，收盘价取最后一根，最高/最低价取极值，成交量求和
     */
    private Kline mergeGroup(List<Kline> group) {
        Kline first = group.get(0);
        Kline last = group.get(group.size() - 1);
        double high = group.stream().mapToDouble(Kline::high).max().orElse(first.high());
        double low = group.stream().mapToDouble(Kline::low).min().orElse(first.low());
        long volume = group.stream().mapToLong(Kline::volume).sum();
        return new Kline(last.date(), first.open(), high, low, last.close(), volume);
    }

    /** 空值安全的价格转换，null时返回0 */
    private double valueOrZero(Double value) {
        return value == null ? 0.0d : value;
    }

    /** 检查响应体是否包含速率限制相关信息 */
    private boolean containsRateLimitMessage(String body) {
        return body != null && body.toLowerCase(Locale.ROOT).contains("limit");
    }
}
