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
 * @author John Li
 */
public class JavassistBeanCopierFactory implements BeanCopierFactory {

    private static final Logger logger = LoggerFactory.getLogger(JavassistBeanCopierFactory.class);

    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    private final static ClassPool CLASS_POOL = ClassPool.getDefault();

    static {
        ClassClassPath classPath = new ClassClassPath(JavassistBeanCopierFactory.class);
        CLASS_POOL.insertClassPath(classPath);
    }

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
     * 获得构造的方法内容
     * <h3>javaassist使用</h3>
     * <ul>
     *     <li>基本类型的处理</li>
     *     <ul>
     *         <li>对于获取值来说，要先转换为包装类，方便后续处理。如果get方法返回值类型是primitive，用$w来转换为包装类</li>
     *         <li>对于写入值来说：如果set方法的参数类型是primitive，则使用BeanCopier的getPrimitive系列方法获取primitive值；如果是包装类，则使用Beans.getExpectTypeValue()获得对应类型的值</li>
     *     </ul>
     *     <li>实参获取：使用$1、$2、$3来获取</li>
     * </ul>
     *
     * @param sourceClass 源类
     * @param targetClass 目标类
     * @param items       项
     * @param cover       覆盖
     * @return {@link String}
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
