package ai.pp.trading.indicator.application.usecase;

import ai.pp.trading.common.dto.KlineResponse;
import ai.pp.trading.common.dto.indicator.*;
import ai.pp.trading.indicator.domain.calculator.BollCalculator;
import ai.pp.trading.indicator.domain.calculator.MaCalculator;
import ai.pp.trading.indicator.domain.calculator.MacdCalculator;
import ai.pp.trading.indicator.domain.calculator.RsiCalculator;
import ai.pp.trading.indicator.domain.model.IndicatorValues;
import ai.pp.trading.indicator.domain.port.IndicatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 计算技术指标用例
 * 实现缓存优先策略：先查数据库缓存，未命中则从K线数据计算指标并缓存
 * 缓存判断逻辑：数据非空、日期范围完全覆盖、条数一致
 */
@Service
public class CalculateIndicatorsUseCase {

    private static final Logger log = LoggerFactory.getLogger(CalculateIndicatorsUseCase.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 指标数据仓储端口 */
    private final IndicatorRepository indicatorRepository;
    /** MA计算器 */
    private final MaCalculator maCalculator;
    /** MACD计算器 */
    private final MacdCalculator macdCalculator;
    /** RSI计算器 */
    private final RsiCalculator rsiCalculator;
    /** 布林带计算器 */
    private final BollCalculator bollCalculator;

    public CalculateIndicatorsUseCase(IndicatorRepository indicatorRepository,
                                      MaCalculator maCalculator,
                                      MacdCalculator macdCalculator,
                                      RsiCalculator rsiCalculator,
                                      BollCalculator bollCalculator) {
        this.indicatorRepository = indicatorRepository;
        this.maCalculator = maCalculator;
        this.macdCalculator = macdCalculator;
        this.rsiCalculator = rsiCalculator;
        this.bollCalculator = bollCalculator;
    }

    /**
     * 计算技术指标
     * @param request 包含K线数据和股票标识的请求
     * @return 四类技术指标的计算结果
     */
    public IndicatorResponse calculate(IndicatorRequest request) {
        String symbol = request.getSymbol().trim().toUpperCase(Locale.ROOT);
        String market = request.getMarket().trim().toLowerCase(Locale.ROOT);
        List<KlineResponse> klines = request.getKlines();

        if (klines == null || klines.isEmpty()) {
            return emptyResponse();
        }

        LocalDate startDate = LocalDate.parse(klines.get(0).getDate(), DATE_FMT);
        LocalDate endDate = LocalDate.parse(klines.get(klines.size() - 1).getDate(), DATE_FMT);

        // 查缓存
        List<IndicatorValues> cached = indicatorRepository.findByRange(symbol, market, startDate, endDate);
        if (!cached.isEmpty()
                && !cached.get(0).date().isAfter(startDate)
                && !cached.get(cached.size() - 1).date().isBefore(endDate)
                && cached.size() == klines.size()) {
            log.info("指标缓存命中: symbol={}, market={}, size={}", symbol, market, cached.size());
            return fromCachedValues(cached);
        }

        if (!cached.isEmpty()) {
            log.info("指标缓存不完整: 缓存{}条, 请求{}条, 重新计算", cached.size(), klines.size());
        }

        // 从 K 线计算
        log.info("开始计算指标: symbol={}, market={}, klines={}", symbol, market, klines.size());
        double[] closes = extractCloses(klines);

        List<Double> ma5 = maCalculator.calculate(closes, 5);
        List<Double> ma10 = maCalculator.calculate(closes, 10);
        List<Double> ma20 = maCalculator.calculate(closes, 20);
        List<Double> ma30 = maCalculator.calculate(closes, 30);
        List<Double> ma60 = maCalculator.calculate(closes, 60);

        List<Double> dif = macdCalculator.calculateDif(closes);
        List<Double> dea = macdCalculator.calculateDea(dif);
        List<Double> macdHist = macdCalculator.calculateMacdHist(dif, dea);

        List<Double> rsi6 = rsiCalculator.calculate(closes, 6);
        List<Double> rsi12 = rsiCalculator.calculate(closes, 12);
        List<Double> rsi24 = rsiCalculator.calculate(closes, 24);

        List<Double> bollMiddle = bollCalculator.calculateMiddle(closes);
        List<Double> bollUpper = bollCalculator.calculateUpper(closes, bollMiddle);
        List<Double> bollLower = bollCalculator.calculateLower(closes, bollMiddle);

        // 构建 IndicatorValues 列表并缓存
        List<IndicatorValues> valuesList = new ArrayList<>(klines.size());
        for (int i = 0; i < klines.size(); i++) {
            LocalDate date = LocalDate.parse(klines.get(i).getDate(), DATE_FMT);
            valuesList.add(new IndicatorValues(date,
                    ma5.get(i), ma10.get(i), ma20.get(i), ma30.get(i), ma60.get(i),
                    dif.get(i), dea.get(i), macdHist.get(i),
                    rsi6.get(i), rsi12.get(i), rsi24.get(i),
                    bollUpper.get(i), bollMiddle.get(i), bollLower.get(i)));
        }

        indicatorRepository.saveAll(symbol, market, valuesList);
        log.info("指标计算完成并已缓存: symbol={}, market={}, count={}", symbol, market, valuesList.size());

        return new IndicatorResponse(
                new MacdResult(dif, dea, macdHist),
                new MaResult(ma5, ma10, ma20, ma30, ma60),
                new RsiResult(rsi6, rsi12, rsi24),
                new BollResult(bollUpper, bollMiddle, bollLower)
        );
    }

    /** 从K线列表中提取收盘价数组 */
    private double[] extractCloses(List<KlineResponse> klines) {
        double[] closes = new double[klines.size()];
        for (int i = 0; i < klines.size(); i++) {
            closes[i] = klines.get(i).getClose();
        }
        return closes;
    }

    /** 将缓存的IndicatorValues列表转换为IndicatorResponse */
    private IndicatorResponse fromCachedValues(List<IndicatorValues> cached) {
        int size = cached.size();
        List<Double> ma5 = new ArrayList<>(size);
        List<Double> ma10 = new ArrayList<>(size);
        List<Double> ma20 = new ArrayList<>(size);
        List<Double> ma30 = new ArrayList<>(size);
        List<Double> ma60 = new ArrayList<>(size);
        List<Double> dif = new ArrayList<>(size);
        List<Double> dea = new ArrayList<>(size);
        List<Double> macdHist = new ArrayList<>(size);
        List<Double> rsi6 = new ArrayList<>(size);
        List<Double> rsi12 = new ArrayList<>(size);
        List<Double> rsi24 = new ArrayList<>(size);
        List<Double> upper = new ArrayList<>(size);
        List<Double> middle = new ArrayList<>(size);
        List<Double> lower = new ArrayList<>(size);

        for (IndicatorValues v : cached) {
            ma5.add(v.ma5());
            ma10.add(v.ma10());
            ma20.add(v.ma20());
            ma30.add(v.ma30());
            ma60.add(v.ma60());
            dif.add(v.dif());
            dea.add(v.dea());
            macdHist.add(v.macdHist());
            rsi6.add(v.rsi6());
            rsi12.add(v.rsi12());
            rsi24.add(v.rsi24());
            upper.add(v.upperBand());
            middle.add(v.middleBand());
            lower.add(v.lowerBand());
        }

        return new IndicatorResponse(
                new MacdResult(dif, dea, macdHist),
                new MaResult(ma5, ma10, ma20, ma30, ma60),
                new RsiResult(rsi6, rsi12, rsi24),
                new BollResult(upper, middle, lower)
        );
    }

    /** 构建空的IndicatorResponse（K线为空时返回） */
    private IndicatorResponse emptyResponse() {
        List<Double> empty = new ArrayList<>();
        return new IndicatorResponse(
                new MacdResult(empty, empty, empty),
                new MaResult(empty, empty, empty, empty, empty),
                new RsiResult(empty, empty, empty),
                new BollResult(empty, empty, empty)
        );
    }
}
