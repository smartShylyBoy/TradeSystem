package ai.pp.trading.indicator.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * 技术指标领域模型
 * 表示某一天的所有技术指标值，包含MA、MACD、RSI、BOLL四类指标
 * 前几个无法计算的位置对应字段为null
 */
public class IndicatorValues {

    /** 日期 */
    private final LocalDate date;
    /** 5日均线 */
    private final Double ma5;
    /** 10日均线 */
    private final Double ma10;
    /** 20日均线 */
    private final Double ma20;
    /** 30日均线 */
    private final Double ma30;
    /** 60日均线 */
    private final Double ma60;
    /** MACD DIF线（快线） */
    private final Double dif;
    /** MACD DEA线（慢线/信号线） */
    private final Double dea;
    /** MACD柱状图 */
    private final Double macdHist;
    /** 6日RSI */
    private final Double rsi6;
    /** 12日RSI */
    private final Double rsi12;
    /** 24日RSI */
    private final Double rsi24;
    /** 布林带上轨 */
    private final Double upperBand;
    /** 布林带中轨（MA20） */
    private final Double middleBand;
    /** 布林带下轨 */
    private final Double lowerBand;

    public IndicatorValues(LocalDate date,
                           Double ma5, Double ma10, Double ma20, Double ma30, Double ma60,
                           Double dif, Double dea, Double macdHist,
                           Double rsi6, Double rsi12, Double rsi24,
                           Double upperBand, Double middleBand, Double lowerBand) {
        this.date = date;
        this.ma5 = ma5;
        this.ma10 = ma10;
        this.ma20 = ma20;
        this.ma30 = ma30;
        this.ma60 = ma60;
        this.dif = dif;
        this.dea = dea;
        this.macdHist = macdHist;
        this.rsi6 = rsi6;
        this.rsi12 = rsi12;
        this.rsi24 = rsi24;
        this.upperBand = upperBand;
        this.middleBand = middleBand;
        this.lowerBand = lowerBand;
    }

    public LocalDate date() { return date; }
    public Double ma5() { return ma5; }
    public Double ma10() { return ma10; }
    public Double ma20() { return ma20; }
    public Double ma30() { return ma30; }
    public Double ma60() { return ma60; }
    public Double dif() { return dif; }
    public Double dea() { return dea; }
    public Double macdHist() { return macdHist; }
    public Double rsi6() { return rsi6; }
    public Double rsi12() { return rsi12; }
    public Double rsi24() { return rsi24; }
    public Double upperBand() { return upperBand; }
    public Double middleBand() { return middleBand; }
    public Double lowerBand() { return lowerBand; }
}
