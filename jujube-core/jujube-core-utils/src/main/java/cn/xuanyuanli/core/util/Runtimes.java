package cn.xuanyuanli.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.constant.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 运行时工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Runtimes {

    /**
     * &#064;符号
     */
    public static final char AT_SYMBOL = '@';
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Runtimes.class);

    /**
     * 执行命令
     *
     * @param command 命令
     * @return {@link Process}
     */
    public static Process execCommand(String[] command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return processBuilder.start();
    }

    /**
     * 执行命令并获得输出,编码默认为gbk。此执行将会阻塞线程，一直到子进程结束
     *
     * @param command 命令
     * @return {@link String}
     */
    public static String execCommandAndGetInput(String[] command) {
        return execCommandAndGetInput(command, Charsets.GBK.name());
    }

    /**
     * 执行命令并获得输出。此执行将会阻塞线程，一直到子进程结束
     *
     * @param command 命令
     * @param charset 字符集
     * @return {@link String}
     */
    public static String execCommandAndGetInput(String[] command, String charset) {
        String result;
        Process process = null;
        try {
            process = execCommand(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
                result = reader.lines().collect(Collectors.joining("\n"));
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    /**
     * 运行bat文件(仅限windows环境下使用)。此执行会打开新窗口，是没有阻塞的
     *
     * @param batPath 脚本所在目录
     * @param batName 脚本名称
     * @return {@link Process}
     */
    public static Process runBat(String batPath, String batName) {
        Validate.isTrue(SystemProperties.WINDOWS);
        Process ps = null;
        try {
            // 盘符
            String drive = batPath.split(":")[0] + ":";
            String[] command = new String[]{"cmd", "/c", drive, "&&", "cd", batPath, "&&", "cmd", "/c", "start", batName};
            ps = execCommand(command);
            ps.waitFor();
            // 接收执行完毕的返回值
            int i = ps.exitValue();
            if (i == 0) {
                logger.info("命令：{} 执行完成.", batName);
            } else {
                logger.info("命令：{} 执行失败.", batName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                ps.destroy();
            }
        }
        return ps;
    }

    /**
     * 获得java进程id
     *
     * @return java进程id
     */
    public static int getPid() {
        String pid = System.getProperty("pid");
        if (pid == null) {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            String processName = runtimeMxBean.getName();
            if (processName.indexOf(AT_SYMBOL) != -1) {
                pid = processName.substring(0, processName.indexOf('@'));
            }
        }
        return Texts.toInt(pid);
    }

    /**
     * 获得运行时的程序名称
     *
     * @return {@link String}
     */
    public static String getRuntimeJarName() {
        String input = execCommandAndGetInput(new String[]{"jps", "-l"}, "utf-8");
        if (StringUtils.isNotBlank(input)) {
            String[] arr = input.split("[\n\r]");
            String vmid = getPid() + " ";
            for (String ele : arr) {
                if (ele.startsWith(vmid)) {
                    return ele.split("\\s+")[1];
                }
            }
        }
        return null;
    }

    /**
     * 线程睡眠
     *
     * @param millis 米尔斯
     */
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("线程睡眠", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 运行zk启动脚步(仅限windows环境下使用)
     */
    public static void runZookeeperStartBat() {
        if (SystemProperties.WINDOWS) {
            String batPath = "";
            File projectPath = new File(Objects.requireNonNull(Resources.getProjectPath()));
            // 最多向上查5层
            for (int i = 0; i < 5; i++) {
                File[] files = projectPath.getParentFile().listFiles(File::isDirectory);
                Stream<File> fileStream = Arrays.stream(Objects.requireNonNull(files)).filter(f -> "3rd-lib".equals(f.getName()));
                Optional<File> first = fileStream.findFirst();
                if (first.isPresent()) {
                    batPath = new File(first.get(), "zookeeper-server").getAbsolutePath();
                    break;
                }
                projectPath = projectPath.getParentFile();
            }
            if (batPath.isEmpty()) {
                System.err.print("没有找到3rd-lib目录，请先git clone 3rd-lib项目");
                return;
            }
            String batName = "startZookeeperServer.cmd";
            runBat(batPath, batName);
        }
    }

    /**
     * 获得方法的父调用链
     *
     * @param basePackage 基本包
     * @param maxSize     最大大小
     * @return {@link List}<{@link String}>
     * @see #getParentStackTrace(String, int, boolean)
     */
    public static List<String> getParentStackTrace(String basePackage, int maxSize) {
        return getParentStackTrace(basePackage, maxSize, true);
    }

    /**
     * 获得方法的父调用链
     *
     * @param basePackage       基础包名
     * @param maxSize           最大条数
     * @param skipAopProxyClass 是否跳过代理类
     * @return 如果结果大于1，则不包含方法本身；否则包括
     */
    public static List<String> getParentStackTrace(String basePackage, int maxSize, boolean skipAopProxyClass) {
        // skip跳过Thread.currentThread() & 当前方法
        Stream<StackTraceElement> stream = Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).filter(e -> e.getClassName().startsWith(basePackage));
        if (skipAopProxyClass) {
            stream = stream.filter(e -> !e.getClassName().contains("$"));
        }
        // 把getParentStackTrace方法本身过滤掉
        stream = stream.filter(e -> !(e.getClassName().equals(Runtimes.class.getName()) && "getParentStackTrace".equals(e.getMethodName())));
        return stream.limit(maxSize).map(e -> e.getClassName() + "#" + e.getMethodName() + " :" + e.getLineNumber()).collect(Collectors.toList());
    }
}
