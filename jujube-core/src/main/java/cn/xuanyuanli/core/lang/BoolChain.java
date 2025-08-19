package cn.xuanyuanli.core.lang;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 关于bool判断的链式执行器
 *
 * @author John Li
 * @date 2021/09/01
 */
public class BoolChain<T> {

    /**
     * 值
     */
    private final T value;

    /**
     * 是否相等
     */
    private boolean isEq;

    /**
     * bool链
     */
    private BoolChain() {
        this.value = null;
    }

    /**
     * bool链
     *
     * @param value 价值
     */
    private BoolChain(T value) {
        this.value = value;
    }

    /**
     * 空
     */
    private static final BoolChain<?> EMPTY = new BoolChain<>();

    /**
     * 初始化一个值到判断链中，可为null
     *
     * @param t t
     * @return {@link BoolChain}<{@link T}>
     * @param <T> 泛型
     */
    public static <T> BoolChain<T> of(T t) {
        if (t == null) {
            return (BoolChain<T>) EMPTY;
        }
        return new BoolChain<>(t);
    }

    /**
     * 判断值是否等于第一个参数，如果等于，则执行第二个参数的逻辑
     *
     * @param t            t
     * @param voidFunction 无效函数
     * @return {@link BoolChain}<{@link T}>
     */
    public BoolChain<T> eqThen(T t, VoidFunction voidFunction) {
        if (!isEq && Objects.equals(t, value)) {
            voidFunction.exec();
            isEq = true;
        }
        return this;
    }

    /**
     * 否则
     *
     * @param voidFunction 无效函数
     */
    public void orElse(VoidFunction voidFunction) {
        if (!isEq) {
            voidFunction.exec();
        }
    }

    /**
     * 然后
     *
     * @param predicate    谓词
     * @param voidFunction 无效函数
     * @return {@link BoolChain}<{@link T}>
     */
    public BoolChain<T> then(Predicate<T> predicate, VoidFunction voidFunction) {
        if (predicate.test(value)) {
            voidFunction.exec();
        }
        return this;
    }
}
