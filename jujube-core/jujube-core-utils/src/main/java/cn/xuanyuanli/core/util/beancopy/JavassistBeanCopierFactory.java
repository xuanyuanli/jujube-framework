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
 * 基于Javassist字节码操作技术的高性能Bean复制器工厂实现类
 * 
 * <p>该工厂通过Javassist动态字节码生成技术创建专用的Bean复制器类，实现了零反射调用的高性能对象属性复制。
 * 生成的复制器类在运行时直接调用getter/setter方法，性能比传统反射方式提升数倍，适用于高并发和性能敏感场景。</p>
 * 
 * <p><strong>核心技术特点：</strong></p>
 * <ul>
 *     <li><strong>字节码生成：</strong>运行时动态创建BeanCopier实现类，避免反射方法调用开销</li>
 *     <li><strong>类型智能转换：</strong>自动处理基本类型与包装类型间的双向转换，支持Number类型族转换</li>
 *     <li><strong>Map源对象支持：</strong>原生支持Map作为数据源，实现Map到Bean的高效映射</li>
 *     <li><strong>灵活复制策略：</strong>支持覆盖模式（强制覆盖）和保护模式（仅复制null值属性）</li>
 *     <li><strong>null值安全：</strong>内置null值检查机制，确保类型转换和赋值操作的安全性</li>
 *     <li><strong>内存优化：</strong>生成的类在使用完毕后自动从ClassPool中分离，避免内存泄漏</li>
 * </ul>
 * 
 * <p><strong>性能优势：</strong></p>
 * <ul>
 *     <li>相比反射方式性能提升3-5倍</li>
 *     <li>生成的字节码经过JVM优化，执行效率接近手工编写的复制代码</li>
 *     <li>类加载一次性开销，后续调用无额外性能损耗</li>
 * </ul>
 * 
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * JavassistBeanCopierFactory factory = new JavassistBeanCopierFactory();
 * 
 * // 准备属性映射关系
 * List<BeanCopyPropertyItem> items = Arrays.asList(
 *     new BeanCopyPropertyItem("name", userProperty, personProperty, false),
 *     new BeanCopyPropertyItem("age", userProperty, personProperty, false)
 * );
 * 
 * // 创建复制器（支持覆盖模式）
 * BeanCopier copier = factory.createBeanCopier(User.class, Person.class, items, true);
 * 
 * // 执行复制操作
 * User source = new User("张三", 25);
 * Person target = (Person) copier.copyBean(source, null, true);
 * 
 * // 也可以复制到已存在的对象
 * Person existingPerson = new Person();
 * copier.copyBean(source, existingPerson, false);
 * }</pre>
 * 
 * <p><strong>线程安全性：</strong>该工厂类是线程安全的，可以在多线程环境中安全使用。
 * 生成的BeanCopier实例也是线程安全的，可以被多个线程同时调用。</p>
 * 
 * <p><strong>注意事项：</strong></p>
 * <ul>
 *     <li>生成的类会被加载到JVM中，频繁创建不同类型的复制器可能导致Metaspace内存增长</li>
 *     <li>建议对常用的类型组合缓存BeanCopier实例，避免重复生成</li>
 *     <li>源对象和目标对象的属性必须具有标准的JavaBean getter/setter方法</li>
 * </ul>
 * 
 * @author xuanyuanli
 */
public class JavassistBeanCopierFactory implements BeanCopierFactory {

    /**
     * 日志记录器，用于记录字节码生成过程中的错误信息和调试信息
     */
    private static final Logger logger = LoggerFactory.getLogger(JavassistBeanCopierFactory.class);

    /**
     * 生成类的实例计数器，确保每个动态生成的BeanCopier类具有唯一的类名
     * <p>使用AtomicInteger保证多线程环境下的线程安全性，避免类名冲突</p>
     */
    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    /**
     * Javassist类池，用于管理和缓存CtClass对象
     * <p>ClassPool是Javassist的核心组件，负责：</p>
     * <ul>
     *     <li>加载和缓存已存在的类的CtClass表示</li>
     *     <li>创建新的CtClass对象用于动态类生成</li>
     *     <li>管理类路径，确保能够找到依赖的类</li>
     *     <li>提供类的字节码操作环境</li>
     * </ul>
     */
    private final static ClassPool CLASS_POOL = ClassPool.getDefault();

    /*
      静态初始化块，配置Javassist类池的类路径
      <p>添加当前类的类路径到ClassPool中，确保Javassist能够访问到相关的依赖类，
      包括BeanCopier接口、工具类等。这对于动态生成的类能够正确引用这些依赖至关重要。</p>
     */
    static {
        ClassClassPath classPath = new ClassClassPath(JavassistBeanCopierFactory.class);
        CLASS_POOL.insertClassPath(classPath);
    }

    /**
     * 创建专用的高性能Bean复制器实例
     * 
     * <p>该方法通过Javassist字节码操作库动态生成一个实现BeanCopier接口的专用类，
     * 该生成类包含针对特定源类型和目标类型优化的copyBean方法实现。生成的方法体直接调用
     * 相应的getter和setter方法，完全避免了反射调用的性能开销。</p>
     * 
     * <p><strong>生成过程详解：</strong></p>
     * <ol>
     *     <li>创建唯一的类名（格式：BeanCopier$$Javassist_源类名_目标类名_序列号）</li>
     *     <li>使用ClassPool创建新的CtClass对象</li>
     *     <li>添加默认无参构造函数</li>
     *     <li>生成copyBean方法的Java源码并编译为字节码</li>
     *     <li>添加BeanCopier接口实现</li>
     *     <li>将CtClass转换为真实的Class对象</li>
     *     <li>创建并返回该Class的实例</li>
     *     <li>从ClassPool中分离CtClass以释放内存</li>
     * </ol>
     * 
     * <p><strong>性能特征：</strong></p>
     * <ul>
     *     <li>首次创建有一定开销（字节码生成和类加载），后续使用性能极高</li>
     *     <li>生成的复制器执行速度接近手工编写的复制代码</li>
     *     <li>适合对同一类型组合进行大量重复复制操作的场景</li>
     * </ul>
     * 
     * <p><strong>异常处理：</strong></p>
     * <ul>
     *     <li>如果字节码生成过程中发生异常，会记录错误日志并返回null</li>
     *     <li>常见异常原因：类路径问题、源/目标类无法访问、属性映射错误等</li>
     *     <li>调用者应检查返回值是否为null，并准备备用的复制策略</li>
     * </ul>
     * 
     * @param sourceClass 源对象的Class类型，不能为null，必须是具有标准getter方法的JavaBean类
     * @param targetClass 目标对象的Class类型，不能为null，必须是具有标准setter方法的JavaBean类或具有无参构造函数
     * @param items 属性复制项列表，包含源属性到目标属性的映射关系、类型转换信息等，不能为null或空集合
     * @param cover 复制策略标志位：{@code true}表示覆盖模式（强制覆盖目标对象的所有对应属性），
     *              {@code false}表示保护模式（仅当目标对象对应属性为null时才进行复制）
     * @return 动态生成的Bean复制器实例，实现了BeanCopier接口，可直接调用copyBean方法进行高性能复制；
     *         如果生成过程中发生异常则返回{@code null}，调用者需要检查并处理这种情况
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

            Class<BeanCopier> classBeanCopy = (Class<BeanCopier>) beanCopyCtClass.toClass(BeanCopier.class);
            beanCopyCtClass.detach();
            return classBeanCopy.getConstructor().newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 生成优化的copyBean方法Java源码字符串
     * 
     * <p>这是该工厂类的核心方法，负责根据给定的属性映射关系动态构建高效的copyBean方法实现源码。
     * 生成的源码将被Javassist编译为字节码，从而创建出针对特定类型组合优化的复制器。</p>
     * 
     * <p><strong>生成的方法结构：</strong></p>
     * <ol>
     *     <li><strong>参数处理：</strong>将Object类型的参数强制转换为具体的源类型和目标类型</li>
     *     <li><strong>目标对象准备：</strong>根据第三个布尔参数决定创建新实例还是使用传入的现有对象</li>
     *     <li><strong>属性复制循环：</strong>为每个属性项生成对应的getter/setter调用代码</li>
     *     <li><strong>类型转换：</strong>自动处理基本类型、包装类型和复杂对象类型之间的转换</li>
     *     <li><strong>条件复制：</strong>根据cover标志决定是否添加null值检查逻辑</li>
     *     <li><strong>返回目标对象：</strong>返回完成复制操作的目标对象</li>
     * </ol>
     * 
     * <p><strong>Javassist特殊语法详解：</strong></p>
     * <dl>
     *     <dt><strong>参数引用：</strong></dt>
     *     <dd>使用$1、$2、$3等特殊标识符引用方法参数，$1表示第一个参数，以此类推</dd>
     *     
     *     <dt><strong>基本类型装箱（$w）：</strong></dt>
     *     <dd>当getter方法返回基本类型时，使用($w)前缀自动转换为对应的包装类型，
     *     这样可以统一处理null值和类型转换逻辑</dd>
     *     
     *     <dt><strong>基本类型拆箱处理：</strong></dt>
     *     <dd>当setter方法的参数是基本类型时，调用BeanCopier工具类的getPrimitive系列方法
     *     （如getInt、getLong等）进行安全的类型转换和null值处理</dd>
     *     
     *     <dt><strong>对象类型转换：</strong></dt>
     *     <dd>对于非基本类型的setter参数，使用Beans.getExpectTypeValue()方法进行智能类型转换，
     *     支持Number族类型转换、String转换等</dd>
     *     
     *     <dt><strong>Map源对象特殊处理：</strong></dt>
     *     <dd>当源对象是Map类型时，直接调用get(key)方法获取值，Map中的值已经是包装类型，
     *     无需使用$w转换</dd>
     * </dl>
     * 
     * <p><strong>生成代码示例：</strong></p>
     * <pre>{@code
     * // 生成的方法体可能如下所示：
     * {
     *     com.example.User source = (com.example.User) $1;
     *     com.example.Person target = $3 ? new com.example.Person() : (com.example.Person) $2;
     *     
     *     // 覆盖模式的属性复制
     *     target.setName((java.lang.String) cn.xuanyuanli.core.util.Beans.getExpectTypeValue(
     *         ($w) source.getName(), java.lang.String.class));
     *         
     *     // 基本类型处理
     *     target.setAge(cn.xuanyuanli.core.util.beancopy.BeanCopier.getInt(($w) source.getAge()));
     *     
     *     // 非覆盖模式的null检查
     *     if(target.getEmail() == null) {
     *         target.setEmail((java.lang.String) cn.xuanyuanli.core.util.Beans.getExpectTypeValue(
     *             ($w) source.getEmail(), java.lang.String.class));
     *     }
     *     
     *     return target;
     * }
     * }</pre>
     * 
     * <p><strong>性能优化策略：</strong></p>
     * <ul>
     *     <li>避免不必要的类型检查，直接进行强制类型转换</li>
     *     <li>将重复使用的类名存储在变量中，减少字符串拼接开销</li>
     *     <li>根据cover参数预先决定是否生成null检查代码，避免运行时判断</li>
     *     <li>使用StringBuilder进行高效的字符串构建</li>
     * </ul>
     *
     * @param sourceClass 源对象的Class类型，用于生成类型转换代码和方法调用代码
     * @param targetClass 目标对象的Class类型，用于生成实例化代码和setter调用代码  
     * @param items 属性复制项列表，每个项包含源属性到目标属性的完整映射信息，
     *              包括属性描述器、是否为Map源等元数据
     * @param cover 复制策略：{@code true}表示生成覆盖式复制代码（无条件赋值），
     *              {@code false}表示生成保护式复制代码（添加null值检查条件）
     * @return 完整的copyBean方法体Java源码字符串，包含方法签名、局部变量声明、
     *         属性复制逻辑和返回语句，可直接用于Javassist的CtNewMethod.make()方法
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
