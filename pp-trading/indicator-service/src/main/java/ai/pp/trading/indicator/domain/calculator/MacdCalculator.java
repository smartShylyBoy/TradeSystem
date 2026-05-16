package ai.pp.trading.indicator.domain.calculator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MACD指标计算器
 * 纯计算领域服务，计算DIF、DEA、MACD柱三个子指标
 * DIF = EMA(12) - EMA(26)，DEA = DIF的9日EMA，MACD柱 = (DIF - DEA) × 2
 * EMA递推: EMA_today = close × (2/(N+1)) + EMA_yesterday × (1 - 2/(N+1))
 */
@Component
public class MacdCalculator {

    /** 短期EMA周期 */
    private static final int SHORT_PERIOD = 12;
    /** 长期EMA周期 */
    private static final int LONG_PERIOD = 26;
    /** 信号线DEA的EMA周期 */
    private static final int SIGNAL_PERIOD = 9;

    /**
     * 计算DIF线（快线）
     * @param closes 收盘价数组
     * @return DIF值列表
     */
    public List<Double> calculateDif(double[] closes) {
        List<Double> ema12 = calculateEma(closes, SHORT_PERIOD);
        List<Double> ema26 = calculateEma(closes, LONG_PERIOD);
        int len = closes.length;
        List<Double> dif = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            if (ema12.get(i) == null || ema26.get(i) == null) {
                dif.add(null);
            } else {
                dif.add(round(ema12.get(i) - ema26.get(i)));
            }
        }
        return dif;
    }

    /**
     * 计算DEA线（慢线/信号线）
     * @param dif DIF值列表
     * @return DEA值列表
     */
    public List<Double> calculateDea(List<Double> dif) {
        return calculateEmaFromNullable(dif, SIGNAL_PERIOD);
    }

    /**
     * 计算MACD柱状图
     * @param dif DIF值列表
     * @param dea DEA值列表
     * @return MACD柱值列表
     */
    public List<Double> calculateMacdHist(List<Double> dif, List<Double> dea) {
        int len = dif.size();
        List<Double> macd = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            if (dif.get(i) == null || dea.get(i) == null) {
                macd.add(null);
            } else {
                macd.add(round((dif.get(i) - dea.get(i)) * 2));
            }
        }
        return macd;
    }

    private List<Double> calculateEma(double[] values, int period) {
        int len = values.length;
        List<Double> ema = new ArrayList<>(len);
        double multiplier = 2.0 / (period + 1);
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                ema.add(values[0]);
            } else {
                double prev = ema.get(i - 1);
                ema.add(round(values[i] * multiplier + prev * (1 - multiplier)));
            }
        }
        return ema;
    }

    private List<Double> calculateEmaFromNullable(List<Double> values, int period) {
        int len = values.size();
        List<Double> ema = new ArrayList<>(len);
        double multiplier = 2.0 / (period + 1);
        Double prevEma = null;
        for (int i = 0; i < len; i++) {
            if (values.get(i) == null) {
                ema.add(null);
                continue;
            }
            if (prevEma == null) {
                prevEma = values.get(i);
                ema.add(round(prevEma));
            } else {
                prevEma = values.get(i) * multiplier + prevEma * (1 - multiplier);
                ema.add(round(prevEma));
            }
        }
        return ema;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
