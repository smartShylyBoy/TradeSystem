package ai.pp.trading.indicator.domain.calculator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RSI（相对强弱指数）计算器
 * 纯计算领域服务，输入收盘价数组，输出RSI数组
 * RSI = 100 - 100 / (1 + RS)，RS = 平均涨幅 / 平均跌幅
 * 使用Wilder平滑法递推: avgGain = (prevAvgGain × (N-1) + currentGain) / N
 */
@Component
public class RsiCalculator {

    /**
     * 计算指定周期的RSI
     * @param closes 收盘价数组
     * @param period RSI周期（如6、12、24）
     * @return RSI值列表，前period个位置为null
     */
    public List<Double> calculate(double[] closes, int period) {
        int len = closes.length;
        List<Double> result = new ArrayList<>(len);

        if (len <= period) {
            for (int i = 0; i < len; i++) {
                result.add(null);
            }
            return result;
        }

        // 前 period 个位置为 null
        for (int i = 0; i < period; i++) {
            result.add(null);
        }

        // 计算初始平均涨幅和平均跌幅
        double avgGain = 0;
        double avgLoss = 0;
        for (int i = 1; i <= period; i++) {
            double change = closes[i] - closes[i - 1];
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += Math.abs(change);
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 第 period 个位置的 RSI
        result.add(calcRsi(avgGain, avgLoss));

        // 递推计算后续 RSI
        for (int i = period + 1; i < len; i++) {
            double change = closes[i] - closes[i - 1];
            double currentGain = change > 0 ? change : 0;
            double currentLoss = change < 0 ? Math.abs(change) : 0;

            avgGain = (avgGain * (period - 1) + currentGain) / period;
            avgLoss = (avgLoss * (period - 1) + currentLoss) / period;

            result.add(calcRsi(avgGain, avgLoss));
        }

        return result;
    }

    private double calcRsi(double avgGain, double avgLoss) {
        if (avgLoss == 0) {
            return 100.0;
        }
        double rs = avgGain / avgLoss;
        return round(100.0 - 100.0 / (1.0 + rs));
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
