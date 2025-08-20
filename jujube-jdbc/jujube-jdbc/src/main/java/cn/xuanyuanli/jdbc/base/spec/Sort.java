package cn.xuanyuanli.jdbc.base.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * 排序规则<br> 排序的构建规则是：如果排序字段以“_D”结尾，则倒序；否则，就是正序
 *
 * @author xuanyuanli
 * @date 2022/07/16
 */
public class Sort {

    /**
     * 排序
     */
    private List<String> sorts = new ArrayList<>();
    /**
     * 不计入排序的字段
     */
    public static final String DEFAULT = "default";
    /**
     * 倒序后缀
     */
    private static final String DESC_SUFFIX = "_D";

    /**
     * 规范
     */
    private final Spec spec;

    /**
     * 排序
     *
     * @param spec 规范
     */
    public Sort(Spec spec) {
        super();
        this.spec = spec;
    }

    /**
     * 正序
     *
     * @param field 场
     * @return {@link Sort}
     */
    public Sort asc(String field) {
        if (!field.equals(DEFAULT) && !sorts.contains(field)) {
            sorts.add(field);
        }
        return this;
    }

    /**
     * 倒序
     *
     * @param field 场
     * @return {@link Sort}
     */
    public Sort desc(String field) {
        String sort = field + DESC_SUFFIX;
        if (!field.equals(DEFAULT) && !sorts.contains(sort)) {
            sorts.add(sort);
        }
        return this;
    }

    /**
     * 结束
     *
     * @return {@link Spec}
     */
    public Spec end() {
        return spec;
    }

    /**
     * 构建联合查询排序规则
     *
     * @return {@link String}
     */
    public String buildSqlSort() {
        if (sorts.size() != 0) {
            StringBuilder sql = new StringBuilder(" order by ");
            // 排序的构建规则是：如果排序字段以“_D”结尾，则倒序；否则，就是正序
            for (String s : sorts) {
                if (s.endsWith(DESC_SUFFIX)) {
                    sql.append(removeSuffix(s)).append(" desc,");
                } else {
                    sql.append(s).append(",");
                }
            }
            return sql.substring(0, sql.length() - 1);
        }
        return "";
    }

    /**
     * 删除后缀
     *
     * @param field 场
     * @return {@link String}
     */
    private String removeSuffix(String field) {
        return field.substring(0, field.length() - 2);
    }

    /**
     * 清洁值
     */
    public void cleanValues() {
        sorts.clear();
    }

    /**
     * 是否空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return sorts.isEmpty();
    }

    /**
     * 克隆
     *
     * @param spec 规范
     * @return {@link Sort}
     */
    public Sort clone(Spec spec) {
        Sort sort = new Sort(spec);
        sort.sorts = new ArrayList<>(this.sorts);
        return sort;
    }

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return "Sort [" + buildSqlSort() + "]";
    }

    /**
     * 散列码
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sorts == null) ? 0 : sorts.hashCode());
        return result;
    }

    /**
     * =
     *
     * @param obj obj
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Sort other = (Sort) obj;
        if (sorts == null && other.sorts != null) {
            return false;
        }
        if (sorts != null) {
            return sorts.equals(other.sorts);
        }
        return false;
    }

}
