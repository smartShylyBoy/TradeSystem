package ai.pp.trading.marketdata.application.usecase;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.marketdata.application.exception.InvalidPeriodException;
import ai.pp.trading.marketdata.domain.model.Kline;
import ai.pp.trading.marketdata.domain.port.KlineRepository;
import ai.pp.trading.marketdata.domain.port.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 获取K线数据用例
 * 实现缓存优先策略：先查数据库缓存，未命中则从FMP API获取并缓存
 */
@Service
public class GetKlineUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetKlineUseCase.class);

    /** K线数据仓储端口 */
    private final KlineRepository klineRepository;
    /** 市场数据提供者端口 */
    private final MarketDataProvider marketDataProvider;

    public GetKlineUseCase(KlineRepository klineRepository, MarketDataProvider marketDataProvider) {
        this.klineRepository = klineRepository;
        this.marketDataProvider = marketDataProvider;
    }

    /**
     * 获取K线数据
     * @param symbol 股票代码（如AAPL）
     * @param market 市场（如us）
     * @param period 周期（daily/weekly/monthly）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线响应列表
     */
    public List<KlineResponse> getKlines(String symbol, String market, String period, LocalDate startDate, LocalDate endDate) {
        // 标准化输入参数
        String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
        String normalizedMarket = market.trim().toLowerCase(Locale.ROOT);
        String normalizedPeriod = period.trim().toLowerCase(Locale.ROOT);
        String tableName = resolveTableName(normalizedPeriod);

        // 先从数据库缓存查询
        List<Kline> cachedKlines = klineRepository.findByRange(
                tableName,
                normalizedSymbol,
                normalizedMarket,
                startDate,
                endDate
        );

        // 缓存命中，直接返回
        if (!cachedKlines.isEmpty()) {
            log.info("K线缓存命中: 从数据库读取 symbol={}, market={}, period={}, startDate={}, endDate={}",
                    normalizedSymbol, normalizedMarket, normalizedPeriod, startDate, endDate);
            return toResponses(cachedKlines);
        }

        // 缓存未命中，从FMP API获取
        log.info("K线缓存未命中: 从FMP API获取 symbol={}, market={}, period={}, startDate={}, endDate={}",
                normalizedSymbol, normalizedMarket, normalizedPeriod, startDate, endDate);

        List<Kline> fetchedKlines = marketDataProvider.getKlines(
                normalizedSymbol,
                normalizedMarket,
                normalizedPeriod,
                startDate,
                endDate
        );

        // 将获取的数据缓存到数据库
        if (!fetchedKlines.isEmpty()) {
            klineRepository.saveAll(tableName, normalizedSymbol, normalizedMarket, fetchedKlines);
        }else {
            log.info("查询FMP API返回空: 从FMP API获取 symbol={}, market={}, period={}, startDate={}, endDate={}",
                    normalizedSymbol, normalizedMarket, normalizedPeriod, startDate, endDate);
        }

        return toResponses(fetchedKlines);
    }

    /**
     * 根据周期解析对应的数据库表名
     * @param period 周期字符串
     * @return 表名
     */
    private String resolveTableName(String period) {
        switch (period) {
            case "daily":
                return "kline_daily";
            case "weekly":
                return "kline_weekly";
            case "monthly":
                return "kline_monthly";
            default:
                throw new InvalidPeriodException("不支持的周期: " + period);
        }
    }

    /**
     * 将领域模型转换为响应DTO
     * @param klines K线领域模型列表
     * @return K线响应列表
     */
    private List<KlineResponse> toResponses(List<Kline> klines) {
        return klines.stream()
                .map(kline -> new KlineResponse(
                        kline.date().toString(),
                        kline.open(),
                        kline.high(),
                        kline.low(),
                        kline.close(),
                        kline.volume()
                ))
                .collect(Collectors.toList());
    }
}
