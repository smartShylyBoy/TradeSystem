package ai.pp.trading.marketdata.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * K线领域模型
 * 表示一根K线（蜡烛图），包含日期和OHLCV数据
 */
public class Kline {

    /** 日期 */
    private final LocalDate date;
    /** 开盘价 */
    private final double open;
    /** 最高价 */
    private final double high;
    /** 最低价 */
    private final double low;
    /** 收盘价 */
    private final double close;
    /** 成交量 */
    private final long volume;

    public Kline(LocalDate date, double open, double high, double low, double close, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public LocalDate date() { return date; }
    public double open() { return open; }
    public double high() { return high; }
    public double low() { return low; }
    public double close() { return close; }
    public long volume() { return volume; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Kline)) return false;
        Kline kline = (Kline) o;
        return Double.compare(kline.open, open) == 0 &&
                Double.compare(kline.high, high) == 0 &&
                Double.compare(kline.low, low) == 0 &&
                Double.compare(kline.close, close) == 0 &&
                volume == kline.volume &&
                Objects.equals(date, kline.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, open, high, low, close, volume);
    }
}
