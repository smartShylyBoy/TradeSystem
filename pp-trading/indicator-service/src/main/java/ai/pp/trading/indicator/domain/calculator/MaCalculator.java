package ai.pp.trading.indicator.domain.calculator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MA（简单移动平均线）计算器
 * 纯计算领域服务，输入收盘价数组，输出MA数组
 * MA(N) = 过去N个收盘价的算术平均，前N-1个位置为null
 */
@Component
public class MaCalculator {

    /**
     * 计算指定周期的MA
     * 使用滑动窗口优化，避免重复求和
     * @param closes 收盘价数组
     * @param period 均线周期（如5、10、20）
     * @return MA值列表，前period-1个位置为null
     */
    public List<Double> calculate(double[] closes, int period) {
        int len = closes.length;
        List<Double> result = new ArrayList<>(len);
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum += closes[i];
            if (i >= period) {
                sum -= closes[i - period];
            }
            if (i >= period - 1) {
                result.add(round(sum / period));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
