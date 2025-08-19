package cn.xuanyuanli.jdbc.base.jpa.handler;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 设置 Mockito 的严格级别为 LENIENT
@DisplayName("IsEmptyHandler 单元测试")
class IsEmptyHandlerTest {

    @Spy
    @InjectMocks
    IsEmptyHandler isEmptyHandler;

    @Mock
    HandlerChain chain;

    @Nested
    @DisplayName("handler 方法测试")
    class HandlerMethodTest {

        static Stream<Arguments> provideHandlerTestData() {
            return Stream.of(
                    Arguments.of("nameIsEmpty", "name", true),
                    Arguments.of("ageIsEmpty", "age", true),
                    Arguments.of("otherMethod", "otherMethod", false)
            );
        }

        @ParameterizedTest(name = "测试方法名：{0}")
        @MethodSource("provideHandlerTestData")
        @DisplayName("处理不同方法名")
        void handler(String truncationMethodName, String expectedField, boolean shouldCallSpecIsEmpty) {
            // Arrange
            DaoMethod method = mock(DaoMethod.class);
            Spec spec = mock(Spec.class);
            List<Object> args = new ArrayList<>();

            // 使用 Spy 模拟父类方法
            doReturn(expectedField).when(isEmptyHandler).getDbColumn(method, expectedField);


            // Act
            isEmptyHandler.handler(method, spec, truncationMethodName, args, chain);

            // Assert
            if (shouldCallSpecIsEmpty) {
                verify(spec).isEmpty(expectedField);
                verify(chain, never()).handler(any(), any(), any(), any());
            } else {
                verify(spec, never()).isEmpty(any());
                verify(chain).handler(method, spec, truncationMethodName, args);
            }
        }
    }
}
