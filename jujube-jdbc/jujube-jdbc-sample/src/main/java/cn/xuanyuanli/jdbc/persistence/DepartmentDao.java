package cn.xuanyuanli.jdbc.persistence;

import java.math.BigInteger;
import java.util.List;
import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.entity.Department;

/**
 * @author John Li
 */
public interface DepartmentDao extends BaseDao<Department, BigInteger> {

    /**
     * 获得表名字
     *
     * @return {@link String}
     */
    @Override
    default String getTableName() {
        return "department";
    }

    /**
     * 根据查询条件获得 总数
     *
     * @param id    {@link Department#getId() id}
     * @return 总数
     */
    long getCountByIdGt(int id);

    /**
     * 根据查询条件获得对象集合
     *
     * @param name    {@link Department#getName() name}
     * @return {@link List}<{@link Long}>
     */
    List<Long> findIdByNameLike(String name);

    /**
     * 找到id，根据名称和id
     *
     * @param name 名字
     * @param ids  id
     * @return {@link List}<{@link Long}>
     */
    List<Long> findIdByNameLikeAndIdIn(String name,List<Long> ids);

    /**
     * 根据查询条件获得对象集合
     *
     * @param name    {@link Department#getName() name}
     * @param ids    {@link Department#getId() id}
     * @return {@link List}<{@link Long}>
     */
    List<Long> findIdByNameLikeAndIdNotIn(String name,List<Long> ids);
}
