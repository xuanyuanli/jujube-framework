package cn.xuanyuanli.jdbc.base.jpa.entity;

import java.io.Serial;
import java.util.Map;
import lombok.NoArgsConstructor;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.lang.Record;

/**
 * 此类即是Record也是BaseEntity,只限用于JpaQuery中，其他地方禁止使用
 *
 * @author xuanyuanli
 * @date 2022/07/16
 */
@NoArgsConstructor
public class RecordEntity extends Record implements BaseEntity {

    /**
     * 记录实体
     *
     * @param map map
     */
    public RecordEntity(Map<String, Object> map) {
        super(map);
    }

    /**
     * 串行版本uid
     */
    @Serial
    private static final long serialVersionUID = -5995445431564439259L;

}
