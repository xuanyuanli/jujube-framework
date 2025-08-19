package cn.xuanyuanli.jdbc.exception;

/**
 * Dao初始化异常
 *
 * @author John Li
 * @date 2021/09/01
 */
public class DaoInitializeException extends  RuntimeException{

    /**
     * 数据访问初始化异常
     */
    public DaoInitializeException() {
    }

    /**
     * 数据访问初始化异常
     *
     * @param message 消息
     */
    public DaoInitializeException(String message) {
        super(message);
    }

    /**
     * 数据访问初始化异常
     *
     * @param message 消息
     * @param cause   导致
     */
    public DaoInitializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
