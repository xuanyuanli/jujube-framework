package ${basePackage};

import base.jdbc.cn.xuanyuanli.BaseDao;
import ${entityPackage}.${className};

/**
 * ${tableComment}数据访问
 *
 * @author generator
 */
public interface ${className}Dao extends BaseDao<${className},${pk.type}> {

    /**
    * 获得表名字
    *
    * @return {@link String}
    */
    @Override
    default String getTableName() {
        return "${tableName}";
    }

}
