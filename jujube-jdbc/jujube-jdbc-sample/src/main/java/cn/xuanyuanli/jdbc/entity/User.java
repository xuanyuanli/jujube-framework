package cn.xuanyuanli.jdbc.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import cn.xuanyuanli.jdbc.base.annotation.Column;
import cn.xuanyuanli.core.lang.BaseEntity;

/**
 * @author xuanyuanli
 */
@Data
@Accessors(chain = true)
public class User implements BaseEntity {
    private Long id;
    private String name;
    private Integer age;
    private Long departmentId;
    @Column("f_info_id_")
    private Integer fInfoId;
    private Integer sex;
}
