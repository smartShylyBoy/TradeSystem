package ai.pp.strategy.domain.strategy.impl;

import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 均线多头排列回踩策略
 *
 * 开仓条件（全部满足）：
 * 1. 无持仓且 currentIndex >= 30
 * 2. MA5、MA10、MA20、MA60 都存在
 * 3. MA5 > MA10 > MA20 > MA60（多头排列）
 * 4. 收盘价 > MA20（价格在中期均线上方）
 * 5. 当前最低价 <= MA10（回踩到MA10附近）
 * 6. 收盘价 > MA10（收盘站在MA10上方，支撑有效）
 * 7. 当日是阳线（反弹确认）
 *
 * 平仓条件：
 * 1. 开仓方向为 LONG
 * 2. MA5、MA10 存在
 * 3. MA5 < MA10（短期均线死叉，多头排列破坏）
 * 4. 收盘价 < MA10（价格跌破MA10）
 */
@Component
public class MaBullishPullbackStrategy implements TradingStrategy {

    private static final String ID = "maBullishPullback";
    private static final String NAME = "均线多头回踩";
    private static final String DESCRIPTION = "MA5>MA10>MA20>MA60多头排列时，回踩MA10获支撑做多，均线死叉时平仓";

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

        Double ma5 = indicators.ma().getMa5At(currentIndex);
        Double ma10 = indicators.ma().getMa10At(currentIndex);
        Double ma20 = indicators.ma().getMa20At(currentIndex);
        Double ma60 = indicators.ma().getMa60At(currentIndex);

        if (ma5 == null || ma10 == null || ma20 == null || ma60 == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // 多头排列: MA5 > MA10 > MA20 > MA60
        if (!(ma5 > ma10 && ma10 > ma20 && ma20 > ma60)) {
            return Optional.empty();
        }

        // 收盘价 > MA20
        if (currK.close() <= ma20) {
            return Optional.empty();
        }

        // 回踩MA10: 最低价触及MA10
        if (currK.low() > ma10) {
            return Optional.empty();
        }

        // 收盘站稳MA10上方
        if (currK.close() <= ma10) {
            return Optional.empty();
        }

        // 当日阳线
        if (!currK.isBullishPillar()) {
            return Optional.empty();
        }

        String reason = String.format(
            "均线多头排列(MA5=%.2f>MA10=%.2f>MA20=%.2f>MA60=%.2f), 回踩MA10支撑",
            ma5, ma10, ma20, ma60
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

        Double ma5 = indicators.ma().getMa5At(currentIndex);
        Double ma10 = indicators.ma().getMa10At(currentIndex);

        if (ma5 == null || ma10 == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // MA5 < MA10（短期均线死叉）且 收盘价 < MA10
        if (ma5 < ma10 && currK.close() < ma10) {
            String reason = String.format(
                "均线死叉(MA5=%.2f<MA10=%.2f), 收盘价%.2f跌破MA10",
                ma5, ma10, currK.close()
            );
            return Optional.of(TradeSignal.closeLong(currentIndex, currK.close(), reason));
        }

        return Optional.empty();
    }
}
