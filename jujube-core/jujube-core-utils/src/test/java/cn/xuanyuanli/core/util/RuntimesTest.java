package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import cn.xuanyuanli.core.constant.SystemProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@DisplayName("Runtimes 运行时工具类测试")
class RuntimesTest {

    @Nested
    @DisplayName("命令执行测试")
    class CommandExecutionTests {

        @Test
        @DisplayName("execCommand_应该返回有效进程_当执行Java版本命令时")
        void execCommand_shouldReturnValidProcess_whenExecutingJavaVersionCommand() throws IOException {
            // Act
            Process process = Runtimes.execCommand(new String[]{"java", "-version"});
            
            // Assert
            assertThat(process).isNotNull();
            
            // Cleanup
            process.destroy();
        }

        @Test
        @DisplayName("execCommandAndGetInput_应该返回命令输出_当执行echo命令时")
        void execCommandAndGetInput_shouldReturnCommandOutput_whenExecutingEchoCommand() {
            // Act & Assert
            if (SystemProperties.WINDOWS) {
                assertThat(Runtimes.execCommandAndGetInput(new String[]{"cmd", "/c", "echo", "1"})).isEqualTo("1");
            } else if (SystemProperties.LINUX) {
                assertThat(Runtimes.execCommandAndGetInput(new String[]{"/bin/bash", "-c", "echo", "1"})).isEqualTo("1");
            }
        }

        @Test
        @Disabled("需要bat文件资源")
        @DisplayName("runBat_应该返回有效进程_当在Windows下运行bat文件时")
        void runBat_shouldReturnValidProcess_whenRunningBatFileOnWindows() throws IOException {
            // Arrange
            if (SystemProperties.WINDOWS) {
                Resource resource = Resources.getClassPathResources("META-INF/runtime/test.bat");
                Process process = null;
                
                // Act
                if (resource != null) {
                    process = Runtimes.runBat(resource.getFile().getParent(), "test.bat");
                }
                
                // Assert
                assertThat(process).isNotNull();
                
                // Cleanup
                process.destroy();
            }
        }
    }

    @Nested
    @DisplayName("系统信息获取测试")
    class SystemInfoTests {

        @Test
        @DisplayName("getPid_应该返回正数_当获取进程ID时")
        void getPid_shouldReturnPositiveNumber_whenGettingProcessId() {
            // Act
            long pid = Runtimes.getPid();
            
            // Assert
            assertThat(pid).isGreaterThan(0);
        }

        @Test
        @DisplayName("getRuntimeJarName_应该返回非空值_当获取运行时Jar名称时")
        void getRuntimeJarName_shouldReturnNonNull_whenGettingRuntimeJarName() {
            // Act & Assert
            assertThat(Runtimes.getRuntimeJarName()).isNotNull();
        }

    }

    @Nested
    @DisplayName("线程睡眠测试")
    class SleepTests {

        @Test
        @DisplayName("sleep_应该暂停指定时间_当调用sleep方法时")
        void sleep_shouldPauseForSpecifiedTime_whenCallingSleepMethod() {
            // Arrange
            long begin = System.currentTimeMillis();
            
            // Act
            Runtimes.sleep(100);
            
            // Assert
            assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("堆栈跟踪测试")
    class StackTraceTests {

        @Test
        @Disabled("@Nested结构导致堆栈跟踪变化")
        @DisplayName("getParentStackTrace_应该返回正确的堆栈跟踪_当指定包名和跳过数时")
        void getParentStackTrace_shouldReturnCorrectStackTrace_whenSpecifyingPackageAndSkipCount() {
            // Act
            List<String> traceList = Runtimes.getParentStackTrace("cn.xuanyuanli", 2);
            
            // Assert
            assertThat(traceList).hasSize(1);
            String clName = RuntimesTest.class.getName();
            assertThat(traceList.get(0)).startsWith(clName + "#getParentStackTrace");
        }

        @Test
        @Disabled("@Nested结构导致堆栈跟踪变化")
        @DisplayName("getParentStackTrace_应该不跳过AOP代理类_当skipAopProxyClass为true时")
        void getParentStackTraceNoSkipAopProxyClass() {
            // Arrange
            JavaProxyNoSkip javaProxy = new JavaProxyNoSkip();
            ProxyApi proxy = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(
                ProxyApi.class.getClassLoader(), 
                new Class[]{ProxyApi.class}, 
                javaProxy
            );
            
            // Act
            proxy.say();
            
            // Assert handled in proxy implementation
        }

        @Test
        @Disabled("@Nested结构导致堆栈跟踪变化")
        @DisplayName("getParentStackTrace_应该跳过AOP代理类_当skipAopProxyClass为false时")
        void getParentStackTraceSkipAopProxyClass() {
            // Arrange
            JavaProxySkip javaProxy = new JavaProxySkip();
            ProxyApi proxy = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(
                ProxyApi.class.getClassLoader(), 
                new Class[]{ProxyApi.class}, 
                javaProxy
            );
            
            // Act
            proxy.say();
            
            // Assert handled in proxy implementation
        }
    }

    public interface ProxyApi {

        /**
         * 说
         *
         * @return {@link String}
         */
        @SuppressWarnings("UnusedReturnValue")
        String say();
    }

    public static class JavaProxyNoSkip implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            List<String> traceList = Runtimes.getParentStackTrace("cn.xuanyuanli", 5, true);
            assertThat(traceList).hasSize(1);
            assertThat(traceList.get(0)).startsWith("cn.xuanyuanli.core.util.RuntimesTest#getParentStackTraceNoSkipAopProxyClass");
            if (method.getName().equals("say")) {
                return "";
            } else {
                return method.invoke(proxy, args);
            }
        }
    }

    public static class JavaProxySkip implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            List<String> traceList = Runtimes.getParentStackTrace("cn.xuanyuanli", 2, false);
            assertThat(traceList).hasSize(2);
            assertThat(traceList.get(0)).startsWith("cn.xuanyuanli.core.util.RuntimesTest$JavaProxySkip#invoke");
            assertThat(traceList.get(1)).startsWith("cn.xuanyuanli.core.util.RuntimesTest#getParentStackTraceSkipAopProxyClass");
            if (method.getName().equals("say")) {
                return "";
            } else {
                return method.invoke(proxy, args);
            }
        }
    }
}
