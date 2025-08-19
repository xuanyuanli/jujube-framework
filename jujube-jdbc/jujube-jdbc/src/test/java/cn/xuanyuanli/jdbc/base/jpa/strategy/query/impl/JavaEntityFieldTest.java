package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.stream.Stream;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.annotation.Column;
import cn.xuanyuanli.jdbc.base.annotation.VisualColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaEntityFieldTest {

    @Nested
    @DisplayName("hasColumnAnnotation()")
    class HasColumnAnnotation {

        private static Stream<Arguments> provideFieldsForHasColumnAnnotation() throws NoSuchFieldException {
            return Stream.of(
                    Arguments.of(SampleEntity.class.getDeclaredField("id"), true),
                    Arguments.of(SampleEntity.class.getDeclaredField("name"), false),
                    Arguments.of(SampleEntity.class.getDeclaredField("age"), false)
            );
        }

        @ParameterizedTest
        @MethodSource("provideFieldsForHasColumnAnnotation")
        @DisplayName("判断是否存在@Column注解 - 成功")
        void hasColumnAnnotation(Field field, boolean expected) {
            // Arrange
            JavaEntityField javaEntityField = new JavaEntityField(field);

            // Act
            boolean result = javaEntityField.hasColumnAnnotation();

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetName {

        private static Stream<Arguments> provideFieldsAndNames() throws NoSuchFieldException {
            return Stream.of(
                    Arguments.of(SampleEntity.class.getDeclaredField("id"), "id"),
                    Arguments.of(SampleEntity.class.getDeclaredField("name"), "name")
            );
        }

        @ParameterizedTest
        @MethodSource("provideFieldsAndNames")
        @DisplayName("获取字段名 - 成功")
        void getName(Field field, String expectedName) {
            // Arrange
            JavaEntityField javaEntityField = new JavaEntityField(field);

            // Act
            String name = javaEntityField.getName();

            // Assert
            assertThat(name).isEqualTo(expectedName);
        }
    }

    @Nested
    @DisplayName("getColumnAnnotationValue()")
    class GetColumnAnnotationValue {

        @Test
        @DisplayName("获取@Column注解的值 - 成功")
        void getColumnAnnotationValue() throws NoSuchFieldException {
            // Arrange
            Field field = SampleEntity.class.getDeclaredField("id");
            JavaEntityField javaEntityField = new JavaEntityField(field);

            // Act
            String value = javaEntityField.getColumnAnnotationValue();

            // Assert
            assertThat(value).isEqualTo("ID");
        }

        @Test
        @DisplayName("获取@Column注解的值 - 注解不存在")
        void getColumnAnnotationValue_AnnotationNotPresent() throws NoSuchFieldException {
            // Arrange
            Field field = SampleEntity.class.getDeclaredField("name");
            JavaEntityField javaEntityField = new JavaEntityField(field);

            // Assert
            assertThrows(NullPointerException.class, javaEntityField::getColumnAnnotationValue);
        }
    }

    @Nested
    @DisplayName("hasVisualColumnAnnotation()")
    class HasVisualColumnAnnotation {
        private static Stream<Arguments> provideFieldsForVisualColumnAnnotation() throws NoSuchFieldException {
            return Stream.of(
                    Arguments.of(SampleEntity.class.getDeclaredField("age"), true),
                    Arguments.of(SampleEntity.class.getDeclaredField("name"), false),
                    Arguments.of(SampleEntity.class.getDeclaredField("id"), false)
            );
        }

        @ParameterizedTest
        @MethodSource("provideFieldsForVisualColumnAnnotation")
        @DisplayName("判断是否存在@VisualColumn注解 - 成功")
        void testHasVisualColumnAnnotation(Field field, boolean expected) {
            // Arrange
            JavaEntityField entityField = new JavaEntityField(field);

            // Act
            boolean result = entityField.hasVisualColumnAnnotation();

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToString {
        @Test
        @DisplayName("转换为字符串")
        void testToString() throws NoSuchFieldException {
            // Arrange
            Field field = SampleEntity.class.getDeclaredField("id");

            JavaEntityField javaEntityField = new JavaEntityField(field);

            // Act
            String result = javaEntityField.toString();

            // Assert
            assertThat(result).isEqualTo(field.toString());

        }
    }

    // 示例实体类
    @Data
    static class SampleEntity {
        @Column("ID")
        private Long id;
        private String name;

        @VisualColumn
        private int age;
    }
}
