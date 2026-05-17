package ai.pp.strategy.domain.strategy.impl;

import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;
import ai.pp.strategy.domain.strategy.StrategyCalculator;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 高位突破-做多策略
 *
 * 开仓条件（全部满足）：
 * 1. 无持仓且 currentIndex >= 10
 * 2. 过去250周期最高价（从currentIndex-2开始往前算，minPeriod=5）
 * 3. maxHigh 不为 NaN
 * 4. MACD: dif != null && dea != null
 * 5. MA5 != null
 * 6. 当前K线收盘价 > maxHigh（突破）
 * 7. 当前K线是阳线
 * 8. 前一根K线最高价 <= maxHigh（前一根未突破）
 * 9. dif > dea（MACD多头排列）
 * 10. min(前一根收盘价, 当前最低价) <= ma5（回踩均线）
 *
 * 平仓条件：
 * 1. 开仓方向为 LONG
 * 2. currentIndex >= 5
 * 3. MA5、MA10 当前和前一日都不为 null
 * 4. 连续2根K线的收盘价都 < MA5 且 都 < MA10
 */
@Component
public class BullishLongStrategy implements TradingStrategy {

    private static final String ID = "bullishLong";
    private static final String NAME = "高位突破-做多";
    private static final String DESCRIPTION = "当价格突破过去250周期最高价，且满足MACD多头、均线回踩条件时做多";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Optional<TradeSignal> checkOpenSignal(List<KlineData> klines,
                                                  IndicatorData indicators,
                                                  int currentIndex,
                                                  boolean hasPosition) {
        // 条件1: 有持仓或索引不足，直接返回
        if (hasPosition || currentIndex < 10) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);
        KlineData lastK1 = klines.get(currentIndex - 1);

        // 条件2: 计算过去250周期最高价（从currentIndex-2开始往前算）
        double maxHigh = StrategyCalculator.getValidMaxCloseHighBetweenLastPeriod(
            klines, currentIndex - 2, 250, 5
        );

        // 条件3: maxHigh 为 NaN 则不满足
        if (Double.isNaN(maxHigh)) {
            return Optional.empty();
        }

        // 条件4: MACD 数据必须存在
        Double dif = indicators.macd().getDifAt(currentIndex);
        Double dea = indicators.macd().getDeaAt(currentIndex);
        if (dif == null || dea == null) {
            return Optional.empty();
        }

        // 条件5: MA5 必须存在
        Double ma5 = indicators.ma().getMa5At(currentIndex);
        if (ma5 == null) {
            return Optional.empty();
        }

        // 条件6: 当前收盘价必须突破 maxHigh
        if (currK.close() <= maxHigh) {
            return Optional.empty();
        }

        // 条件7: 当前K线必须是阳线
        if (!currK.isBullishPillar()) {
            return Optional.empty();
        }

        // 条件8: 前一根K线最高价必须 <= maxHigh（前一根未突破）
        if (lastK1.high() > maxHigh) {
            return Optional.empty();
        }

        // 条件9: MACD 多头排列 (dif > dea)
        if (dif <= dea) {
            return Optional.empty();
        }

        // 条件10: 回踩均线条件 - min(前一根收盘价, 当前最低价) <= ma5
        double pullbackPrice = Math.min(lastK1.close(), currK.low());
        if (pullbackPrice > ma5) {
            return Optional.empty();
        }

        // 所有条件满足，产生开仓信号
        String reason = String.format(
            "突破250周期高点%.2f, MACD多头(DIF=%.4f>DEA=%.4f), 回踩MA5",
            maxHigh, dif, dea
        );
        return Optional.of(TradeSignal.openLong(currentIndex, currK.close(), reason));
    }

    @Override
    public Optional<TradeSignal> checkCloseSignal(List<KlineData> klines,
                                                   IndicatorData indicators,
                                                   int currentIndex,
                                                   TradeSignal openSignal) {
        // 条件1: 只处理做多仓位
        if (openSignal.direction() != ai.pp.strategy.domain.model.Direction.LONG) {
            return Optional.empty();
        }

        // 条件2: 索引不足
        if (currentIndex < 5) {
            return Optional.empty();
        }

        // 条件3: MA5、MA10 当前和前一日都必须存在
        Double ma5Curr = indicators.ma().getMa5At(currentIndex);
        Double ma5Prev = indicators.ma().getMa5At(currentIndex - 1);
        Double ma10Curr = indicators.ma().getMa10At(currentIndex);
        Double ma10Prev = indicators.ma().getMa10At(currentIndex - 1);

        if (ma5Curr == null || ma5Prev == null || ma10Curr == null || ma10Prev == null) {
            return Optional.empty();
        }

        // 条件4: 连续2根K线的收盘价都 < MA5 且 都 < MA10
        KlineData currK = klines.get(currentIndex);
        KlineData prevK = klines.get(currentIndex - 1);

        boolean currBelowMa5 = currK.close() < ma5Curr;
        boolean currBelowMa10 = currK.close() < ma10Curr;
        boolean prevBelowMa5 = prevK.close() < ma5Prev;
        boolean prevBelowMa10 = prevK.close() < ma10Prev;

        if (currBelowMa5 && currBelowMa10 && prevBelowMa5 && prevBelowMa10) {
            String reason = "连续2根K线收盘价低于MA5和MA10";
            return Optional.of(TradeSignal.closeLong(currentIndex, currK.close(), reason));
        }

        return Optional.empty();
    }
}
