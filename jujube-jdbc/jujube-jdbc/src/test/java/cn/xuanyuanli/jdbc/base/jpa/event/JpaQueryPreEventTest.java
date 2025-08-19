package cn.xuanyuanli.jdbc.base.jpa.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("JpaQueryPreEvent 测试")
class JpaQueryPreEventTest {

    @Nested
    @DisplayName("构造函数 JpaQueryPreEvent(Method method, Object[] args)")
    class ConstructorTest {

        @ParameterizedTest
        @MethodSource("provideConstructorData")
        @DisplayName("构造函数测试")
        void testConstructor(Method method, Object[] args) {
            JpaQueryPreEvent event = new JpaQueryPreEvent(method, args);
            assertThat(event).isNotNull();

            JpaQueryPreEvent.JpaQueryPreEventSource source = (JpaQueryPreEvent.JpaQueryPreEventSource) event.getSource();
            assertThat(source).isNotNull();
            assertThat(source.getMethod()).isEqualTo(method);
            assertThat(source.getArgs()).isEqualTo(args);
        }

        private static Stream<Arguments> provideConstructorData() throws NoSuchMethodException {
            Method method1 = ConstructorTest.class.getDeclaredMethod("provideConstructorData");
            Object[] args1 = new Object[]{"arg1", 123};

            Method method2 = ConstructorTest.class.getDeclaredMethod("testConstructor", Method.class, Object[].class);
            Object[] args2 = new Object[]{}; // 空参数

            return Stream.of(
                    Arguments.of(method1, args1),
                    Arguments.of(method2, args2)
            );
        }
    }

    @Nested
    @DisplayName("JpaQueryPreEventSource 类")
    class JpaQueryPreEventSourceTest {

        @ParameterizedTest(name = "测试 JpaQueryPreEventSource: 方法={0}, 参数={1}")
        @MethodSource("provideSourceData")
        @DisplayName("JpaQueryPreEventSource 属性测试")
        void testJpaQueryPreEventSourceProperties(Method method, Object[] args) {
            JpaQueryPreEvent.JpaQueryPreEventSource source = new JpaQueryPreEvent.JpaQueryPreEventSource(method, args);
            assertThat(source).isNotNull();
            assertThat(source.getMethod()).isEqualTo(method);
            assertThat(source.getArgs()).isEqualTo(args);

            // 测试通过 setter 方法修改值
            Method newMethod = null;
            try {
                newMethod = String.class.getMethod("length");
            } catch (NoSuchMethodException ignored) {
            }
            Object[] newArgs = {1, 2, 3};
            source.setMethod(newMethod);
            source.setArgs(newArgs);
            assertThat(source.getMethod()).isEqualTo(newMethod);
            assertThat(source.getArgs()).isEqualTo(newArgs);

        }


        private static Stream<Arguments> provideSourceData() throws NoSuchMethodException {
            Method method1 = JpaQueryPreEventSourceTest.class.getDeclaredMethod("provideSourceData");
            Object[] args1 = new Object[]{"test", 456};

            Method method2 = JpaQueryPreEventSourceTest.class.getDeclaredMethod("testJpaQueryPreEventSourceProperties", Method.class, Object[].class);
            Object[] args2 = null;

            return Stream.of(
                    Arguments.of(method1, args1),
                    Arguments.of(method2, args2)
            );
        }

        @ParameterizedTest
        @MethodSource("provideEqualsAndHashCodeData")
        @DisplayName("equals() 和 hashCode() 方法测试")
        void testEqualsAndHashCode(JpaQueryPreEvent.JpaQueryPreEventSource source1, JpaQueryPreEvent.JpaQueryPreEventSource source2, boolean expected) {
            assertThat(source1.equals(source2)).isEqualTo(expected);
            if (expected) {
                assertThat(source1.hashCode()).isEqualTo(source2.hashCode());
            }
        }

        private static Stream<Arguments> provideEqualsAndHashCodeData() throws NoSuchMethodException {
            Method method1 = JpaQueryPreEventSourceTest.class.getDeclaredMethod("provideEqualsAndHashCodeData");
            Object[] args1 = new Object[]{"a", 1};
            Object[] args1Copy = Arrays.copyOf(args1, args1.length);

            Method method2 = JpaQueryPreEventSourceTest.class.getDeclaredMethod("testEqualsAndHashCode",
                    JpaQueryPreEvent.JpaQueryPreEventSource.class, JpaQueryPreEvent.JpaQueryPreEventSource.class, boolean.class);
            Object[] args2 = new Object[]{"b", 2};

            return Stream.of(
                    Arguments.of(new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1), new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1Copy), true),
                    Arguments.of(new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1), new JpaQueryPreEvent.JpaQueryPreEventSource(method2, args1), false),
                    Arguments.of(new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1), new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args2), false),
                    Arguments.of(new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1), new JpaQueryPreEvent.JpaQueryPreEventSource(method2, args2), false),
                    Arguments.of(new JpaQueryPreEvent.JpaQueryPreEventSource(method1, args1), null, false)
            );
        }

        @Test
        @DisplayName("toString()方法测试")
        void testToString() throws NoSuchMethodException {
            Method method = getClass().getDeclaredMethod("testToString");
            Object[] args = {1, "a"};
            JpaQueryPreEvent.JpaQueryPreEventSource source = new JpaQueryPreEvent.JpaQueryPreEventSource(method, args);
            String expected = "JpaQueryPreEvent.JpaQueryPreEventSource(method=" + method + ", args=" + Arrays.toString(args) + ")";
            assertThat(source.toString()).isEqualTo(expected);
        }
    }
}
