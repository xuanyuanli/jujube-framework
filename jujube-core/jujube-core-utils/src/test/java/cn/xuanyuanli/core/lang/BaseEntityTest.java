package cn.xuanyuanli.core.lang;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BaseEntity 测试")
public class BaseEntityTest {

    @Nested
    @DisplayName("转换为Record测试")
    class ToRecordTests {

        @Test
        @DisplayName("toRecord_应该返回空Record_当实体为空时")
        void toRecord_shouldReturnEmptyRecord_whenEntityIsEmpty() {
            // Arrange
            FromEntity entity = new FromEntity();

            // Act
            Record result = entity.toRecord();

            // Assert
            assertThat(result).isEqualTo(new Record());
        }

        @Test
        @DisplayName("toRecord_应该返回包含属性的Record_当实体有值时")
        void toRecord_shouldReturnRecordWithProperties_whenEntityHasValues() {
            // Arrange
            FromEntity entity = new FromEntity();
            entity.setName("a");
            entity.setAge(10);

            // Act
            Record result = entity.toRecord();

            // Assert
            assertThat(result).isEqualTo(new Record().set("name", "a").set("age", 10));
        }
    }

    @Nested
    @DisplayName("转换为Map测试")
    class ToMapTests {

        @Test
        @DisplayName("toMap_应该返回包含null值的Map_当实体为空时")
        void toMap_shouldReturnMapWithNullValues_whenEntityIsEmpty() {
            // Arrange
            FromEntity entity = new FromEntity();
            HashMap<Object, Object> expected = new HashMap<>();
            expected.put("name", null);
            expected.put("age", null);
            expected.put("bo", null);
            expected.put("list", null);

            // Act
            Map<String, Object> result = entity.toMap();

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("toMap_应该返回包含属性值的Map_当实体有值时")
        void toMap_shouldReturnMapWithPropertyValues_whenEntityHasValues() {
            // Arrange
            FromEntity entity = new FromEntity();
            entity.setName("a");
            entity.setAge(10);
            HashMap<Object, Object> expected = new HashMap<>();
            expected.put("name", "a");
            expected.put("age", 10);
            expected.put("bo", null);
            expected.put("list", null);

            // Act
            Map<String, Object> result = entity.toMap();

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("toMapFilterNull_应该返回过滤null值的Map_当实体有值时")
        void toMapFilterNull_shouldReturnMapFilteringNullValues_whenEntityHasValues() {
            // Arrange
            FromEntity entity = new FromEntity();
            entity.setName("a");
            entity.setAge(10);
            HashMap<Object, Object> expected = new HashMap<>();
            expected.put("name", "a");
            expected.put("age", 10);

            // Act
            Map<String, Object> result = entity.toMapFilterNull();

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("转换为BO测试")
    class ToBOTests {

        @Test
        @DisplayName("toBO_应该正确转换实体_当转换为指定BO类型时")
        void toBO_shouldCorrectlyConvertEntity_whenConvertingToSpecifiedBOType() {
            // Arrange
            DestEntity destEntity = new DestEntity();
            destEntity.setAge(12);
            destEntity.setName("abc");
            InnerBO bo = new InnerBO();
            bo.setName("efg");
            destEntity.setBo(bo);
            destEntity.setList(new ArrayList<>(List.of("7")));

            // Act
            FromBO fromBO = destEntity.toBO(FromBO.class);

            // Assert
            assertThat(fromBO.getAge()).isEqualTo(destEntity.getAge());
            assertThat(fromBO.getName()).isEqualTo(destEntity.getName());
            assertThat(fromBO.getBo()).isEqualTo(destEntity.getBo());
            assertThat(fromBO.getList()).isEqualTo(destEntity.getList());
        }
    }

    @Nested
    @DisplayName("克隆测试")
    class CloneTests {

        @Test
        @DisplayName("cloneSelf_应该创建深拷贝_当克隆实体时")
        void cloneSelf_shouldCreateDeepCopy_whenCloningEntity() {
            // Arrange
            DestEntity destEntity = new DestEntity();
            destEntity.setAge(12);
            destEntity.setName("abc");
            InnerBO bo = new InnerBO();
            bo.setName("efg");
            destEntity.setBo(bo);
            destEntity.setExt(true);

            // Act
            DestEntity target = destEntity.cloneSelf();

            // Assert
            assertThat(target.getAge()).isEqualTo(destEntity.getAge());
            assertThat(target.getName()).isEqualTo(destEntity.getName());
            assertThat(target.getBo()).isEqualTo(destEntity.getBo());
            assertThat(target.isExt()).isTrue();
        }

        @Test
        @DisplayName("cloneSelf_应该创建独立的集合副本_当克隆包含集合的实体时")
        void cloneSelf_shouldCreateIndependentCollectionCopy_whenCloningEntityWithCollections() {
            // Arrange
            DestEntity destEntity = new DestEntity();
            destEntity.setList(new ArrayList<>(Arrays.asList("7", "8")));

            // Act
            DestEntity target = destEntity.cloneSelf();
            target.getList().remove("8");

            // Assert
            assertThat(target.getList()).containsExactly("7");
            assertThat(destEntity.getList()).containsExactly("7", "8");
        }
    }

    @Data
    public static class InnerBO implements BaseEntity {

        private String name;
    }

    @Data
    public static class FromEntity implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
        private InnerBO bo;
    }

    @Data
    public static class FromBO implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
        private InnerBO bo;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString(callSuper = true)
    public static class DestEntity extends FromEntity {

        private Integer type;
        private boolean ext;
    }
}
