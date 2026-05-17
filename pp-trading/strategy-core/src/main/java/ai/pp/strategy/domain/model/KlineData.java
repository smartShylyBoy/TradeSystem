package ai.pp.strategy.domain.model;

/**
 * K线数据
 * 封装单根K线的OHLCV数据
 */
public class KlineData {

    private final String date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    public KlineData(String date, double open, double high, double low, double close, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String date() { return date; }
    public double open() { return open; }
    public double high() { return high; }
    public double low() { return low; }
    public double close() { return close; }
    public long volume() { return volume; }

    /**
     * 是否阳线（收盘价 > 开盘价）
     */
    public boolean isBullishPillar() {
        return close > open;
    }

    /**
     * 是否阴线（收盘价 < 开盘价）
     */
    public boolean isBearishPillar() {
        return close < open;
    }
}
