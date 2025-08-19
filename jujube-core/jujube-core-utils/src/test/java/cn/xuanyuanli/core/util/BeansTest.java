package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.SequencedCollection;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.util.Beans.FieldDidderence;
import org.junit.jupiter.api.Test;

public class BeansTest {


    @Test
    public void getDeclaredMethod() {
        Method setT = Beans.getDeclaredMethod(ChildUser.class, "setT", String.class, int.class, double.class);
        assertThat(setT.getName()).isEqualTo("setT");

        Method getCardId = Beans.getDeclaredMethod(ChildUser.class, "getCardId");
        assertThat(getCardId.getName()).isEqualTo("getCardId");

        assertThat(Beans.getDeclaredMethod(IdmChildClass.class, "getByAge", int.class).getName()).isEqualTo("getByAge");
    }

    @Test
    public final void beanToMap() {
        assertThat(Beans.beanToMap(null)).isNull();
        HashMap<String, Object> hashMap = new HashMap<>();
        assertThat(Beans.beanToMap(hashMap)).isEqualTo(hashMap);

        ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);
        Map<String, Object> map = Beans.beanToMap(user);
        assertThat(map).hasSize(8);
        assertThat(map.get("name")).isEqualTo("abc");
        assertThat(map.get("age")).isEqualTo(12);
        assertThat(map.get("id")).isNull();
    }

    @Test
    public final void beanToMapFilterNull() {
        ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);
        Map<String, Object> map = Beans.beanToMap(user, true);
        assertThat(map).hasSize(2);
        assertThat(map.get("name")).isEqualTo("abc");
        assertThat(map.get("age")).isEqualTo(12);
        assertThat(map.containsKey("id")).isFalse();
    }

    @Test
    public final void getInstance() {
        assertThat(Beans.getInstance(User.class)).isNotNull();
        assertThatThrownBy(() -> Beans.getInstance(Integer.class)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void forName() {
        assertThat(Beans.forName("cn.xuanyuanli.core.util.BeansTest$User")).isNotNull();
        assertThatThrownBy(() -> Beans.forName("java.lang.Integer1")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getDeclaredField() {
        assertThat(Beans.getDeclaredField(ChildUser.class, "id").getName()).isEqualTo("id");
        assertThat(Beans.getDeclaredField(ChildUser.class, "price").getName()).isEqualTo("price");
        assertThat(Beans.getDeclaredField(ChildUser.class, "price123")).isNull();
    }

    @Test
    void getSelfDeclaredField() {
        assertThat(Beans.getSelfDeclaredField(ChildUser.class, "id")).isNull();
        assertThat(Beans.getSelfDeclaredField(ChildUser.class, "price").getName()).isEqualTo("price");
        assertThat(Beans.getSelfDeclaredField(ChildUser.class, "price123")).isNull();
    }

    @Test
    void contrastObject() {
        assertThat(Beans.contrastObject(new User(), null)).isEmpty();
        assertThat(Beans.contrastObject(null, new User())).isEmpty();
        assertThat(Beans.contrastObject(new ChildUser(), new User())).isEmpty();
        assertThat(Beans.contrastObject(new User(), new User())).isEmpty();
        User u1 = new User();
        u1.setAge(12).setName("jack");
        User u2 = new User();
        u2.setAge(1).setName("john").setBlogType("2");
        List<FieldDidderence> fieldDidderences = Beans.contrastObject(u1, u2);
        assertThat(fieldDidderences).hasSize(3);
        assertThat(fieldDidderences.stream().map(FieldDidderence::getFiledName)).contains("age", "name", "blogType");
        assertThat(fieldDidderences.stream().map(FieldDidderence::getNewValue)).contains("1", "john", "2");
        assertThat(fieldDidderences.stream().map(FieldDidderence::getOldValue)).contains("12", "jack", "");
    }

    @Test
    void getClassGenericType() {
        assertThat(Beans.getClassGenericType(User.class)).isEqualTo(Object.class);
        assertThat(Beans.getClassGenericType(GenericImpl.class)).isEqualTo(User.class);
        assertThat(Beans.getClassGenericType(GenericChildInterface.class)).isEqualTo(User.class);
        assertThat(Beans.getClassGenericType(GenericImpl2.class)).isEqualTo(Integer.class);
        assertThat(Beans.getClassGenericType(GenericImpl2.class, 1)).isEqualTo(Long.class);
        assertThat(Beans.getClassGenericType(GenericImpl2.class, 2)).isEqualTo(Object.class);
    }

    @Test
    void getDefaultClassLoader() {
        assertThat(Beans.getDefaultClassLoader()).isNotNull();
    }

    @Test
    void getObjcetFromMethodArgs() {
        User user = new User();
        assertThat(Beans.getObjcetFromMethodArgs(new Object[]{user, 12}, User.class)).isEqualTo(user);
        ChildUser childUser = new ChildUser();
        assertThat(Beans.getObjcetFromMethodArgs(new Object[]{childUser, 12}, User.class)).isEqualTo(childUser);
    }

    @Test
    public final void getPropertyDescriptor() {
        PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(ChildUser.class, "name");
        assertThat(propertyDescriptor.getWriteMethod()).isNotNull();
        assertThat(propertyDescriptor.getPropertyType()).isEqualTo(String.class);

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "name1");
        assertThat(propertyDescriptor).isNull();

        propertyDescriptor = Beans.getPropertyDescriptor(User.class, "aPass");
        assertThat(propertyDescriptor).isNull();
    }

    @Test
    public final void getAllDeclaredFields() {
        List<String> names = Beans.getAllDeclaredFieldNames(ChildUser.class);
        assertThat(names).hasSize(8).contains("age", "blogType", "cardId", "id", "log_type", "name", "price", "APass");
    }

    @Test
    public final void getAllDeclaredFields2() {
        List<String> names = Beans.getAllDeclaredFieldNames(FieldTest.class);
        assertThat(names).hasSize(2).contains("price", "account");
    }


    @Test
    public final void getMethodParamNames() {
        Method method = Beans.getMethod(User.class, "setT", String.class, int.class, double.class);
        assertThat(Beans.getMethodParamNames(method)).hasSize(3).contains("tname", "tage", "tprice");

        method = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);
        assertThat(Beans.getMethodParamNames(method)).hasSize(2).contains("age", "departmentId");
    }

    @Test
    public final void getProperty() {
        User2 user = new User2().setFInfoId(123).setAge(2).setBlogType("t").setLog_type("y");
        assertThat(Beans.getProperty(user, "fInfoId")).isEqualTo(123);
        assertThat(Beans.getProperty(user, "age")).isEqualTo(2);
        assertThat(Beans.getProperty(user, "blogType")).isEqualTo("t");
        assertThat(Beans.getProperty(user, "log_type")).isEqualTo("y");
    }

    @Test
    public final void setProperty() {
        ChildUser childUser = new ChildUser();
        childUser.setId(12L);
        User3 user = new User3();
        Beans.setProperty(user, "childUser", childUser);
        assertThat(user.getChildUser().getId()).isEqualTo(12L);

        User2 user2 = new User2();
        Beans.setProperty(user, "childUser", new User().setId(13L));
        assertThat(user2.getChildUser()).isNull();
    }

    @Test
    public final void getFormalParamSimpleMapping() {
        Method method = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);
        Map<String, Object> methodParamNames = Beans.getFormalParamSimpleMapping(method, 1, 2);
        assertThat(methodParamNames).hasSize(2);
        assertThat(methodParamNames.get("age")).isEqualTo(1);
        assertThat(methodParamNames.get("departmentId")).isEqualTo(2);
    }

    @Test
    public final void getGenericReturnType() throws NoSuchMethodException {
        Method test = GrClass.class.getMethod("test");
        assertThat(test.getReturnType()).isEqualTo(List.class);
        ParameterizedType genericReturnType = (ParameterizedType) test.getGenericReturnType();
        assertThat(genericReturnType.getActualTypeArguments()[0]).isEqualTo(IUser.class);
    }

    @Test
    public final void getExpectTypeValue() {
        assertThat(Beans.getExpectTypeValue(90, Long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90L, Integer.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90, Double.class)).isEqualTo(90D);
        assertThat(Beans.getExpectTypeValue(90, String.class)).isEqualTo("90");
        assertThat(Beans.getExpectTypeValue(null, String.class)).isNull();
        assertThat(Beans.getExpectTypeValue(null, Long.class)).isNull();
        assertThat(Beans.getExpectTypeValue(null, Short.class)).isNull();
        assertThat(Beans.getExpectTypeValue(null, Integer.class)).isNull();
        assertThat(Beans.getExpectTypeValue(null, Double.class)).isNull();
        assertThat(Beans.getExpectTypeValue(null, Float.class)).isNull();
    }

    @Test
    public final void getExpectTypeValueOfPrimitive() {
        assertThat(Beans.getExpectTypeValue(90L, Long.TYPE)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90, Long.TYPE)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90, long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(90L, int.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90, double.class)).isEqualTo(90D);
        assertThat(Beans.getExpectTypeValue(90D, int.class)).isEqualTo(90);
        assertThat(Beans.getExpectTypeValue(90D, long.class)).isEqualTo(90L);
        assertThat(Beans.getExpectTypeValue(null, int.class)).isEqualTo(0);
        assertThat(Beans.getExpectTypeValue(null, double.class)).isEqualTo(0.0D);
        assertThat(Beans.getExpectTypeValue(null, float.class)).isEqualTo(0.0f);
        assertThat(Beans.getExpectTypeValue(null, long.class)).isEqualTo(0L);
        assertThat(Beans.getExpectTypeValue(null, boolean.class)).isEqualTo(false);
        assertThat(Beans.getExpectTypeValue(null, short.class)).isEqualTo((short) 0);
    }

    @Test
    public void isPrimitive() {
        assertThat(int.class.isPrimitive()).isTrue();
        assertThat(double.class.isPrimitive()).isTrue();
        assertThat(Integer.class.isPrimitive()).isFalse();
        assertThat(String.class.isPrimitive()).isFalse();
    }

    @Test
    void getAllInterfacesAndParentClass() {
        assertThat(Beans.getAllInterfacesAndParentClass(Object.class)).isEmpty();
        assertThat(Beans.getAllInterfacesAndParentClass(User.class)).containsExactly(Object.class);
        assertThat(Beans.getAllInterfacesAndParentClass(IdmClass.class)).isEmpty();
        assertThat(Beans.getAllInterfacesAndParentClass(IdmChildClass.class)).containsSequence(IdmClass.class);
        assertThat(Beans.getAllInterfacesAndParentClass(ArrayList.class)).hasSize(10)
                .containsSequence(AbstractList.class, List.class, SequencedCollection.class, Collection.class, Iterable.class, AbstractCollection.class, Object.class, RandomAccess.class,
                        Cloneable.class, Serializable.class);
    }

    @Test
    public void invokeDefaultMethod1() {
        Object getTableName = Beans.invokeDefaultMethod(Beans.getDeclaredMethod(IdmClass.class, "getTableName"));
        Assertions.assertThat(getTableName).isEqualTo("user");
    }

    @Test
    public void invokeDefaultMethod1_1() {
        Method method = Beans.getDeclaredMethod(IdmChildClass.class, "getIdByAge1");
        assertThatThrownBy(() -> Beans.invokeDefaultMethod(method)).isInstanceOf(RuntimeException.class).hasMessageContaining("InvocationTargetException");
        Method method2 = Beans.getDeclaredMethod(IdmChildClass.class, "getByAge", int.class);
        assertThatThrownBy(() -> Beans.invokeDefaultMethod(method2, 5)).isInstanceOf(RuntimeException.class).hasMessageContaining("InvocationTargetException");
    }

    @Test
    public void invokeDefaultMethod2() {
        Class<?>[] classes = {IdmClass.class};
        IdmClass child = (IdmClass) Proxy.newProxyInstance(IdmClass.class.getClassLoader(), classes, (proxy, method, args) -> {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            return null;
        });
        Assertions.assertThat(child.getTableName()).isEqualTo("user");
    }

    @Test
    public void invokeDefaultMethod2_1() {
        Class<?>[] classes = {IdmChildClass.class};
        IdmChildClass child = (IdmChildClass) Proxy.newProxyInstance(IdmChildClass.class.getClassLoader(), classes, (proxy, method, args) -> {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            return null;
        });
        Assertions.assertThat(child.getTableName()).isEqualTo("user");
    }

    @Test
    public void invokeDefaultMethod3() {
        Class<?>[] clazz = {IdmClass.class};
        IdmClass child = (IdmClass) Proxy.newProxyInstance(clazz[0].getClassLoader(), clazz, (proxy, method, args) -> {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            if (method.getName().equals("findIdByAge")) {
                return 10L;
            }
            return null;
        });
        Assertions.assertThat(child.getByAge(5)).isEqualTo(10L);
    }

    @Test
    public void invokeDefaultMethod3_1() {
        Class<?>[] clazz = {IdmChildClass.class};
        IdmChildClass child = (IdmChildClass) Proxy.newProxyInstance(clazz[0].getClassLoader(), clazz, (proxy, method, args) -> {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            if (method.getName().equals("findIdByAge")) {
                return 10L;
            }
            return null;
        });
        Assertions.assertThat(child.getByAge(5)).isEqualTo(12L);
        Assertions.assertThat(child.getIdByAge1()).isEqualTo(12L);
    }

    @Test
    public void isBasicType() {
        Assertions.assertThat(Beans.isBasicType(boolean.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(int.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(long.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(short.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(double.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(float.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(char.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(byte.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(String.class)).isTrue();

        Assertions.assertThat(Beans.isBasicType(Boolean.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Integer.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Long.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Short.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Double.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Float.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Character.class)).isTrue();
        Assertions.assertThat(Beans.isBasicType(Byte.class)).isTrue();

        Assertions.assertThat(Beans.isBasicType(null)).isFalse();
        Assertions.assertThat(Beans.isBasicType(Class.class)).isFalse();
        Assertions.assertThat(Beans.isBasicType(User.class)).isFalse();
    }

    @Test
    void getMethodReturnParameterizedTypeFirst() throws NoSuchMethodException {
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test8"), null)).isEqualTo(Serializable.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test7"), null)).isEqualTo(Serializable.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test6"), null)).isNull();
        Method test5 = GrClass.class.getMethod("test5");
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(test5, test5.getReturnType())).isEqualTo(IUser.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test"), null)).isEqualTo(IUser.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test2"), Object.class)).isEqualTo(Object.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test2"), null)).isEqualTo(Object.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test3"), null)).isEqualTo(String.class);
        Assertions.assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test4"), null)).isEqualTo(String.class);
    }

    @SuppressWarnings("unused")
    public interface GenericInterface<T> {

    }

    public interface GenericChildInterface extends GenericInterface<User> {

    }

    public interface IdmClass {

        default String getTableName() {
            return "user";
        }

        default long getByAge(int age) {
            return findIdByAge(age);
        }

        long findIdByAge(int age);
    }

    public interface IdmChildClass extends IdmClass {

        default long getIdByAge1() {
            return findIdByAge(1);
        }

        default long findIdByAge(int age) {
            return 12;
        }

    }

    public interface IUser {

        @SuppressWarnings("unused")
        long queryAgeCount(long age, long departmentId);
    }

    @SuppressWarnings("rawtypes")
    public interface GrClass {

        List<IUser> test();

        List<?> test2();

        List<String> test3();

        Map<String, Object> test4();

        IUser test5();

        List test6();

        List<? extends Serializable> test7();

        Map<? extends Serializable, String> test8();
    }

    @SuppressWarnings("unused")
    public static abstract class AbstractGeneric<T, K> {

    }

    public static class GenericImpl implements GenericInterface<User> {

    }

    public static class GenericImpl2 extends AbstractGeneric<Integer, Long> implements GenericInterface<User> {

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User {

        private Long id;
        private String name;
        private Integer age;
        private String blogType;
        private String log_type;

        @SuppressWarnings({"unused", "EmptyMethod"})
        public void setT(String tname, int tage, double tprice) {
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ChildUser extends User {

        private Long cardId;
        private Long aPass;
        private Double price;

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User2 {

        private Long id;
        private String name;
        private Integer age;
        private String blogType;
        private String log_type;
        private Integer fInfoId;
        private ChildUser childUser;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User3 {

        private User childUser;
    }

    @Data
    public static class FieldTest {

        private Double price;

        @SuppressWarnings("unused")
        public Double getAccount() {
            return price;
        }
    }
}
