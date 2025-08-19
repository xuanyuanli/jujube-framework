package cn.xuanyuanli.jdbc.base.jpa;

import java.io.Serializable;
import lombok.Getter;
import cn.xuanyuanli.jdbc.base.BaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.entity.RecordEntity;
import cn.xuanyuanli.core.lang.BaseEntity;

/**
 * JPA Dao支持
 *
 * @author John Li
 */
@Getter
public class JpaBaseDaoSupport extends BaseDaoSupport<RecordEntity, Serializable> {

    /**
     * 真实的Entity类型
     */
    private final Class<? extends BaseEntity> originalRealGenericType;

    /**
     * jpa基地数据访问支持
     *
     * @param originalRealGenericType 原来真正泛型类型
     * @param realPrimayKeyType       真正primay键类型
     * @param tableName               表名
     */
    public JpaBaseDaoSupport(Class<? extends BaseEntity> originalRealGenericType, Class<? extends Serializable> realPrimayKeyType, String tableName) {
        super(RecordEntity.class, (Class<Serializable>) realPrimayKeyType, tableName);
        this.originalRealGenericType = originalRealGenericType;
    }

}
