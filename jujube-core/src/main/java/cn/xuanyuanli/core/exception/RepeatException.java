package cn.xuanyuanli.core.exception;

/**
 * 元素重复异常
 *
 * @author John Li
 * @date 2021/09/01
 */
public class RepeatException extends RuntimeException{

    /**
     * 重复异常
     */
    public RepeatException() {
    }

    /**
     * 重复异常
     *
     * @param message 消息
     */
    public RepeatException(String message) {
        super(message);
    }

    /**
     * 重复异常
     *
     * @param message 消息
     * @param cause   导致
     */
    public RepeatException(String message, Throwable cause) {
        super(message, cause);
    }
}
