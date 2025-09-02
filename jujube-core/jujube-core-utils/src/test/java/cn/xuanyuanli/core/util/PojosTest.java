package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;
import java.util.stream.Stream;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.Pojos.FieldMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Pojos 对象映射工具测试")
class PojosTest {

    @Nested
    @DisplayName("对象映射测试")
    class MappingTests {

        @Test
        @DisplayName("mapping_应该完成Map到Bean的字段映射_当使用FieldMapping时")
        void mapping_shouldMapMapToBean_whenUsingFieldMapping() {
            // Arrange
            Map<String, Object> map = new HashMap<>();
            map.put("id", "123");
            map.put("name", "abc");
            map.put("age", "12");
            map.put("btype", "1");
            map.put("ltype", "2");

            // Act
            ChildUser result = Pojos.mapping(map, ChildUser.class, 
                new FieldMapping().field("btype", "blogType").field("ltype", "log_type"));

            // Assert
            assertThat(result.getBlogType()).isEqualTo(map.get("btype"));
            assertThat(result.getLog_type()).isEqualTo(map.get("ltype"));
            assertThat(result.getAge()).isEqualTo(12);
        }

        @Test
        @DisplayName("mapping_应该完成Record到Bean的基本类型映射_当字段名匹配时")
        void mapping_shouldMapRecordToBean_whenFieldNamesMatch() {
            // Arrange
            Record record = new Record().set("a_pass_password", "123").set("a_pass", 0).set("nameType", 1);

            // Act
            Material result = Pojos.mapping(record, Material.class);

            // Assert
            assertThat(result.getAPass()).isEqualTo(0);
            assertThat(result.getNameType()).isEqualTo(1);
            assertThat(result.getAPassPassword()).isEqualTo("123");
        }

        @Test
        @DisplayName("mapping_应该支持下划线结尾的字段名映射_当字段名有下划线后缀时")
        void mapping_shouldMapUnderscoreFields_whenFieldHasUnderscoreSuffix() {
            // Arrange
            Record record = new Record().set("a_pass_password_", "123").set("a_pass_", 0);

            // Act
            Material result = Pojos.mapping(record, Material.class);

            // Assert
            assertThat(result.getAPass()).isEqualTo(0);
            assertThat(result.getAPassPassword()).isEqualTo("123");
        }

        @Test
        @DisplayName("mapping_应该返回null_当源对象为Map但目标字段类型不匹配时")
        void mapping_shouldReturnNull_whenSourceIsMapButTargetFieldTypeMismatch() {
            // Arrange
            Record sex = new Record();
            sex.set("name", "1");
            Record record = new Record().set("sex", sex);

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            assertThat(result.getSex()).isNull();
        }

        @Test
        @DisplayName("mapping_应该正确复制Bean对象_当类型完全匹配时")
        void mapping_shouldCopyBean_whenTypesExactlyMatch() {
            // Arrange
            Sex sex = new Sex().setName("1");
            Record record = new Record().set("sex", sex);

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            assertThat(result.getSex()).isEqualTo(sex);
        }

        @Test
        @DisplayName("mapping_应该返回null_当Bean类型不兼容时")
        void mapping_shouldReturnNull_whenBeanTypesIncompatible() {
            // Arrange
            Address address = new Address().setAddress("b");
            Record record = new Record().set("sex", address);

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            assertThat(result.getSex()).isNull();
        }

        @Test
        @DisplayName("mapping_应该正确复制List集合_当泛型类型匹配时")
        void mapping_shouldCopyList_whenGenericTypesMatch() {
            // Arrange
            List<Address> addresses = new ArrayList<>();
            addresses.add(new Address().setAddress("bj"));
            Record record = new Record().set("addresses", addresses);

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            assertThat(result.getAddresses()).isEqualTo(addresses);
        }

        @Test
        @DisplayName("mapping_应该复制不同类型的List_由于泛型消除问题")
        void mapping_shouldCopyDifferentTypeList_dueToTypeErasure() {
            // Arrange
            Sex sex = new Sex().setName("1");
            List<Sex> addresses = new ArrayList<>();
            addresses.add(sex);
            Record record = new Record().set("addresses", addresses);

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            // 由于泛型消除问题，这里实际上会复制不同类型的列表
            assertThat(result.getAddresses()).hasSize(1);
            // 由于类型不同，这里只检查list的内容是否被复制
            assertThat(result.getAddresses().toString()).contains(sex.getName());
        }

        @Test
        @DisplayName("mapping_应该使用字段映射转换Record_当提供FieldMapping时")
        void mapping_shouldMapRecordWithFieldMapping_whenFieldMappingProvided() {
            // Arrange
            Record record = new Record().set("id", 1).set("pid", 256);

            // Act
            User result = Pojos.mapping(record, User.class, new FieldMapping().field("pid", "id"));

            // Assert
            assertThat(result.getId()).isEqualTo(256);
        }

        @Test
        @DisplayName("mapping_应该对子类字段进行映射_当使用FieldMapping时")
        void mapping_shouldMapChildClassFields_whenUsingFieldMapping() {
            // Arrange
            Record record = new Record().set("blog_type", 1);

            // Act
            ChildUser result = Pojos.mapping(record, ChildUser.class, new FieldMapping().field("blog_type", "blogType"));

            // Assert
            assertThat(result.getBlogType()).isEqualTo("1");
        }

        @ParameterizedTest(name = "mapping_应该返回null_当参数为null时: {0}")
        @MethodSource("provideNullParameters")
        @DisplayName("mapping_应该返回null_当参数为null时")
        void mapping_shouldReturnNull_whenParametersAreNull(@SuppressWarnings("unused") String description, Object sourceObj, Class<?> destClass) {
            // Act & Assert
            assertThat(Pojos.mapping(sourceObj, destClass)).isNull();
        }

        private static Stream<Arguments> provideNullParameters() {
            return Stream.of(
                Arguments.of("源对象为null", null, User.class),
                Arguments.of("目标类为null", new Record(), null)
            );
        }

        @Test
        @DisplayName("mapping_应该抛出IllegalArgumentException_当目标类为Map时")
        void mapping_shouldThrowIllegalArgumentException_whenDestClassIsMap() {
            // Arrange
            User user = new User();

            // Act & Assert
            assertThatThrownBy(() -> Pojos.mapping(user, Record.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("destClass不能为Map");
        }

        @Test
        @DisplayName("mapping_应该使用默认值_当Record中数据类型错误时")
        void mapping_shouldUseDefaultValues_whenRecordDataTypesAreWrong() {
            // Arrange
            Record record = new Record().set("id", "1,2").set("age", "abc");

            // Act
            User result = Pojos.mapping(record, User.class);

            // Assert
            assertThat(result.getId()).isEqualTo(0L);
            assertThat(result.getAge()).isEqualTo(0);
        }

        @Test
        @DisplayName("mapping_应该完成包装类型到基本类型的转换_当类型兼容时")
        void mapping_shouldConvertWrapperToPrimitive_whenTypesCompatible() {
            // Arrange
            Record record = new Record().set("id", "1").set("fieldBool", true);

            // Act
            UserPrimitive result = Pojos.mapping(record, UserPrimitive.class);

            // Assert
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getAge()).isEqualTo(0);
            assertThat(result.getFieldByte()).isEqualTo(Byte.valueOf("0"));
            assertThat(result.getFieldDouble()).isEqualTo(0.0D);
            assertThat(result.getFieldFloat()).isEqualTo(0.0F);
            assertThat(result.getFieldChar()).isEqualTo('\u0000');
            assertThat(result.getFieldShort()).isEqualTo(Short.valueOf("0"));
            assertThat(result.isFieldBool()).isTrue();
        }

        @Test
        @DisplayName("mapping_应该完成基本类型到包装类型的转换_当类型兼容时")
        void mapping_shouldConvertPrimitiveToWrapper_whenTypesCompatible() {
            // Arrange
            UserPrimitive userPrimitive = new UserPrimitive();
            userPrimitive.setAge(10).setName("jack").setId(100);

            // Act
            User result = Pojos.mapping(userPrimitive, User.class);

            // Assert
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getAge()).isEqualTo(10);
            assertThat(result.getName()).isEqualTo("jack");
        }
    }

    @Nested
    @DisplayName("数组映射测试")
    class MappingArrayTests {

        @Test
        @DisplayName("mappingArray_应该返回null_当源列表为null时")
        void mappingArray_shouldReturnNull_whenSourceListIsNull() {
            // Act & Assert
            assertThat(Pojos.mappingArray(null, User.class)).isNull();
        }

        @Test
        @DisplayName("mapping_应该复制非基本类型的复杂对象_当类型完全匹配时")
        void mapping_shouldCopyComplexObjects_whenTypesExactlyMatch() {
            // Arrange
            User user = new User();
            Sex sex = new Sex().setName("a");
            user.setSex(sex).setAge(10);
            ArrayList<Address> addresses = new ArrayList<>(Collections.singletonList(new Address().setAddress("bj")));
            user.setAddresses(addresses);

            // Act
            User result = Pojos.mapping(user, User.class);

            // Assert
            assertThat(result.getSex()).isEqualTo(sex);
            assertThat(result.getAddresses()).isEqualTo(addresses);
            assertThat(result.getAge()).isEqualTo(10);
        }

        @Test
        @DisplayName("mapping_应该处理空值设置类型_当基本类型对应时")
        void mapping_shouldHandleNullSetType_whenPrimitiveTypesMatch() {
            // Arrange
            User user = new User().setAge(12);

            // Act
            UserPrimitiveAge result = Pojos.mapping(user, UserPrimitiveAge.class);

            // Assert
            assertThat(result.getAge()).isEqualTo(12);
        }

        @Test
        @DisplayName("mappingArray_应该映射Bean列表_当所有元素类型一致时")
        void mappingArray_shouldMapBeanList_whenAllElementsHaveSameType() {
            // Arrange
            User user = new User().setAge(12);
            List<User> sourceList = new ArrayList<>(Collections.singletonList(user));

            // Act
            List<UserPrimitiveAge> result = Pojos.mappingArray(sourceList, UserPrimitiveAge.class);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAge()).isEqualTo(12);
        }

        @Test
        @DisplayName("mappingArray_应该映射Map列表_当元素为Map时")
        void mappingArray_shouldMapMapList_whenElementsAreMap() {
            // Arrange
            Record record1 = new Record().set("id", 1).set("name", "b");
            Record record2 = new Record().set("id", 2).set("name", "a").set("age", 1);
            List<Record> sourceList = new ArrayList<>(Arrays.asList(record1, record2));

            // Act
            List<User> result = Pojos.mappingArray(sourceList, User.class);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAge()).isNull();
            assertThat(result.get(1).getAge()).isEqualTo(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(0).getName()).isEqualTo("b");
            assertThat(result.get(1).getName()).isEqualTo("a");
        }

        @Test
        @DisplayName("mappingArray_应该使用字段映射转换Map列表_当提供FieldMapping时")
        void mappingArray_shouldMapMapListWithFieldMapping_whenFieldMappingProvided() {
            // Arrange
            Record record1 = new Record().set("id", 1).set("a_name", "b");
            Record record2 = new Record().set("id", 2).set("a_name", "a").set("age", 1);
            List<Record> sourceList = new ArrayList<>(Arrays.asList(record1, record2));

            // Act
            List<User> result = Pojos.mappingArray(sourceList, User.class, 
                new FieldMapping().field("a_name", "name"));

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("b");
            assertThat(result.get(1).getName()).isEqualTo("a");
        }

        @Test
        @DisplayName("mappingArray_应该处理混合类型列表_当包含Bean和Map时")
        void mappingArray_shouldHandleMixedTypeList_whenContainsBeanAndMap() {
            // Arrange
            Record record = new Record().set("id", 2).set("name", "a").set("age", 1);
            User user = new User().setId(1L).setName("b").setSex(new Sex().setName("nan"));
            List<Object> sourceList = new ArrayList<>(Arrays.asList(record, user));

            // Act
            List<User> result = Pojos.mappingArray(sourceList, User.class);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(1).getAge()).isNull();
            assertThat(result.get(0).getAge()).isEqualTo(1);
            assertThat(result.get(1).getId()).isEqualTo(1L);
            assertThat(result.get(0).getId()).isEqualTo(2L);
            assertThat(result.get(1).getName()).isEqualTo("b");
            assertThat(result.get(0).getName()).isEqualTo("a");
            assertThat(result.get(1).getSex().getName()).isEqualTo("nan");
        }
    }

    @Nested
    @DisplayName("对象复制测试")
    class CopyTests {

        @Test
        @DisplayName("copy_应该复制非空字段_当cover为false且目标字段为空时")
        void copy_shouldCopyNonNullFields_whenCoverFalseAndTargetFieldsEmpty() {
            // Arrange
            ChildUser source = new ChildUser();
            source.setName("5");
            User target = new User();

            // Act
            Pojos.copy(source, target, false);

            // Assert
            assertThat(target.getName()).isEqualTo("5");
            assertThat(target.getId()).isNull();
        }

        @Test
        @DisplayName("copy_应该保持原有值_当cover为false且目标字段不为空时")
        void copy_shouldKeepOriginalValues_whenCoverFalseAndTargetFieldsNotEmpty() {
            // Arrange
            ChildUser source = new ChildUser();
            source.setName("5");
            User target = new User().setName("4").setAge(3);

            // Act
            Pojos.copy(source, target, false);

            // Assert
            assertThat(target.getName()).isEqualTo("4");
            assertThat(target.getId()).isNull();
            assertThat(target.getAge()).isEqualTo(3);
        }

        @Test
        @DisplayName("copy_应该覆盖所有字段_当cover为true时")
        void copy_shouldOverrideAllFields_whenCoverTrue() {
            // Arrange
            ChildUser source = new ChildUser();
            source.setName("5");
            User target = new User().setName("4").setAge(3);

            // Act
            Pojos.copy(source, target, true);

            // Assert
            assertThat(target.getName()).isEqualTo("5");
            assertThat(target.getId()).isNull();
            assertThat(target.getAge()).isNull();
        }

        @Test
        @DisplayName("copy_应该设置null值_当source对象字段为null且cover为true时")
        void copy_shouldSetNullValues_whenSourceFieldsNullAndCoverTrue() {
            // Arrange
            ChildUser source = new ChildUser();
            User target = new User().setName("4").setAge(3);

            // Act
            Pojos.copy(source, target, true);

            // Assert
            assertThat(target.getName()).isNull();
            assertThat(target.getId()).isNull();
            assertThat(target.getAge()).isNull();
        }

        @Test
        @DisplayName("copy_应该抛出IllegalArgumentException_当目标对象为Map时")
        void copy_shouldThrowIllegalArgumentException_whenTargetIsMap() {
            // Arrange
            User user = new User();
            Record target = new Record();

            // Act & Assert
            assertThatThrownBy(() -> Pojos.copy(user, target))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("destClass不能为Map");
        }

        @ParameterizedTest(name = "copy_应该抛出NullPointerException_当参数为null时: {0}")
        @MethodSource("provideNullCopyParameters")
        @DisplayName("copy_应该抛出NullPointerException_当参数为null时")
        void copy_shouldThrowNullPointerException_whenParametersNull(@SuppressWarnings("unused") String description, Object source, Object target) {
            // Act & Assert
            assertThatThrownBy(() -> Pojos.copy(source, target))
                .isInstanceOf(NullPointerException.class);
        }

        private static Stream<Arguments> provideNullCopyParameters() {
            return Stream.of(
                Arguments.of("源对象为null", null, new Record()),
                Arguments.of("目标对象为null", new User(), null)
            );
        }
    }

    // 测试数据类
    @Data
    @Accessors(chain = true)
    public static class User {

        private Long id;
        private String name;
        private Integer age;
        private Sex sex;
        private List<Address> addresses;

        @SuppressWarnings("unused")
        public int getDogAge() {
            return age;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Sex {

        private String name;
    }

    @Data
    @Accessors(chain = true)
    public static class Address {

        private String address;
    }

    @Data
    @Accessors(chain = true)
    public static class UserPrimitiveAge {
        private int age;
    }

    @Data
    @Accessors(chain = true)
    public static class UserPrimitive {

        private long id;
        private String name;
        private int age;
        private byte fieldByte;
        private char fieldChar;
        private double fieldDouble;
        private float fieldFloat;
        private boolean fieldBool;
        private short fieldShort;
        private int innerDogAge;

        @SuppressWarnings("unused")
        public void setDogAge() {
            innerDogAge = 1;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static class ChildUser extends User {

        private String blogType;
        private String log_type;

        public int getDogAge() {
            return 0;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Material {

        private Integer aPass;
        private Integer nameType;
        private String aPassPassword;

    }


}
