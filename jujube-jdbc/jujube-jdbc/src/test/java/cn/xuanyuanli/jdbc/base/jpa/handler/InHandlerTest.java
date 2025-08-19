package cn.xuanyuanli.jdbc.base.jpa.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@DisplayName("InHandler 测试")
class InHandlerTest {

    @Nested
    @DisplayName("handler 方法")
    class HandlerMethodTest {

        @ParameterizedTest
        @MethodSource("provideHandlerData")
        @DisplayName("handler 方法测试")
        void testHandler(String truncationMethodName, Object argValue, boolean expectIn, boolean expectChainCall) {
            InHandler inHandler = spy(new InHandler());
            DaoMethod mockDaoMethod = mock(DaoMethod.class);
            Spec mockSpec = mock(Spec.class);
            HandlerChain mockChain = mock(HandlerChain.class);

            List<Object> args = new ArrayList<>();
            args.add(argValue);

            // Mock getDbColumn 方法
            doAnswer(invocation -> invocation.getArgument(1)).when(inHandler).getDbColumn(any(), anyString());

            // 如果期望调用 spec.in() 方法，则设置 mock 行为
            if (expectIn) {
                Mockito.doReturn(mockSpec).when(mockSpec).in(anyString(), any());
            }

            inHandler.handler(mockDaoMethod, mockSpec, truncationMethodName, args, mockChain);

            // 验证 spec.in() 方法是否被调用
            if (expectIn) {
                verify(mockSpec).in(anyString(), any());
                assertThat(args).isEmpty();
            } else {
                verify(mockSpec, never()).in(anyString(), any());
            }

            // 验证 chain.handler() 方法是否被调用
            if (expectChainCall) {
                verify(mockChain).handler(mockDaoMethod, mockSpec, truncationMethodName, args);
            } else {
                verify(mockChain, never()).handler(any(), any(), any(), anyList());
            }
        }

        private static Stream<Arguments> provideHandlerData() {
            return Stream.of(
                    // 正常情况，以 "In" 结尾，参数为 List
                    Arguments.of("fieldIn", Arrays.asList("value1", "value2"), true, false),
                    // 正常情况，以 "In" 结尾，参数为数组
                    Arguments.of("fieldIn", new String[]{"value1", "value2"}, true, false),
                    // 正常情况，以 "In" 结尾，参数为 Iterable
                    Arguments.of("fieldIn", Collections.singleton("value1"), true, false),
                    // 正常情况，以 "In" 结尾，参数为 null
                    Arguments.of("fieldIn", null, true, false),
                    // 不以 "In" 结尾
                    Arguments.of("field", Arrays.asList("value1", "value2"), false, true),
                    // 以 "In" 结尾，但参数不是 List 或数组
                    Arguments.of("fieldIn", "value1", true, false),
                    // 以"In"结尾，参数是空数组
                    Arguments.of("fieldIn", new Object[]{}, true, false)
            );
        }
    }

    @Nested
    @DisplayName("toIterable 方法")
    class ToIterableMethodTest {

        @ParameterizedTest
        @MethodSource("provideToIterableData")
        @DisplayName("toIterable 方法测试")
        void testToIterable(Object input, List<Object> expected) {
            InHandler inHandler = new InHandler();
            Iterable<Object> result = (Iterable<Object>) inHandler.toIterable(input);
            assertThat(result).isNotNull().containsExactlyElementsOf(expected);
        }

        private static Stream<Arguments> provideToIterableData() {
            List<Object> list1 = Arrays.asList("a", "b", "c");
            Object[] array1 = new Object[]{"x", "y", "z"};
            List<Object> listExpected = Arrays.asList("x", "y", "z");

            return Stream.of(
                    Arguments.of(list1, list1), // 已经是 Iterable
                    Arguments.of(array1, listExpected), // 数组
                    Arguments.of("singleValue", Collections.emptyList()), // 单个值
                    Arguments.of(null, Collections.emptyList()) // null 值
            );
        }
    }

    @Test
    @DisplayName("测试IN常量")
    void testInConstant() {
        assertThat(InHandler.IN).isEqualTo("In");
    }
}
