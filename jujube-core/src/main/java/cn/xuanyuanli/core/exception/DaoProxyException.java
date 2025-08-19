package cn.xuanyuanli.core.exception;

/**
 * Dao代理异常
 *
 * @author John Li
 * @date 2021/09/01
 */
public class DaoProxyException extends  RuntimeException{

    /**
     * 数据访问代理异常
     */
    public DaoProxyException() {
    }

    /**
     * 数据访问代理异常
     *
     * @param message 消息
     */
    public DaoProxyException(String message) {
        super(message);
    }

    /**
     * 数据访问代理异常
     *
     * @param message 消息
     * @param cause   导致
     */
    public DaoProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}
