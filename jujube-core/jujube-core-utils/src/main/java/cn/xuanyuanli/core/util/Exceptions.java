package cn.xuanyuanli.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 异常工具
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exceptions {

    /**
     * 抛出运行时异常
     *
     * @param e e
     */
    public static void throwException(Throwable e) {
        throw new RuntimeException(e);
    }

    /**
     * 获得异常堆栈信息
     *
     * @param exception 异常
     * @return {@link String}
     */
    public static String exceptionToString(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * 获得异常堆栈信息
     *
     * @param exception 异常
     * @param len       len
     * @return {@link String}
     */
    public static String exceptionToString(Exception exception, int len) {
        String data = exceptionToString(exception);
        if (data != null && data.length() > len) {
            data = data.substring(0, len);
        }
        return data;
    }

}
