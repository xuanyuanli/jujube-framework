package cn.xuanyuanli.jdbc.pagination;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Pojos;

/**
 * 分页中间类
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2023/04/25
 */
@Setter
public class Pageable<T> implements Iterable<T>, BaseEntity {

    /**
     * 串行版本uid
     */
    @Serial
    private static final long serialVersionUID = -566814709144497590L;

    /**
     * 数据
     */
    @Getter
    private List<T> data;
    /**
     * 总条数
     */
    @Getter
    private long totalElements;
    /**
     * 每页显示多少条
     */
    private int size;
    /**
     * 当前页码
     */
    @Getter
    private int index;
    /**
     * 相当于limit begin（用于自定义，一般来说用不到）
     */
    private int start;
    /**
     * 默认每页显示条数
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * 分页
     *
     * @param index 指数
     * @param size  大小
     * @param start 开始
     */
    public Pageable(int index, int size, int start) {
        super();
        this.size = size;
        this.index = index;
        this.start = start;
    }

    /**
     * 分页
     *
     * @param index 指数
     * @param size  大小
     */
    public Pageable(int index, int size) {
        super();
        this.size = size;
        this.index = index;
    }

    /**
     * 分页
     *
     * @param index 指数
     */
    public Pageable(int index) {
        super();
        this.index = index;
        this.size = DEFAULT_SIZE;
    }

    /**
     * 分页
     */
    public Pageable() {
        super();
        this.index = 1;
        this.size = DEFAULT_SIZE;
    }

    /**
     * 获得总共多少页
     *
     * @return long
     */
    public long getTotalPages() {
        return (totalElements + size - 1) / size;
    }

    /**
     * 获得大小
     *
     * @return int
     */
    public int getSize() {
        return size < 1 ? DEFAULT_SIZE : size;
    }

    /**
     * 获得开始
     *
     * @return int
     */
    public int getStart() {
        if (start <= 0) {
            return (index - 1) * size;
        } else {
            return start;
        }
    }

    /**
     * 是否有前一页
     *
     * @return boolean
     */
    public boolean hasPreviousPage() {
        return index > 1;
    }

    /**
     * 是否是第一页
     *
     * @return boolean
     */
    public boolean isFirstPage() {
        return !hasPreviousPage();
    }

    /**
     * 是否 有后一页
     *
     * @return boolean
     */
    public boolean hasNextPage() {
        return index < getTotalPages();
    }

    /**
     * 是否为最后一页
     *
     * @return boolean
     */
    public boolean isLastPage() {
        return !hasNextPage();
    }

    /**
     * 迭代器
     *
     * @return {@link Iterator}<{@link T}>
     */
    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    /**
     * 转换Pageable的泛型为指定类型
     *
     * @param clazz clazz
     * @param <V>   泛型
     * @return Pageable<{ @ link V }>
     */
    public <V extends Serializable> Pageable<V> toGenericType(Class<V> clazz) {
        return toGenericType(f -> Pojos.mapping(f, clazz));
    }

    /**
     * 转换Pageable的泛型为指定类型
     *
     * @param func 函数
     * @param <V>  泛型
     * @return Pageable<{ @ link V }>
     */
    public <V extends Serializable> Pageable<V> toGenericType(Function<T, V> func) {
        Pageable<V> result = new Pageable<>();
        result.setTotalElements(this.getTotalElements());
        result.setSize(this.getSize());
        result.setIndex(this.getIndex());
        result.setStart(this.getStart());
        if (this.getData() != null) {
            result.setData(this.getData().stream().map(func).collect(Collectors.toList()));
        }
        return result;
    }
}
