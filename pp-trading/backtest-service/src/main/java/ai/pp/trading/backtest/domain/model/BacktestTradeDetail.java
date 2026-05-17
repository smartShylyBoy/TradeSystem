package ai.pp.trading.backtest.domain.model;

/**
 * 回测交易明细
 * 记录一笔交易的开仓和平仓信息
 */
public class BacktestTradeDetail {

    /** 开仓K线索引 */
    private final int openIndex;
    /** 平仓K线索引，未平仓则为 -1 */
    private final int closeIndex;
    /** 开仓日期 */
    private final String openDate;
    /** 平仓日期，未平仓则为 null */
    private final String closeDate;
    /** 开仓价格 */
    private final double openPrice;
    /** 平仓价格，未平仓则为当前最新价 */
    private final double closePrice;
    /** 方向: LONG 或 SHORT */
    private final String direction;
    /** 开仓原因 */
    private final String openReason;
    /** 平仓原因，未平仓则为 null */
    private final String closeReason;
    /** 是否已平仓 */
    private final boolean closed;

    public BacktestTradeDetail(int openIndex, int closeIndex, String openDate, String closeDate,
                               double openPrice, double closePrice, String direction,
                               String openReason, String closeReason, boolean closed) {
        this.openIndex = openIndex;
        this.closeIndex = closeIndex;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.direction = direction;
        this.openReason = openReason;
        this.closeReason = closeReason;
        this.closed = closed;
    }

    public int getOpenIndex() { return openIndex; }
    public int getCloseIndex() { return closeIndex; }
    public String getOpenDate() { return openDate; }
    public String getCloseDate() { return closeDate; }
    public double getOpenPrice() { return openPrice; }
    public double getClosePrice() { return closePrice; }
    public String getDirection() { return direction; }
    public String getOpenReason() { return openReason; }
    public String getCloseReason() { return closeReason; }
    public boolean isClosed() { return closed; }
}
