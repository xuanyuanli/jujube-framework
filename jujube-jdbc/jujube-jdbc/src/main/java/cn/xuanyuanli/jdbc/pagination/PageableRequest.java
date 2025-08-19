package cn.xuanyuanli.jdbc.pagination;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import cn.xuanyuanli.core.lang.BaseEntity;

/**
 * 分页请求
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2021/09/01
 */
public class PageableRequest implements BaseEntity {

    /**
     * 串行版本uid
     */
    @Serial
    private static final long serialVersionUID = -590137694303783744L;
    /**
     * 当前页码
     */
    @Getter
    private int index;
    /**
     * 大小
     */
    @Setter
    private int size;
    /**
     * 开始
     *
     */
    @Getter
    @Setter
    private int start;

    /**
     * 总条数。放置这个元素的目的是，如果在第一页查询出了totalElements，那么后面的页数中，就可以直接使用totalElements。 而不用再次查询总条数
     */
    @Setter
    @Getter
    private long totalElements;

    /**
     * 分页请求
     *
     * @param index 指数
     * @param size  大小
     */
    public PageableRequest(int index, int size) {
        super();
        setIndex(index);
        setSize(size);
    }

    /**
     * 分页请求
     */
    public PageableRequest() {
        super();
        this.index = 1;
        this.size = Pageable.DEFAULT_SIZE;
    }

    /**
     * 设置当前页码
     *
     * @param index 当前页码
     */
    public void setIndex(int index) {
        if (index <= 0) {
            index = 1;
        }
        this.index = index;
    }

    /**
     * 获得大小
     *
     * @return int
     */
    public int getSize() {
        return size < 1 ? Pageable.DEFAULT_SIZE : size;
    }

    /**
     * 新分页
     *
     * @return {@link Pageable}<{@link T}>
     *     @param <T> 泛型
     */
    public <T> Pageable<T> newPageable() {
        Pageable<T> ts = new Pageable<>(index, size, start);
        ts.setTotalElements(this.getTotalElements());
        return ts;
    }

    /**
     * 构建分页请求
     *
     * @param pageableRequest 分页请求
     * @return {@link PageableRequest}
     * @author John Li Email：jujubeframework@163.com
     */
    public static PageableRequest buildPageRequest(PageableRequest pageableRequest) {
        if (pageableRequest == null) {
            pageableRequest = new PageableRequest();
        }
        return pageableRequest;
    }

    /**
     * 构建分页请求
     *
     * @return {@link PageableRequest}
     */
    public static PageableRequest buildPageRequest() {
        return buildPageRequest(null);
    }
}
