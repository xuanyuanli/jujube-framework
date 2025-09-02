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
import cn.xuanyuanli.core.util.Beans.FieldDidderence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Beans 工具类测试")
class BeansTest {

    @Nested
    @DisplayName("反射方法测试")
    class ReflectionMethodTests {

        @Test
        @DisplayName("getDeclaredMethod_应该返回指定方法_当方法存在时")
        void getDeclaredMethod_shouldReturnSpecifiedMethod_whenMethodExists() {
            // Arrange & Act
            Method setT = Beans.getDeclaredMethod(ChildUser.class, "setT", String.class, int.class, double.class);
            Method getCardId = Beans.getDeclaredMethod(ChildUser.class, "getCardId");
            Method getByAge = Beans.getDeclaredMethod(IdmChildClass.class, "getByAge", int.class);

            // Assert
            assertThat(setT.getName()).isEqualTo("setT");
            assertThat(getCardId.getName()).isEqualTo("getCardId");
            assertThat(getByAge.getName()).isEqualTo("getByAge");
        }

        @Test
        @DisplayName("getMethodParamNames_应该返回参数名列表_当方法有参数时")
        void getMethodParamNames_shouldReturnParameterNames_whenMethodHasParameters() {
            // Arrange
            Method method = Beans.getMethod(User.class, "setT", String.class, int.class, double.class);
            Method interfaceMethod = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);

            // Act & Assert
            assertThat(Beans.getMethodParamNames(method)).hasSize(3).contains("tname", "tage", "tprice");
            assertThat(Beans.getMethodParamNames(interfaceMethod)).hasSize(2).contains("age", "departmentId");
        }

        @Test
        @DisplayName("getFormalParamSimpleMapping_应该返回参数映射_当调用方法时")
        void getFormalParamSimpleMapping_shouldReturnParameterMapping_whenMethodIsCalled() {
            // Arrange
            Method method = Beans.getSelfDeclaredMethod(IUser.class, "queryAgeCount", long.class, long.class);

            // Act
            Map<String, Object> result = Beans.getFormalParamSimpleMapping(method, 1, 2);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get("age")).isEqualTo(1);
            assertThat(result.get("departmentId")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Bean转换测试")
    class BeanConversionTests {

        @Test
        @DisplayName("beanToMap_应该返回null_当bean为null时")
        void beanToMap_shouldReturnNull_whenBeanIsNull() {
            // Act & Assert
            assertThat(Beans.beanToMap(null)).isNull();
        }

        @Test
        @DisplayName("beanToMap_应该返回原对象_当输入为Map时")
        void beanToMap_shouldReturnOriginalObject_whenInputIsMap() {
            // Arrange
            HashMap<String, Object> hashMap = new HashMap<>();

            // Act & Assert
            assertThat(Beans.beanToMap(hashMap)).isEqualTo(hashMap);
        }

        @Test
        @DisplayName("beanToMap_应该转换为Map_当输入为普通Bean时")
        void beanToMap_shouldConvertToMap_whenInputIsRegularBean() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);

            // Act
            Map<String, Object> map = Beans.beanToMap(user);

            // Assert
            assertThat(map).hasSize(8);
            assertThat(map.get("name")).isEqualTo("abc");
            assertThat(map.get("age")).isEqualTo(12);
            assertThat(map.get("id")).isNull();
        }

        @Test
        @DisplayName("beanToMap_应该过滤null值_当filterNull为true时")
        void beanToMap_shouldFilterNullValues_whenFilterNullIsTrue() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setName("abc").setAge(12);

            // Act
            Map<String, Object> map = Beans.beanToMap(user, true);

            // Assert
            assertThat(map).hasSize(2);
            assertThat(map.get("name")).isEqualTo("abc");
            assertThat(map.get("age")).isEqualTo(12);
            assertThat(map.containsKey("id")).isFalse();
        }
    }

    @Nested
    @DisplayName("类实例化测试")
    class ClassInstantiationTests {

        @Test
        @DisplayName("getInstance_应该创建实例_当类有默认构造函数时")
        void getInstance_shouldCreateInstance_whenClassHasDefaultConstructor() {
            // Act & Assert
            assertThat(Beans.getInstance(User.class)).isNotNull();
        }

        @Test
        @DisplayName("getInstance_应该抛出异常_当类没有默认构造函数时")
        void getInstance_shouldThrowException_whenClassHasNoDefaultConstructor() {
            // Act & Assert
            assertThatThrownBy(() -> Beans.getInstance(Integer.class)).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("forName_应该返回类_当类名存在时")
        void forName_shouldReturnClass_whenClassNameExists() {
            // Act & Assert
            assertThat(Beans.forName("cn.xuanyuanli.core.util.BeansTest$User")).isNotNull();
        }

        @Test
        @DisplayName("forName_应该抛出异常_当类名不存在时")
        void forName_shouldThrowException_whenClassNameDoesNotExist() {
            // Act & Assert
            assertThatThrownBy(() -> Beans.forName("java.lang.Integer1")).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("字段访问测试")
    class FieldAccessTests {

        @Test
        @DisplayName("getDeclaredField_应该返回字段_当字段存在时")
        void getDeclaredField_shouldReturnField_whenFieldExists() {
            // Act & Assert
            assertThat(Beans.getDeclaredField(ChildUser.class, "id").getName()).isEqualTo("id");
            assertThat(Beans.getDeclaredField(ChildUser.class, "price").getName()).isEqualTo("price");
            assertThat(Beans.getDeclaredField(ChildUser.class, "price123")).isNull();
        }

        @Test
        @DisplayName("getSelfDeclaredField_应该只返回本类字段_当字段存在时")
        void getSelfDeclaredField_shouldReturnOnlySelfDeclaredField_whenFieldExists() {
            // Act & Assert
            assertThat(Beans.getSelfDeclaredField(ChildUser.class, "id")).isNull();
            assertThat(Beans.getSelfDeclaredField(ChildUser.class, "price").getName()).isEqualTo("price");
            assertThat(Beans.getSelfDeclaredField(ChildUser.class, "price123")).isNull();
        }

        @Test
        @DisplayName("getAllDeclaredFieldNames_应该返回所有字段名_当获取类字段时")
        void getAllDeclaredFieldNames_shouldReturnAllFieldNames_whenGettingClassFields() {
            // Act
            List<String> names = Beans.getAllDeclaredFieldNames(ChildUser.class);
            List<String> fieldTestNames = Beans.getAllDeclaredFieldNames(FieldTest.class);

            // Assert
            assertThat(names).hasSize(8).contains("age", "blogType", "cardId", "id", "log_type", "name", "price", "APass");
            assertThat(fieldTestNames).hasSize(2).contains("price", "account");
        }
    }

    @Nested
    @DisplayName("对象对比测试")
    class ObjectComparisonTests {

        @Test
        @DisplayName("contrastObject_应该返回空列表_当对象相同或为null时")
        void contrastObject_shouldReturnEmptyList_whenObjectsAreSameOrNull() {
            // Act & Assert
            assertThat(Beans.contrastObject(new User(), null)).isEmpty();
            assertThat(Beans.contrastObject(null, new User())).isEmpty();
            assertThat(Beans.contrastObject(new ChildUser(), new User())).isEmpty();
            assertThat(Beans.contrastObject(new User(), new User())).isEmpty();
        }

        @Test
        @DisplayName("contrastObject_应该返回差异列表_当对象不同时")
        void contrastObject_shouldReturnDifferenceList_whenObjectsAreDifferent() {
            // Arrange
            User u1 = new User();
            u1.setAge(12).setName("jack");
            User u2 = new User();
            u2.setAge(1).setName("john").setBlogType("2");

            // Act
            List<FieldDidderence> fieldDidderences = Beans.contrastObject(u1, u2);

            // Assert
            assertThat(fieldDidderences).hasSize(3);
            assertThat(fieldDidderences.stream().map(FieldDidderence::getFiledName)).contains("age", "name", "blogType");
            assertThat(fieldDidderences.stream().map(FieldDidderence::getNewValue)).contains("1", "john", "2");
            assertThat(fieldDidderences.stream().map(FieldDidderence::getOldValue)).contains("12", "jack", "");
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("getClassGenericType_应该返回泛型类型_当类有泛型时")
        void getClassGenericType_shouldReturnGenericType_whenClassHasGeneric() {
            // Act & Assert
            assertThat(Beans.getClassGenericType(User.class)).isEqualTo(Object.class);
            assertThat(Beans.getClassGenericType(GenericImpl.class)).isEqualTo(User.class);
            assertThat(Beans.getClassGenericType(GenericChildInterface.class)).isEqualTo(User.class);
            assertThat(Beans.getClassGenericType(GenericImpl2.class)).isEqualTo(Integer.class);
            assertThat(Beans.getClassGenericType(GenericImpl2.class, 1)).isEqualTo(Long.class);
            assertThat(Beans.getClassGenericType(GenericImpl2.class, 2)).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("getGenericReturnType_应该返回泛型返回类型_当方法有泛型时")
        void getGenericReturnType_shouldReturnGenericReturnType_whenMethodHasGeneric() throws NoSuchMethodException {
            // Arrange
            Method test = GrClass.class.getMethod("test");

            // Act
            ParameterizedType genericReturnType = (ParameterizedType) test.getGenericReturnType();

            // Assert
            assertThat(test.getReturnType()).isEqualTo(List.class);
            assertThat(genericReturnType.getActualTypeArguments()[0]).isEqualTo(IUser.class);
        }

        @Test
        @DisplayName("getMethodReturnParameterizedTypeFirst_应该返回第一个泛型类型_当方法有参数化类型时")
        void getMethodReturnParameterizedTypeFirst_shouldReturnFirstGenericType_whenMethodHasParameterizedType() throws NoSuchMethodException {
            // Act & Assert
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test8"), null)).isEqualTo(Serializable.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test7"), null)).isEqualTo(Serializable.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test6"), null)).isNull();
            
            Method test5 = GrClass.class.getMethod("test5");
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(test5, test5.getReturnType())).isEqualTo(IUser.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test"), null)).isEqualTo(IUser.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test2"), Object.class)).isEqualTo(Object.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test2"), null)).isEqualTo(Object.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test3"), null)).isEqualTo(String.class);
            assertThat(Beans.getMethodReturnParameterizedTypeFirst(GrClass.class.getMethod("test4"), null)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("类加载器测试")
    class ClassLoaderTests {

        @Test
        @DisplayName("getDefaultClassLoader_应该返回类加载器_当调用时")
        void getDefaultClassLoader_shouldReturnClassLoader_whenCalled() {
            // Act & Assert
            assertThat(Beans.getDefaultClassLoader()).isNotNull();
        }
    }

    @Nested
    @DisplayName("方法参数测试")
    class MethodArgumentTests {

        @Test
        @DisplayName("getObjcetFromMethodArgs_应该返回指定类型对象_当参数中包含时")
        void getObjcetFromMethodArgs_shouldReturnSpecifiedTypeObject_whenArgsContainIt() {
            // Arrange
            User user = new User();
            ChildUser childUser = new ChildUser();

            // Act & Assert
            assertThat(Beans.getObjcetFromMethodArgs(new Object[]{user, 12}, User.class)).isEqualTo(user);
            assertThat(Beans.getObjcetFromMethodArgs(new Object[]{childUser, 12}, User.class)).isEqualTo(childUser);
        }
    }

    @Nested
    @DisplayName("属性描述符测试")
    class PropertyDescriptorTests {

        @Test
        @DisplayName("getPropertyDescriptor_应该返回属性描述符_当属性存在时")
        void getPropertyDescriptor_shouldReturnPropertyDescriptor_whenPropertyExists() {
            // Act
            PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(ChildUser.class, "name");

            // Assert
            assertThat(propertyDescriptor.getWriteMethod()).isNotNull();
            assertThat(propertyDescriptor.getPropertyType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getPropertyDescriptor_应该返回null_当属性不存在时")
        void getPropertyDescriptor_shouldReturnNull_whenPropertyDoesNotExist() {
            // Act & Assert
            assertThat(Beans.getPropertyDescriptor(User.class, "name1")).isNull();
            assertThat(Beans.getPropertyDescriptor(User.class, "aPass")).isNull();
        }
    }

    @Nested
    @DisplayName("属性操作测试")
    class PropertyOperationTests {

        @Test
        @DisplayName("getProperty_应该返回属性值_当获取属性时")
        void getProperty_shouldReturnPropertyValue_whenGettingProperty() {
            // Arrange
            User2 user = new User2().setFInfoId(123).setAge(2).setBlogType("t").setLog_type("y");

            // Act & Assert
            assertThat(Beans.getProperty(user, "fInfoId")).isEqualTo(123);
            assertThat(Beans.getProperty(user, "age")).isEqualTo(2);
            assertThat(Beans.getProperty(user, "blogType")).isEqualTo("t");
            assertThat(Beans.getProperty(user, "log_type")).isEqualTo("y");
        }

        @Test
        @DisplayName("setProperty_应该设置属性值_当类型匹配时")
        void setProperty_shouldSetPropertyValue_whenTypeMatches() {
            // Arrange
            ChildUser childUser = new ChildUser();
            childUser.setId(12L);
            User3 user = new User3();

            // Act
            Beans.setProperty(user, "childUser", childUser);

            // Assert
            assertThat(user.getChildUser().getId()).isEqualTo(12L);
        }

        @Test
        @DisplayName("setProperty_应该忽略设置_当类型不匹配时")
        void setProperty_shouldIgnoreSetting_whenTypeDoesNotMatch() {
            // Arrange
            User2 user2 = new User2();
            User3 user = new User3();

            // Act
            Beans.setProperty(user, "childUser", new User().setId(13L));

            // Assert
            assertThat(user2.getChildUser()).isNull();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("getExpectTypeValue_应该转换为期望类型_当输入有值时")
        void getExpectTypeValue_shouldConvertToExpectedType_whenInputHasValue() {
            // Act & Assert
            assertThat(Beans.getExpectTypeValue(90, Long.class)).isEqualTo(90L);
            assertThat(Beans.getExpectTypeValue(90L, Integer.class)).isEqualTo(90);
            assertThat(Beans.getExpectTypeValue(90, Double.class)).isEqualTo(90D);
            assertThat(Beans.getExpectTypeValue(90, String.class)).isEqualTo("90");
        }

        @Test
        @DisplayName("getExpectTypeValue_应该返回null_当输入为null且为包装类型时")
        void getExpectTypeValue_shouldReturnNull_whenInputIsNullAndWrapperType() {
            // Act & Assert
            assertThat(Beans.getExpectTypeValue(null, String.class)).isNull();
            assertThat(Beans.getExpectTypeValue(null, Long.class)).isNull();
            assertThat(Beans.getExpectTypeValue(null, Short.class)).isNull();
            assertThat(Beans.getExpectTypeValue(null, Integer.class)).isNull();
            assertThat(Beans.getExpectTypeValue(null, Double.class)).isNull();
            assertThat(Beans.getExpectTypeValue(null, Float.class)).isNull();
        }

        @Test
        @DisplayName("getExpectTypeValue_应该转换为基本类型_当目标为基本类型时")
        void getExpectTypeValue_shouldConvertToPrimitiveType_whenTargetIsPrimitive() {
            // Act & Assert
            assertThat(Beans.getExpectTypeValue(90L, Long.TYPE)).isEqualTo(90L);
            assertThat(Beans.getExpectTypeValue(90, Long.TYPE)).isEqualTo(90L);
            assertThat(Beans.getExpectTypeValue(90, long.class)).isEqualTo(90L);
            assertThat(Beans.getExpectTypeValue(90L, int.class)).isEqualTo(90);
            assertThat(Beans.getExpectTypeValue(90, double.class)).isEqualTo(90D);
            assertThat(Beans.getExpectTypeValue(90D, int.class)).isEqualTo(90);
            assertThat(Beans.getExpectTypeValue(90D, long.class)).isEqualTo(90L);
        }

        @Test
        @DisplayName("getExpectTypeValue_应该返回默认值_当输入为null且为基本类型时")
        void getExpectTypeValue_shouldReturnDefaultValue_whenInputIsNullAndPrimitiveType() {
            // Act & Assert
            assertThat(Beans.getExpectTypeValue(null, int.class)).isEqualTo(0);
            assertThat(Beans.getExpectTypeValue(null, double.class)).isEqualTo(0.0D);
            assertThat(Beans.getExpectTypeValue(null, float.class)).isEqualTo(0.0f);
            assertThat(Beans.getExpectTypeValue(null, long.class)).isEqualTo(0L);
            assertThat(Beans.getExpectTypeValue(null, boolean.class)).isEqualTo(false);
            assertThat(Beans.getExpectTypeValue(null, short.class)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("isPrimitive_应该正确判断基本类型_当检查类型时")
        void isPrimitive_shouldCorrectlyIdentifyPrimitiveTypes_whenCheckingTypes() {
            // Act & Assert
            assertThat(int.class.isPrimitive()).isTrue();
            assertThat(double.class.isPrimitive()).isTrue();
            assertThat(Integer.class.isPrimitive()).isFalse();
            assertThat(String.class.isPrimitive()).isFalse();
        }

        @Test
        @DisplayName("isBasicType_应该正确判断基础类型_当检查类型时")
        void isBasicType_shouldCorrectlyIdentifyBasicTypes_whenCheckingTypes() {
            // Act & Assert
            assertThat(Beans.isBasicType(boolean.class)).isTrue();
            assertThat(Beans.isBasicType(int.class)).isTrue();
            assertThat(Beans.isBasicType(long.class)).isTrue();
            assertThat(Beans.isBasicType(short.class)).isTrue();
            assertThat(Beans.isBasicType(double.class)).isTrue();
            assertThat(Beans.isBasicType(float.class)).isTrue();
            assertThat(Beans.isBasicType(char.class)).isTrue();
            assertThat(Beans.isBasicType(byte.class)).isTrue();
            assertThat(Beans.isBasicType(String.class)).isTrue();

            assertThat(Beans.isBasicType(Boolean.class)).isTrue();
            assertThat(Beans.isBasicType(Integer.class)).isTrue();
            assertThat(Beans.isBasicType(Long.class)).isTrue();
            assertThat(Beans.isBasicType(Short.class)).isTrue();
            assertThat(Beans.isBasicType(Double.class)).isTrue();
            assertThat(Beans.isBasicType(Float.class)).isTrue();
            assertThat(Beans.isBasicType(Character.class)).isTrue();
            assertThat(Beans.isBasicType(Byte.class)).isTrue();

            assertThat(Beans.isBasicType(null)).isFalse();
            assertThat(Beans.isBasicType(Class.class)).isFalse();
            assertThat(Beans.isBasicType(User.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("getAllInterfacesAndParentClass_应该返回继承关系_当获取类层次时")
        void getAllInterfacesAndParentClass_shouldReturnInheritanceHierarchy_whenGettingClassHierarchy() {
            // Act & Assert
            assertThat(Beans.getAllInterfacesAndParentClass(Object.class)).isEmpty();
            assertThat(Beans.getAllInterfacesAndParentClass(User.class)).containsExactly(Object.class);
            assertThat(Beans.getAllInterfacesAndParentClass(IdmClass.class)).isEmpty();
            assertThat(Beans.getAllInterfacesAndParentClass(IdmChildClass.class)).containsSequence(IdmClass.class);
            assertThat(Beans.getAllInterfacesAndParentClass(ArrayList.class)).hasSize(10)
                    .containsSequence(AbstractList.class, List.class, SequencedCollection.class, Collection.class, Iterable.class, AbstractCollection.class, Object.class, RandomAccess.class,
                            Cloneable.class, Serializable.class);
        }
    }

    @Nested
    @DisplayName("默认方法调用测试")
    class DefaultMethodInvocationTests {

        @Test
        @DisplayName("invokeDefaultMethod_应该调用默认方法_当方法为默认方法时")
        void invokeDefaultMethod_shouldInvokeDefaultMethod_whenMethodIsDefault() {
            // Act
            Object getTableName = Beans.invokeDefaultMethod(Beans.getDeclaredMethod(IdmClass.class, "getTableName"));

            // Assert
            assertThat(getTableName).isEqualTo("user");
        }

        @Test
        @DisplayName("invokeDefaultMethod_应该抛出异常_当调用失败时")
        void invokeDefaultMethod_shouldThrowException_whenInvocationFails() {
            // Arrange
            Method method = Beans.getDeclaredMethod(IdmChildClass.class, "getIdByAge1");
            Method method2 = Beans.getDeclaredMethod(IdmChildClass.class, "getByAge", int.class);

            // Act & Assert
            assertThatThrownBy(() -> Beans.invokeDefaultMethod(method)).isInstanceOf(RuntimeException.class).hasMessageContaining("InvocationTargetException");
            assertThatThrownBy(() -> Beans.invokeDefaultMethod(method2, 5)).isInstanceOf(RuntimeException.class).hasMessageContaining("InvocationTargetException");
        }

        @Test
        @DisplayName("invokeDefaultMethod_应该在代理中正常工作_当使用Proxy时")
        void invokeDefaultMethod_shouldWorkInProxy_whenUsingProxy() {
            // Arrange
            Class<?>[] classes = {IdmClass.class};
            IdmClass child = (IdmClass) Proxy.newProxyInstance(IdmClass.class.getClassLoader(), classes, (proxy, method, args) -> {
                if (method.isDefault()) {
                    return Beans.invokeDefaultMethod(proxy, method, args);
                }
                return null;
            });

            // Act & Assert
            assertThat(child.getTableName()).isEqualTo("user");
        }

        @Test
        @DisplayName("invokeDefaultMethod_应该在子接口代理中正常工作_当使用Proxy时")
        void invokeDefaultMethod_shouldWorkInChildInterfaceProxy_whenUsingProxy() {
            // Arrange
            Class<?>[] classes = {IdmChildClass.class};
            IdmChildClass child = (IdmChildClass) Proxy.newProxyInstance(IdmChildClass.class.getClassLoader(), classes, (proxy, method, args) -> {
                if (method.isDefault()) {
                    return Beans.invokeDefaultMethod(proxy, method, args);
                }
                return null;
            });

            // Act & Assert
            assertThat(child.getTableName()).isEqualTo("user");
        }

        @Test
        @DisplayName("invokeDefaultMethod_应该正确调用组合方法_当方法依赖其他方法时")
        void invokeDefaultMethod_shouldCorrectlyInvokeCompositeMethod_whenMethodDependsOnOthers() {
            // Arrange
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

            Class<?>[] clazz2 = {IdmChildClass.class};
            IdmChildClass child2 = (IdmChildClass) Proxy.newProxyInstance(clazz2[0].getClassLoader(), clazz2, (proxy, method, args) -> {
                if (method.isDefault()) {
                    return Beans.invokeDefaultMethod(proxy, method, args);
                }
                if (method.getName().equals("findIdByAge")) {
                    return 10L;
                }
                return null;
            });

            // Act & Assert
            assertThat(child.getByAge(5)).isEqualTo(10L);
            assertThat(child2.getByAge(5)).isEqualTo(12L);
            assertThat(child2.getIdByAge1()).isEqualTo(12L);
        }
    }

    // 测试数据类定义
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