package ai.pp.strategy.domain.model;

/**
 * 交易信号
 * 封装策略产生的开仓/平仓信号
 */
public class TradeSignal {

    private final int index;
    private final double price;
    private final Direction direction;
    private final String type;
    private final String reason;

    public TradeSignal(int index, double price, Direction direction, String type, String reason) {
        this.index = index;
        this.price = price;
        this.direction = direction;
        this.type = type;
        this.reason = reason;
    }

    public int index() { return index; }
    public double price() { return price; }
    public Direction direction() { return direction; }
    public String type() { return type; }
    public String reason() { return reason; }

    /**
     * 创建做多开仓信号
     */
    public static TradeSignal openLong(int index, double price, String reason) {
        return new TradeSignal(index, price, Direction.LONG, "OPEN", reason);
    }

    /**
     * 创建做多平仓信号
     */
    public static TradeSignal closeLong(int index, double price, String reason) {
        return new TradeSignal(index, price, Direction.LONG, "CLOSE", reason);
    }

    /**
     * 创建做空开仓信号
     */
    public static TradeSignal openShort(int index, double price, String reason) {
        return new TradeSignal(index, price, Direction.SHORT, "OPEN", reason);
    }

    /**
     * 创建做空平仓信号
     */
    public static TradeSignal closeShort(int index, double price, String reason) {
        return new TradeSignal(index, price, Direction.SHORT, "CLOSE", reason);
    }
}
