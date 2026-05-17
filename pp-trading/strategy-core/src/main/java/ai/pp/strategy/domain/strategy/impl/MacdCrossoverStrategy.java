package ai.pp.strategy.domain.strategy.impl;

import ai.pp.strategy.domain.indicator.IndicatorData;
import ai.pp.strategy.domain.model.KlineData;
import ai.pp.strategy.domain.model.TradeSignal;
import ai.pp.strategy.domain.strategy.TradingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * MACD金叉做多策略
 *
 * 开仓条件（全部满足）：
 * 1. 无持仓且 currentIndex >= 30
 * 2. DIF、DEA 前一日和当日都存在
 * 3. 前一日 DIF <= DEA（DIF在DEA下方或相等）
 * 4. 当日 DIF > DEA（DIF上穿DEA，金叉）
 * 5. MACD柱 > 0（确认动能转正）
 * 6. 收盘价 > MA20（价格在中期均线上方，趋势向上）
 * 7. 当日是阳线
 *
 * 平仓条件：
 * 1. 开仓方向为 LONG
 * 2. DIF、DEA 存在
 * 3. DIF < DEA（DIF下穿DEA，死叉）
 * 4. MACD柱 < 0（动能转负）
 */
@Component
public class MacdCrossoverStrategy implements TradingStrategy {

    private static final String ID = "macdCrossover";
    private static final String NAME = "MACD金叉做多";
    private static final String DESCRIPTION = "DIF上穿DEA形成金叉，MACD柱转正，价格站上MA20时做多，死叉时平仓";

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

        Double difCurr = indicators.macd().getDifAt(currentIndex);
        Double deaCurr = indicators.macd().getDeaAt(currentIndex);
        Double difPrev = indicators.macd().getDifAt(currentIndex - 1);
        Double deaPrev = indicators.macd().getDeaAt(currentIndex - 1);
        Double macdBar = indicators.macd().getMacdAt(currentIndex);
        Double ma20 = indicators.ma().getMa20At(currentIndex);

        if (difCurr == null || deaCurr == null || difPrev == null || deaPrev == null
                || macdBar == null || ma20 == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // 前一日 DIF <= DEA，当日 DIF > DEA → 金叉
        if (difPrev > deaPrev || difCurr <= deaCurr) {
            return Optional.empty();
        }

        // MACD柱必须为正
        if (macdBar <= 0) {
            return Optional.empty();
        }

        // 收盘价 > MA20
        if (currK.close() <= ma20) {
            return Optional.empty();
        }

        // 当日阳线
        if (!currK.isBullishPillar()) {
            return Optional.empty();
        }

        String reason = String.format(
            "MACD金叉(DIF=%.4f上穿DEA=%.4f), MACD柱=%.4f, 收盘价%.2f>MA20=%.2f",
            difCurr, deaCurr, macdBar, currK.close(), ma20
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

        Double difCurr = indicators.macd().getDifAt(currentIndex);
        Double deaCurr = indicators.macd().getDeaAt(currentIndex);
        Double macdBar = indicators.macd().getMacdAt(currentIndex);

        if (difCurr == null || deaCurr == null || macdBar == null) {
            return Optional.empty();
        }

        KlineData currK = klines.get(currentIndex);

        // DIF < DEA 且 MACD柱 < 0 → 死叉确认
        if (difCurr < deaCurr && macdBar < 0) {
            String reason = String.format(
                "MACD死叉(DIF=%.4f<DEA=%.4f), MACD柱=%.4f",
                difCurr, deaCurr, macdBar
            );
            return Optional.of(TradeSignal.closeLong(currentIndex, currK.close(), reason));
        }

        return Optional.empty();
    }
}
