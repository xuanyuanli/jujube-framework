package cn.xuanyuanli.jdbc.entity;

import cn.xuanyuanli.core.lang.BaseEntity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xuanyuanli
 */
@Data
@Accessors(chain = true)
public class Department implements BaseEntity {
    private Integer id;
    private String name;
}
