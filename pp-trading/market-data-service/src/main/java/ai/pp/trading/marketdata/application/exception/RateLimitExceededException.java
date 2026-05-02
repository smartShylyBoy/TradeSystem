package ai.pp.trading.marketdata.application.exception;

/**
 * API速率限制超出异常
 * 当外部API（如FMP）的调用频率超过限制时抛出
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
