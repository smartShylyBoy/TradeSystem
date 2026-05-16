package ai.pp.trading.indicator.domain.calculator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * BOLL（布林带）计算器
 * 纯计算领域服务，计算上轨、中轨、下轨三条线
 * 中轨 = MA20，上轨 = 中轨 + 2×标准差，下轨 = 中轨 - 2×标准差
 * 标准差使用总体标准差（population std dev）
 */
@Component
public class BollCalculator {

    /** 均线周期 */
    private static final int PERIOD = 20;
    /** 标准差倍数 */
    private static final double MULTIPLIER = 2.0;

    /**
     * 计算中轨（MA20）
     * @param closes 收盘价数组
     * @return 中轨值列表，前19个位置为null
     */
    public List<Double> calculateMiddle(double[] closes) {
        int len = closes.length;
        List<Double> result = new ArrayList<>(len);
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum += closes[i];
            if (i >= PERIOD) {
                sum -= closes[i - PERIOD];
            }
            if (i >= PERIOD - 1) {
                result.add(round(sum / PERIOD));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    /**
     * 计算上轨 = 中轨 + 2×标准差
     * @param closes 收盘价数组
     * @param middle 中轨值列表
     * @return 上轨值列表
     */
    public List<Double> calculateUpper(double[] closes, List<Double> middle) {
        int len = closes.length;
        List<Double> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            if (middle.get(i) == null) {
                result.add(null);
            } else {
                double std = populationStd(closes, i, PERIOD);
                result.add(round(middle.get(i) + MULTIPLIER * std));
            }
        }
        return result;
    }

    /**
     * 计算下轨 = 中轨 - 2×标准差
     * @param closes 收盘价数组
     * @param middle 中轨值列表
     * @return 下轨值列表
     */
    public List<Double> calculateLower(double[] closes, List<Double> middle) {
        int len = closes.length;
        List<Double> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            if (middle.get(i) == null) {
                result.add(null);
            } else {
                double std = populationStd(closes, i, PERIOD);
                result.add(round(middle.get(i) - MULTIPLIER * std));
            }
        }
        return result;
    }

    private double populationStd(double[] values, int endIndex, int period) {
        double sum = 0;
        for (int i = endIndex - period + 1; i <= endIndex; i++) {
            sum += values[i];
        }
        double mean = sum / period;
        double sumSq = 0;
        for (int i = endIndex - period + 1; i <= endIndex; i++) {
            double diff = values[i] - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / period);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
