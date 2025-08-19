package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.Pojos.FieldMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ConstantValue")
public class PojosTests {

    @Test
    public void mappingMapFieldMapping() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "123");
        map.put("name", "abc");
        map.put("age", "12");
        map.put("btype", "1");
        map.put("ltype", "2");

        // 高端版
        ChildUser user2 = Pojos.mapping(map, ChildUser.class, new FieldMapping().field("btype", "blogType").field("ltype", "log_type"));
        assertThat(user2.getBlogType()).isEqualTo(map.get("btype"));
        assertThat(user2.getLog_type()).isEqualTo(map.get("ltype"));
        assertThat(user2.getAge()).isEqualTo(12);
    }

    @Test
    public void mappingRecodPrimitive() {
        Record record = new Record().set("a_pass_password", "123").set("a_pass", 0).set("nameType", 1);
        Material material = Pojos.mapping(record, Material.class);
        assertThat(material.getAPass()).isEqualTo(0);
        assertThat(material.getNameType()).isEqualTo(1);
        assertThat(material.getAPassPassword()).isEqualTo("123");

        record = new Record().set("a_pass_password_", "123").set("a_pass_", 0);
        material = Pojos.mapping(record, Material.class);
        assertThat(material.getAPass()).isEqualTo(0);
        assertThat(material.getAPassPassword()).isEqualTo("123");
    }

    @Test
    public void mappingRecodMapToBean() {
        Record sex = new Record();
        sex.set("name", "1");
        Record record = new Record().set("sex", sex);
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getSex()).isNull();
    }

    @Test
    public void mappingRecodBeanToBean() {
        Sex sex = new Sex().setName("1");
        Record record = new Record().set("sex", sex);
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getSex()).isEqualTo(sex);
    }

    @Test
    public void mappingRecodBeanToBeanDiff() {
        Address address = new Address().setAddress("b");
        Record record = new Record().set("sex", address);
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getSex()).isNull();
    }

    @Test
    public void mappingRecodListToList() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address().setAddress("bj"));
        Record record = new Record().set("addresses", addresses);
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getAddresses()).isEqualTo(addresses);
    }

    /**
     * 从测试中可以看到，集合复制的过程中存在泛型消除问题
     */
    @Test
    public void mappingRecodListToListDiff() {
        Sex sex = new Sex().setName("1");
        List<Sex> addresses = new ArrayList<>();
        addresses.add(sex);
        Record record = new Record().set("addresses", addresses);
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getAddresses()).isEqualTo(addresses);
    }

    @Test
    public void mappingRecordFieldMapping() {
        Record record = new Record().set("id", 1).set("pid", 256);
        User user = Pojos.mapping(record, User.class, new FieldMapping().field("pid", "id"));
        assertThat(user.getId()).isEqualTo(256);
    }

    @Test
    public void mappingRecordChildFieldMapping() {
        Record record = new Record().set("blog_type", 1);
        ChildUser user = Pojos.mapping(record, ChildUser.class, new FieldMapping().field("blog_type", "blogType"));
        assertThat(user.getBlogType()).isEqualTo("1");
    }

    @Test
    public void mappingNull() {
        assertThat(Pojos.mapping(null, User.class)).isEqualTo(null);
        assertThat((Object) Pojos.mapping(new Record(), null)).isEqualTo(null);

        List<User> users = Pojos.mappingArray(null, User.class);
        assertThat(users).isEqualTo(null);
    }

    @Test
    public void mappingBeanToMap() {
        @SuppressWarnings("PojosMappingInspection")
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> Pojos.mapping(new User(), Record.class));
        assertThat(exception.getMessage()).isEqualTo("destClass不能为Map");
    }

    @Test
    public void mappingRecordErrorDefaultValue() {
        Record record = new Record().set("id", "1,2").set("age", "abc");
        User user = Pojos.mapping(record, User.class);
        assertThat(user.getId()).isEqualTo(0L);
        assertThat(user.getAge()).isEqualTo(0);
    }

    @Test
    public void mappingRecordWrapToPrimitive() {
        Record record = new Record().set("id", "1").set("fieldBool", true);
        UserPrimitive user = Pojos.mapping(record, UserPrimitive.class);
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getAge()).isEqualTo(0);
        assertThat(user.getFieldByte()).isEqualTo(Byte.valueOf("0"));
        assertThat(user.getFieldDouble()).isEqualTo(0.0D);
        assertThat(user.getFieldFloat()).isEqualTo(0.0F);
        assertThat(user.getFieldChar()).isEqualTo('\u0000');
        assertThat(user.getFieldShort()).isEqualTo(Short.valueOf("0"));
        assertThat(user.isFieldBool()).isEqualTo(true);
    }

    @Test
    public void mappingRecordPrimitiveToWrap() {
        UserPrimitive userPrimitive = new UserPrimitive();
        userPrimitive.setAge(10).setName("jack").setId(100);
        User user = Pojos.mapping(userPrimitive, User.class);
        assertThat(user.getId()).isEqualTo(100L);
        assertThat(user.getAge()).isEqualTo(10);
        assertThat(user.getName()).isEqualTo("jack");
    }

    @Test
    @Disabled
    public void mappingRecordPrimitiveToWrap_multiple() {
        for (int i = 0; i < 10_000_000; i++) {
            mappingRecordPrimitiveToWrap();
        }
    }

    @Test
    public void mappingBeanNotBaseType() {
        User user = new User();
        Sex sex = new Sex().setName("a");
        user.setSex(sex).setAge(10);
        ArrayList<Address> addresses = Lists.newArrayList(new Address().setAddress("bj"));
        user.setAddresses(addresses);
        User copy = Pojos.mapping(user, User.class);
        assertThat(copy.getSex()).isEqualTo(sex);
        assertThat(copy.getAddresses()).isEqualTo(addresses);
        assertThat(copy.getAge()).isEqualTo(10);
    }

    @Test
    public void mappingBeanNullSetType() {
        User user = new User().setAge(12);
        UserPrimitiveAge userPrimitive = Pojos.mapping(user, UserPrimitiveAge.class);
        assertThat(userPrimitive.getAge()).isEqualTo(12);
    }

    @Test
    public void mappingArrayBean() {
        User user = new User().setAge(12);
        List<UserPrimitiveAge> userPrimitive = Pojos.mappingArray(Lists.newArrayList(user), UserPrimitiveAge.class);
        assertThat(userPrimitive.get(0).getAge()).isEqualTo(12);
    }

    @Test
    public void mappingArrayMap() {
        Record record = new Record().set("id", 1).set("name", "b");
        Record record1 = new Record().set("id", 2).set("name", "a").set("age", 1);
        List<User> users = Pojos.mappingArray(Lists.newArrayList(record, record1), User.class);
        assertThat(users.get(0).getAge()).isNull();
        assertThat(users.get(1).getAge()).isEqualTo(1);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(0).getName()).isEqualTo("b");
        assertThat(users.get(1).getName()).isEqualTo("a");
    }

    @Test
    public void mappingArrayMapFieldMapping() {
        Record record = new Record().set("id", 1).set("a_name", "b");
        Record record1 = new Record().set("id", 2).set("a_name", "a").set("age", 1);
        List<User> users = Pojos.mappingArray(Lists.newArrayList(record, record1), User.class, new FieldMapping().field("a_name", "name"));
        assertThat(users.get(0).getAge()).isNull();
        assertThat(users.get(1).getAge()).isEqualTo(1);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(0).getName()).isEqualTo("b");
        assertThat(users.get(1).getName()).isEqualTo("a");
    }

    @Test
    public void mappingArrayBeanAndMap() {
        Record record = new Record().set("id", 2).set("name", "a").set("age", 1);
        User record1 = new User().setId(1L).setName("b").setSex(new Sex().setName("nan"));
        List<User> users = Pojos.mappingArray(Lists.newArrayList(record, record1), User.class);
        assertThat(users.get(1).getAge()).isNull();
        assertThat(users.get(0).getAge()).isEqualTo(1);
        assertThat(users.get(1).getId()).isEqualTo(1L);
        assertThat(users.get(0).getId()).isEqualTo(2L);
        assertThat(users.get(1).getName()).isEqualTo("b");
        assertThat(users.get(0).getName()).isEqualTo("a");
        assertThat(users.get(1).getSex().getName()).isEqualTo("nan");
    }

    @Test
    public void copy() {
        ChildUser source = new ChildUser();
        source.setName("5");

        User destObj = new User();
        Pojos.copy(source, destObj, false);
        assertThat(destObj.getName()).isEqualTo("5");
        assertThat(destObj.getId()).isNull();
    }

    @Test
    public void copyCoverFalse() {
        ChildUser source = new ChildUser();
        source.setName("5");

        User destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj, false);
        assertThat(destObj.getName()).isEqualTo("4");
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isEqualTo(3);
    }

    @Test
    public void copyCoverTrue() {
        ChildUser source = new ChildUser();
        source.setName("5");
        User destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj, true);
        assertThat(destObj.getName()).isEqualTo("5");
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isNull();

        source = new ChildUser();
        destObj = new User().setName("4").setAge(3);
        Pojos.copy(source, destObj, true);
        assertThat(destObj.getName()).isNull();
        assertThat(destObj.getId()).isNull();
        assertThat(destObj.getAge()).isNull();
    }

    @Test
    public void copyBeanToMap() {
        @SuppressWarnings("PojosMappingInspection")
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> Pojos.copy(new User(), new Record()));
        assertThat(exception.getMessage()).isEqualTo("destClass不能为Map");
    }

    @Test
    public void copyNull() {
        @SuppressWarnings("PojosMappingInspection")
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> Pojos.copy(null, new Record()));
        assertThat(exception.getMessage()).isNull();

        exception = Assertions.assertThrows(NullPointerException.class, () -> Pojos.copy(new User(), null));
        assertThat(exception.getMessage()).isNull();
    }

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
