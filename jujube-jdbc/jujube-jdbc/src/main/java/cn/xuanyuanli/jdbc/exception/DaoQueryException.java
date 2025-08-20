package cn.xuanyuanli.jdbc.exception;

/**
 * 查询异常
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class DaoQueryException extends RuntimeException {

    /**
     * 数据访问查询异常
     */
    public DaoQueryException() {
    }

    /**
     * 数据访问查询异常
     *
     * @param message 消息
     */
    public DaoQueryException(String message) {
        super(message);
    }

    /**
     * 数据访问查询异常
     *
     * @param message 消息
     * @param cause   导致
     */
    public DaoQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
