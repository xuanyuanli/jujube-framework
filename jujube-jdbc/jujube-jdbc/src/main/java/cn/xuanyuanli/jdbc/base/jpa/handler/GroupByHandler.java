package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * @author John Li
 */
public class GroupByHandler implements Handler {

    /**
     * GroupBy
     */
    public static final String GROUP_BY = "GroupBy";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.contains(GROUP_BY)) {
            int index = truncationMethodName.indexOf(GROUP_BY);
            String mname = truncationMethodName.substring(index + GROUP_BY.length());
            String[] sarr = Strings.splitByAnd(mname);
            List<String> groups = new ArrayList<>();
            for (String field : sarr) {
                field = getDbColumn(method, field);
                groups.add(field);
            }
            spec.groupBy(StringUtils.join(groups, ","));
            truncationMethodName = truncationMethodName.substring(0, index);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }

}
