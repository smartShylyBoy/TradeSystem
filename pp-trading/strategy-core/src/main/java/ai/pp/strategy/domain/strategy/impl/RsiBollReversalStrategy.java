package ai.pp.strategy.domain.strategy.impl;

import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * RSI超卖布林下轨反弹策略
 *
 * 开仓条件（全部满足）：
 * 1. 无持仓且 currentIndex >= 30
 * 2. RSI6 前一日和当日都存在
 * 3. 前一日 RSI6 < 20（超卖区域）
 * 4. 当日 RSI6 > 20（RSI回升离开超卖区）
 * 5. 收盘价 <= 布林带下轨 * 1.02（价格接近或触及下轨）
 * 6. 当日是阳线（反弹确认）
 * 7. 收盘价 > 最低价 * 1.005（非光头阴线，有下影线支撑）
 *
 * 平仓条件：
 * 1. 开仓方向为 LONG
 * 2. RSI6 存在
 * 3. RSI6 > 70（进入超买区域）或 收盘价 > 布林带上轨（突破上轨）
 */
@Component
public class RsiBollReversalStrategy implements TradingStrategy {

    private static final String ID = "rsiBollReversal";
    private static final String NAME = "RSI超卖布林反弹";
    private static final String DESCRIPTION = "RSI6跌入超卖区后回升，同时价格触及布林带下轨时做多，RSI超买或突破上轨时平仓";

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
        if (hasPosition || currentIndex < 30) {
            return Optional.empty();
        }

        Double rsi6Curr = indicators.rsi().getRsi6At(currentIndex);
        Double rsi6Prev = indicators.rsi().getRsi6At(currentIndex - 1);
        Double bollLower = indicators.boll().getLowerAt(currentIndex);

        if (rsi6Curr == null || rsi6Prev == null || bollLower == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // 前一日 RSI6 < 20（超卖）
        if (rsi6Prev >= 20) {
            return Optional.empty();
        }

        // 当日 RSI6 回升离开超卖区
        if (rsi6Curr <= 20) {
            return Optional.empty();
        }

        // 收盘价接近布林带下轨（容差2%）
        if (currK.close() > bollLower * 1.02) {
            return Optional.empty();
        }

        // 当日阳线
        if (!currK.isBullishPillar()) {
            return Optional.empty();
        }

        // 有下影线支撑（收盘价相对最低价有一定幅度）
        if (currK.close() <= currK.low() * 1.005) {
            return Optional.empty();
        }

        String reason = String.format(
            "RSI6超卖反弹(前一日=%.1f→当日=%.1f), 触及布林下轨%.2f",
            rsi6Prev, rsi6Curr, bollLower
        );
        return Optional.of(TradeSignal.openLong(currentIndex, currK.close(), reason));
    }

    @Override
    public Optional<TradeSignal> checkCloseSignal(List<KlineData> klines,
                                                   IndicatorData indicators,
                                                   int currentIndex,
                                                   TradeSignal openSignal) {
        if (openSignal.direction() != ai.pp.strategy.domain.model.Direction.LONG) {
            return Optional.empty();
        }

        Double rsi6 = indicators.rsi().getRsi6At(currentIndex);
        Double bollUpper = indicators.boll().getUpperAt(currentIndex);

        if (rsi6 == null || bollUpper == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // RSI超买 或 突破布林上轨
        if (rsi6 > 70) {
            String reason = String.format("RSI6超买(%.1f>70)", rsi6);
            return Optional.of(TradeSignal.closeLong(currentIndex, currK.close(), reason));
        }

        if (currK.close() > bollUpper) {
            String reason = String.format("突破布林上轨(收盘价%.2f>上轨%.2f)", currK.close(), bollUpper);
            return Optional.of(TradeSignal.closeLong(currentIndex, currK.close(), reason));
        }

        return Optional.empty();
    }
}
