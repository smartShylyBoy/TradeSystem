package ai.pp.trading.marketdata.application.exception;

/**
 * 无效周期异常
 * 当请求的K线周期（如daily/weekly/monthly）不支持时抛出
 */
public class InvalidPeriodException extends RuntimeException {

    public InvalidPeriodException(String message) {
        super(message);
    }
}
