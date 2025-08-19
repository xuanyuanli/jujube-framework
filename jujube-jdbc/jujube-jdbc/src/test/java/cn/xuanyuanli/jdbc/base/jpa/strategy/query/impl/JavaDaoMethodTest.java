package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.base.annotation.SelectField;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityClass;
import cn.xuanyuanli.core.lang.BaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaDaoMethodTest {

    @Nested
    @DisplayName("getEntityClass()")
    class GetEntityClass {

        @Test
        @DisplayName("获取实体类 - 成功")
        void getEntityClass() throws NoSuchMethodException {
            // Arrange
            Method method = SampleDao.class.getMethod("findById", Long.class);
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Act
            EntityClass entityClass = javaDaoMethod.getEntityClass();

            // Assert
            assertThat(entityClass).isNotNull();
            assertThat(entityClass.getName()).isEqualTo(SampleEntity.class.getName());
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetName {

        @Test
        @DisplayName("获取方法名 - 成功")
        void getName() throws NoSuchMethodException {
            // Arrange
            Method method = SampleDao.class.getMethod("findById", Long.class);
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Act
            String name = javaDaoMethod.getName();

            // Assert
            assertThat(name).isEqualTo("findById");
        }

    }

    @Nested
    @DisplayName("hasSelectFieldAnnotation()")
    class HasSelectFieldAnnotation {

        private static Stream<Arguments> provideMethodsForHasSelectFieldAnnotation() throws NoSuchMethodException {
            return Stream.of(Arguments.of(SampleDao.class.getMethod("findById", Long.class), false),
                    Arguments.of(SampleDao.class.getMethod("findByName", String.class), true));
        }

        @ParameterizedTest
        @MethodSource("provideMethodsForHasSelectFieldAnnotation")
        @DisplayName("判断是否存在@SelectField注解 - 成功")
        void hasSelectFieldAnnotation(Method method, boolean expected) {
            // Arrange
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Act
            boolean result = javaDaoMethod.hasSelectFieldAnnotation();

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getSelectFieldAnnotationValue()")
    class GetSelectFieldAnnotationValue {

        @Test
        @DisplayName("获取@SelectField注解的值 - 成功")
        void getSelectFieldAnnotationValue() throws NoSuchMethodException {
            // Arrange
            Method method = SampleDao.class.getMethod("findByName", String.class);
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Act
            String[] value = javaDaoMethod.getSelectFieldAnnotationValue();

            // Assert
            assertThat(value).containsExactly("id", "name");
        }

        @Test
        @DisplayName("获取@SelectField注解的值 - 注解不存在")
        void getSelectFieldAnnotationValue_AnnotationNotPresent() throws NoSuchMethodException {
            // Arrange
            Method method = SampleDao.class.getMethod("findById", Long.class);
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Mockito cannot mock/spy final methods.  It's a limitation of Mockito.
            // However, since getSelectFieldAnnotationValue() is a simple getter,
            // we expect an exception when the annotation is not present.
            // So this test is designed to verify this behavior.
            assertThrows(NullPointerException.class, javaDaoMethod::getSelectFieldAnnotationValue);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToString {

        @Test
        @DisplayName("转换为字符串 - 成功")
        void testToString() throws NoSuchMethodException {
            // Arrange
            Method method = SampleDao.class.getMethod("findById", Long.class);
            JavaDaoMethod javaDaoMethod = new JavaDaoMethod(method);

            // Act
            String result = javaDaoMethod.toString();

            // Assert
            assertThat(result).isEqualTo(method.toString());
        }
    }

    // 示例接口和实体类
    interface SampleDao extends BaseDao<SampleEntity, Long> {

        SampleEntity findById(Long id);

        @SelectField({"id", "name"})
        SampleEntity findByName(String name);
    }

    @Data
    static class SampleEntity implements BaseEntity {

        private Long id;
        private String name;
    }
}

