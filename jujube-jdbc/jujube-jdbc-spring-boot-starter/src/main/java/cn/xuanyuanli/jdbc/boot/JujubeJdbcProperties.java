package cn.xuanyuanli.jdbc.boot;

import lombok.Data;

/**
 * @author John Li
 */
@Data
public class JujubeJdbcProperties {
    /**
     * Dao接口所在的package
     */
    private String basePackage;

    /**
     * 是否自动刷新Dao Sql的缓存。默认五秒扫描一次
     */
    private boolean autoRefreshSql;
    /** 刷新周期，单位为秒。默认值：5 */
    private Integer refreshSqlPeriod;
}
