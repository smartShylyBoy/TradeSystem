package ai.pp.trading.common.dto;

/**
 * 错误响应DTO
 * 统一的API错误响应格式
 */
public class ErrorResponse {

    /** HTTP状态码 */
    private final int status;
    /** 错误信息 */
    private final String message;
    /** 错误发生时间戳 */
    private final String timestamp;

    public ErrorResponse(int status, String message, String timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
