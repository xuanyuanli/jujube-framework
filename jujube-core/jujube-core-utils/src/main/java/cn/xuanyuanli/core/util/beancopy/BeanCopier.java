package cn.xuanyuanli.core.util.beancopy;

import cn.xuanyuanli.core.util.Beans;

/**
 * Bean复制者
 * <h3>方法说明</h3>
 * <ul>
 *     <li>get*()系列方法作用：把其他类型转换为正确的primitive类型</li>
 *     <li>copyBean()方法作用：把sourceObject的属性复制到targetObject中</li></li>
 * </ul>
 *
 * @author xuanyuanli
 */
public interface BeanCopier {

    /**
     * 将源对象的属性复制到目标对象的属性。
     *
     * @param sourceObject  要从中复制属性的源对象。
     * @param target        要复制属性的目标类或对象。
     * @param targetIsClass 目标是否为类，如果为类，则需要new初始化；为对象，则直接使用。
     * @return targetObject 返回目标对象。
     */
    Object copyBean(Object sourceObject, Object target, boolean targetIsClass);

    /**
     * 获取布尔
     *
     * @param sourceVal 源值
     * @return boolean
     */
    static boolean getBoolean(Object sourceVal) {
        return Boolean.TRUE.equals(Beans.getExpectTypeValue(sourceVal, boolean.class));
    }

    /**
     * 获得字符
     *
     * @param sourceVal 源值
     * @return char
     */
    @SuppressWarnings("DataFlowIssue")
    static char getChar(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, char.class);
    }

    /**
     * 获得字节
     *
     * @param sourceVal 源值
     * @return byte
     */
    @SuppressWarnings("DataFlowIssue")
    static byte getByte(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, byte.class);
    }

    /**
     * 获得短
     *
     * @param sourceVal 源值
     * @return short
     */
    @SuppressWarnings("DataFlowIssue")
    static short getShort(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, short.class);
    }

    /**
     * 获得int
     *
     * @param sourceVal 源值
     * @return int
     */
    @SuppressWarnings("DataFlowIssue")
    static int getInt(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, int.class);
    }

    /**
     * 获得长
     *
     * @param sourceVal 源值
     * @return long
     */
    @SuppressWarnings("DataFlowIssue")
    static long getLong(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, long.class);
    }

    /**
     * 获得浮动
     *
     * @param sourceVal 源值
     * @return float
     */
    @SuppressWarnings("DataFlowIssue")
    static float getFloat(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, float.class);
    }

    /**
     * 获得浮动
     *
     * @param sourceVal 源值
     * @return double
     */
    @SuppressWarnings("DataFlowIssue")
    static double getDouble(Object sourceVal) {
        return Beans.getExpectTypeValue(sourceVal, double.class);
    }
}
