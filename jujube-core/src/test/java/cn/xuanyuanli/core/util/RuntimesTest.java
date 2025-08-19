package cn.xuanyuanli.core.util;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import cn.xuanyuanli.core.constant.SystemProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class RuntimesTest {

    @Test
    void execCommand() throws IOException {
        Process process = Runtimes.execCommand(new String[]{"java", "-version"});
        assertThat(process).isNotNull();
        process.destroy();
    }

    @Test
    void execCommandAndGetInput() {
        if (SystemProperties.WINDOWS) {
            assertThat(Runtimes.execCommandAndGetInput(new String[]{"cmd", "/c", "echo", "1"})).isEqualTo("1");
        } else if (SystemProperties.LINUX) {
            assertThat(Runtimes.execCommandAndGetInput(new String[]{"/bin/bash", "-c", "echo", "1"})).isEqualTo("1");
        }
    }

    @Test
    @Disabled
    void runBat() throws IOException {
        if (SystemProperties.WINDOWS) {
            Resource resource = Resources.getClassPathResources("META-INF/runtime/test.bat");
            Process process = null;
            if (resource != null) {
                process = Runtimes.runBat(resource.getFile().getParent(), "test.bat");
            }
            assertThat(process).isNotNull();
            process.destroy();
        }
    }

    @Test
    void getPid() {
        assertThat(Runtimes.getPid()).isGreaterThan(0);
    }

    @Test
    void getRuntimeJarName() {
        assertThat(Runtimes.getRuntimeJarName()).isNotNull();
    }

    @Test
    void sleep() {
        long begin = System.currentTimeMillis();
        Runtimes.sleep(100);
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(100);
    }

    @Test
    void getHostName() {
        assertThat(Runtimes.getRuntimeJarName()).isNotNull();
    }

    @Test
    void getParentStackTrace() {
        List<String> traceList = Runtimes.getParentStackTrace("cn.xuanyuanli", 2);
        assertThat(traceList).hasSize(1);
        String clName = RuntimesTest.class.getName();
        assertThat(traceList.get(0)).startsWith(clName + "#getParentStackTrace");
    }

    @Test
    void getParentStackTraceNoSkipAopProxyClass() {
        JavaProxyNoSkip javaProxy = new JavaProxyNoSkip();
        ProxyApi t = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(ProxyApi.class.getClassLoader(), new Class[]{ProxyApi.class}, javaProxy);
        t.say();
    }

    @Test
    void getParentStackTraceSkipAopProxyClass() {
        JavaProxySkip javaProxy = new JavaProxySkip();
        ProxyApi t = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(ProxyApi.class.getClassLoader(), new Class[]{ProxyApi.class}, javaProxy);
        t.say();
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
