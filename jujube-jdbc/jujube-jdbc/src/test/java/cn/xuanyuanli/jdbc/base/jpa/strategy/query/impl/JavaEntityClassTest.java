package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaEntityClassTest {

    @Nested
    @DisplayName("getDeclaredFields()")
    class GetDeclaredFields {

        @Test
        @DisplayName("获取所有声明的字段 - 成功")
        void getDeclaredFields() {
            // Arrange
            JavaEntityClass javaEntityClass = new JavaEntityClass(SampleEntity.class);

            // Act
            EntityField[] declaredFields = javaEntityClass.getDeclaredFields();

            // Assert
            assertThat(declaredFields).hasSize(2);
            assertThat(declaredFields[0].getName()).isEqualTo("id");
            assertThat(declaredFields[1].getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetName {

        private static Stream<Arguments> provideClassAndName() {
            return Stream.of(
                    Arguments.of(SampleEntity.class, SampleEntity.class.getName()),
                    Arguments.of(String.class, String.class.getName())
            );
        }

        @ParameterizedTest
        @MethodSource("provideClassAndName")
        @DisplayName("获取类名 - 成功")
        void getName(Class<?> clazz, String expectedName) {
            // Arrange
            JavaEntityClass javaEntityClass = new JavaEntityClass(clazz);

            // Act
            String name = javaEntityClass.getName();

            // Assert
            assertThat(name).isEqualTo(expectedName);
        }
    }


    @Nested
    @DisplayName("toString()")
    class ToStringTest {
        private static Stream<Arguments> provideClassAndString() {
            return Stream.of(
                    Arguments.of(SampleEntity.class, SampleEntity.class.toString()),
                    Arguments.of(String.class, String.class.toString())
            );
        }
        @ParameterizedTest
        @MethodSource("provideClassAndString")
        @DisplayName("转换为字符串 - 成功")
        void testToString(Class<?> clazz, String expectedString) {
            // Arrange
            JavaEntityClass javaEntityClass = new JavaEntityClass(clazz);

            // Act
            String result = javaEntityClass.toString();

            // Assert
            assertThat(result).isEqualTo(expectedString);
        }
    }

    @Data
    static class SampleEntity {
        private Long id;
        private String name;
    }
}
