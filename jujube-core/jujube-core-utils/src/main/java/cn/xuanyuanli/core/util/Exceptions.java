package cn.xuanyuanli.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 异常处理工具类
 * <p>
 * 提供异常处理的常用功能，包括异常转换、堆栈信息提取和异常包装等。
 * 主要用于简化异常处理逻辑，特别是在需要统一异常格式或提取异常信息的场景中。
 * </p>
 * 
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>异常转换：</strong>将检查异常转换为运行时异常，简化异常处理</li>
 * <li><strong>堆栈提取：</strong>将异常堆栈信息转换为字符串，便于日志记录和调试</li>
 * <li><strong>信息截取：</strong>支持限制堆栈信息长度，避免日志过长</li>
 * <li><strong>空值处理：</strong>对空异常进行安全处理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 将检查异常转换为运行时异常
 * try {
 *     // 可能抛出 IOException 的代码
 *     Files.readAllLines(Paths.get("file.txt"));
 * } catch (IOException e) {
 *     Exceptions.throwException(e);  // 转换为 RuntimeException
 * }
 * 
 * // 提取完整的异常堆栈信息
 * try {
 *     riskyOperation();
 * } catch (Exception e) {
 *     String stackTrace = Exceptions.exceptionToString(e);
 *     logger.error("操作失败: {}", stackTrace);
 * }
 * 
 * // 提取限长的异常信息（适用于存储到数据库）
 * try {
 *     someOperation();
 * } catch (Exception e) {
 *     String shortTrace = Exceptions.exceptionToString(e, 500);
 *     // 只保留前 500 个字符，适合数据库字段限制
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>应用场景：</strong>
 * <ul>
 * <li><strong>日志记录：</strong>将异常信息格式化后写入日志文件</li>
 * <li><strong>错误报告：</strong>提取异常信息用于错误报告和用户反馈</li>
 * <li><strong>异常转换：</strong>在框架层将检查异常转换为运行时异常</li>
 * <li><strong>调试支持：</strong>在开发过程中快速查看完整的异常信息</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>设计理念：</strong>
 * <ul>
 * <li><strong>简化处理：</strong>减少异常处理的样板代码</li>
 * <li><strong>信息完整：</strong>保留完整的异常上下文信息</li>
 * <li><strong>安全可靠：</strong>对边界情况进行安全处理</li>
 * <li><strong>性能友好：</strong>避免不必要的字符串操作和内存分配</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>throwException 方法会将原始异常包装在 RuntimeException 中</li>
 * <li>异常堆栈信息可能很长，在生产环境中建议使用限长版本</li>
 * <li>对于 null 异常会返回固定的字符串 "null"</li>
 * <li>堆栈信息提取会创建临时的 StringWriter 对象</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exceptions {

    /**
     * 将任意异常转换为运行时异常并抛出
     * <p>
     * 这是一个异常转换工具方法，主要用于将检查异常（Checked Exception）转换为
     * 运行时异常（Runtime Exception），从而简化异常处理逻辑。特别适用于函数式编程
     * 和 Lambda 表达式中无法直接抛出检查异常的场景。
     * </p>
     * 
     * <p>
     * <strong>转换逻辑：</strong>
     * <ul>
     * <li>如果传入的异常为 null，则抛出 NullPointerException</li>
     * <li>否则将原始异常包装在 RuntimeException 中并抛出</li>
     * <li>保留原始异常的完整信息和调用栈</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 在 Lambda 表达式中处理检查异常
     * List<String> lines = files.stream()
     *     .map(file -> {
     *         try {
     *             return Files.readAllLines(file.toPath());
     *         } catch (IOException e) {
     *             Exceptions.throwException(e);
     *             return null; // 永远不会执行
     *         }
     *     })
     *     .collect(toList());
     * 
     * // 在工具方法中简化异常处理
     * public void processFile(String fileName) {
     *     try {
     *         // 可能抛出多种检查异常的操作
     *         complexFileOperation(fileName);
     *     } catch (Exception e) {
     *         Exceptions.throwException(e); // 统一转换为运行时异常
     *     }
     * }
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li><strong>Lambda 表达式：</strong>在不支持检查异常的函数接口中使用</li>
     * <li><strong>框架封装：</strong>在框架层统一异常处理策略</li>
     * <li><strong>简化代码：</strong>减少异常处理的样板代码</li>
     * <li><strong>异常链：</strong>保持异常的因果关系链</li>
     * </ul>
     * </p>
     *
     * @param e 要转换的原始异常，可以是任意类型的 Throwable
     * @throws NullPointerException 如果传入的异常为 null
     * @throws RuntimeException 包装原始异常的运行时异常
     */
    public static void throwException(Throwable e) {
        if (e == null) {
            throw new NullPointerException();
        }
        throw new RuntimeException(e);
    }

    /**
     * 获得异常的完整堆栈信息字符串
     * <p>
     * 将异常的堆栈跟踪信息转换为字符串格式，包含异常类型、异常消息和完整的调用栈信息。
     * 这对于日志记录、错误报告和调试非常有用，特别是在需要将异常信息持久化或传输的场景中。
     * </p>
     * 
     * <p>
     * <strong>输出格式：</strong>
     * 返回的字符串包含以下信息：
     * <ul>
     * <li>异常类的完全限定名</li>
     * <li>异常的详细消息（如果有）</li>
     * <li>完整的调用栈跟踪，包括类名、方法名、文件名和行号</li>
     * <li>如果有异常链（caused by），会递归显示所有关联异常</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 在日志中记录异常详情
     * try {
     *     processData();
     * } catch (Exception e) {
     *     String stackTrace = Exceptions.exceptionToString(e);
     *     logger.error("数据处理失败:\n{}", stackTrace);
     * }
     * 
     * // 发送错误报告
     * try {
     *     criticalOperation();
     * } catch (Exception e) {
     *     String errorDetail = Exceptions.exceptionToString(e);
     *     errorReportService.sendReport("系统错误", errorDetail);
     * }
     * 
     * // 在开发环境中调试
     * Exception debugException = new SQLException("Connection failed");
     * String debugInfo = Exceptions.exceptionToString(debugException);
     * System.out.println(debugInfo); // 查看完整堆栈信息
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>性能考虑：</strong>
     * <ul>
     * <li>方法会创建 StringWriter 和 PrintWriter 对象</li>
     * <li>堆栈信息可能很长，特别是对于深层调用栈</li>
     * <li>建议在生产环境中谨慎使用，考虑性能影响</li>
     * <li>对于高频异常，考虑使用 {@link #exceptionToString(Exception, int)} 限制长度</li>
     * </ul>
     * </p>
     *
     * @param exception 要转换的异常对象，允许为 null
     * @return 异常的堆栈跟踪字符串；如果异常为 null 则返回 "null"
     * @see #exceptionToString(Exception, int)
     */
    public static String exceptionToString(Exception exception) {
        if (exception == null) {
            return "null";
        }
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * 获得限制长度的异常堆栈信息字符串
     * <p>
     * 这是 {@link #exceptionToString(Exception)} 的限长版本，用于控制异常信息的输出长度。
     * 特别适用于数据库存储、网络传输或UI显示等对字符串长度有限制的场景。
     * </p>
     * 
     * <p>
     * <strong>截取策略：</strong>
     * <ul>
     * <li>如果指定长度 <= 0，直接返回空字符串</li>
     * <li>首先获取完整的异常堆栈信息</li>
     * <li>如果完整信息长度超过指定长度，则截取前 N 个字符</li>
     * <li>截取操作基于字符数，不考虑行的完整性</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 存储到数据库的错误日志表（字段长度限制 500 字符）
     * try {
     *     businessOperation();
     * } catch (Exception e) {
     *     String errorMsg = Exceptions.exceptionToString(e, 500);
     *     errorLogRepository.save(new ErrorLog(errorMsg));
     * }
     * 
     * // Web API 返回错误信息（避免响应过大）
     * try {
     *     processRequest();
     * } catch (Exception e) {
     *     String briefError = Exceptions.exceptionToString(e, 200);
     *     return ResponseEntity.badRequest().body(briefError);
     * }
     * 
     * // 移动端显示错误信息（屏幕空间有限）
     * try {
     *     mobileOperation();
     * } catch (Exception e) {
     *     String shortMsg = Exceptions.exceptionToString(e, 100);
     *     showToast(shortMsg);
     * }
     * }</pre>
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li><strong>数据库存储：</strong>适配数据库字段长度限制</li>
     * <li><strong>日志文件：</strong>避免单条日志过长影响性能</li>
     * <li><strong>用户界面：</strong>在有限的显示空间内展示错误信息</li>
     * <li><strong>网络传输：</strong>减少异常信息的传输开销</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>注意事项：</strong>
     * <ul>
     * <li>截取可能导致异常信息不完整，影响问题排查</li>
     * <li>建议保留足够的长度以包含异常类型和关键信息</li>
     * <li>对于重要的生产环境异常，建议同时保存完整版本</li>
     * <li>截取操作可能切断多字节字符，但通常不影响可读性</li>
     * </ul>
     * </p>
     *
     * @param exception 要转换的异常对象，允许为 null
     * @param len       限制的最大字符数，必须大于 0 才有效
     * @return 限制长度的异常堆栈跟踪字符串；如果 len <= 0 则返回空字符串
     * @see #exceptionToString(Exception)
     */
    public static String exceptionToString(Exception exception, int len) {
        if (len <= 0) {
            return "";
        }
        String data = exceptionToString(exception);
        if (data != null && data.length() > len) {
            data = data.substring(0, len);
        }
        return data;
    }

}
