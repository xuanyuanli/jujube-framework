package cn.xuanyuanli.core.util.beancopy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.xuanyuanli.core.util.Beans;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import cn.xuanyuanli.core.util.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Javassist字节码操作技术的Bean复制器工厂实现类
 * 
 * <p>该工厂使用Javassist动态生成Bean复制器类，通过直接生成字节码来实现高性能的Bean属性复制。
 * 相比反射方式，动态生成的复制器在运行时性能更优。</p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *     <li>动态生成Bean复制器类，避免反射调用开销</li>
 *     <li>支持基本类型和包装类型之间的自动转换</li>
 *     <li>支持Map类型作为源对象的属性复制</li>
 *     <li>支持覆盖模式和非覆盖模式的属性复制</li>
 *     <li>自动处理类型转换和null值检查</li>
 * </ul>
 * 
 * @author xuanyuanli
 */
public class JavassistBeanCopierFactory implements BeanCopierFactory {

    private static final Logger logger = LoggerFactory.getLogger(JavassistBeanCopierFactory.class);

    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    private final static ClassPool CLASS_POOL = ClassPool.getDefault();

    static {
        ClassClassPath classPath = new ClassClassPath(JavassistBeanCopierFactory.class);
        CLASS_POOL.insertClassPath(classPath);
    }

    /**
     * 创建Bean复制器实例
     * 
     * <p>使用Javassist动态生成实现了BeanCopier接口的类，该类包含优化的copyBean方法。
     * 生成的类将直接调用getter/setter方法，避免反射调用的性能开销。</p>
     * 
     * <p>生成的类名格式：BeanCopier$$Javassist_源类名_目标类名_序列号</p>
     * 
     * @param sourceClass 源对象的类型
     * @param targetClass 目标对象的类型
     * @param items 需要复制的属性项列表，包含源属性到目标属性的映射关系
     * @param cover 是否覆盖目标对象中已存在的非null值
     * @return 生成的Bean复制器实例，如果生成失败则返回null
     */
    @Override
    public BeanCopier createBeanCopier(Class<?> sourceClass, Class<?> targetClass, List<BeanCopyPropertyItem> items, boolean cover) {
        String className = BeanCopier.class.getName() + "$$Javassist_" + sourceClass.getSimpleName() + "_" + targetClass.getSimpleName() + "_"
                + INSTANCE_COUNT.incrementAndGet();
        try {
            CtClass beanCopyInterface = CLASS_POOL.get(BeanCopier.class.getName());

            CtClass beanCopyCtClass = CLASS_POOL.makeClass(className);

            CtConstructor constructor = CtNewConstructor.defaultConstructor(beanCopyCtClass);
            beanCopyCtClass.addConstructor(constructor);

            CtClass objectClass = CLASS_POOL.get(Object.class.getName());
            String methodBody = getCopyBeanMethodBody(sourceClass, targetClass, items, cover);
            CtMethod copyBeanMethod = CtNewMethod.make(objectClass, "copyBean",
                    new CtClass[]{objectClass, objectClass, CLASS_POOL.get(boolean.class.getName())}, null, methodBody, beanCopyCtClass);
            beanCopyCtClass.addMethod(copyBeanMethod);

            beanCopyCtClass.addInterface(beanCopyInterface);

            @SuppressWarnings("unchecked")
            Class<BeanCopier> classBeanCopy = (Class<BeanCopier>) beanCopyCtClass.toClass(BeanCopier.class);
            beanCopyCtClass.detach();
            return classBeanCopy.getConstructor().newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 生成copyBean方法的Java源码字符串
     * 
     * <p>该方法根据属性映射关系动态构建copyBean方法的实现代码。生成的方法会：</p>
     * <ul>
     *     <li>将第一个参数转换为源对象类型</li>
     *     <li>根据第三个参数决定是创建新的目标对象还是使用现有对象</li>
     *     <li>遍历所有属性项，生成相应的getter/setter调用代码</li>
     *     <li>处理基本类型和包装类型之间的转换</li>
     *     <li>根据cover参数决定是否进行null值检查</li>
     * </ul>
     * 
     * <h3>Javassist语法说明：</h3>
     * <ul>
     *     <li><strong>参数获取：</strong>使用$1、$2、$3等获取方法参数</li>
     *     <li><strong>基本类型处理：</strong></li>
     *     <ul>
     *         <li>获取值时：如果getter返回基本类型，使用$w转换为包装类</li>
     *         <li>设置值时：如果setter参数是基本类型，使用BeanCopier的getPrimitive系列方法；否则使用Beans.getExpectTypeValue()进行类型转换</li>
     *     </ul>
     *     <li><strong>Map源对象：</strong>直接调用get方法获取值，无需$w转换</li>
     * </ul>
     *
     * @param sourceClass 源对象的类型
     * @param targetClass 目标对象的类型
     * @param items 属性复制项列表，包含源属性到目标属性的映射关系
     * @param cover 是否覆盖目标对象中已存在的非null值
     * @return 生成的copyBean方法体Java源码字符串
     */
    public String getCopyBeanMethodBody(Class<?> sourceClass, Class<?> targetClass, List<BeanCopyPropertyItem> items, boolean cover) {
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{\n");
        methodBody.append(sourceClass.getName()).append(" source = (").append(sourceClass.getName()).append(") $1;\n");
        methodBody.append(targetClass.getName()).append(" target = $3 ? new ").append(targetClass.getName()).append("() : (").append(targetClass.getName())
                .append(") $2;\n");

        String beanCopierName = BeanCopier.class.getName();
        String beansName = Beans.class.getName();
        for (BeanCopyPropertyItem item : items) {
            String sourceGet;
            Class<?> writeType = item.getTargetProperty().getWriteMethod().getParameterTypes()[0];
            if (item.isSourceIsMap()) {
                // map中的值默认会转换为包装类，所以此处不用$w转换
                sourceGet = "source.get(\"" + item.getSourcePropertyName() + "\")";
            } else {
                Method readMethod = item.getSourceProperty().getReadMethod();
                sourceGet = (readMethod.getReturnType().isPrimitive() ? "($w)" : "") + "source." + readMethod.getName() + "()";
            }
            StringBuilder setVal = new StringBuilder();
            if (writeType.isPrimitive()) {
                setVal.append("target.").append(item.getTargetProperty().getWriteMethod().getName())
                        .append("(").append(beanCopierName).append(".get")
                        .append(Texts.capitalize(writeType.getName())).append("(").append(sourceGet)
                        .append(")").append(");\n");
            } else {
                setVal.append("target.").append(item.getTargetProperty().getWriteMethod().getName())
                        .append("(").append("(").append(writeType.getName())
                        .append(")").append(beansName).append(".getExpectTypeValue(")
                        .append(sourceGet).append(",").append(writeType.getName())
                        .append(".class));\n");
            }
            if (cover) {
                methodBody.append(setVal);
            } else {
                String targetGet = "target." + item.getTargetProperty().getReadMethod().getName() + "()";
                methodBody.append("if(").append(targetGet).append("==null){").append(setVal).append("}\n");
            }
        }
        methodBody.append("return target;\n}\n");
        return methodBody.toString();
    }

}
