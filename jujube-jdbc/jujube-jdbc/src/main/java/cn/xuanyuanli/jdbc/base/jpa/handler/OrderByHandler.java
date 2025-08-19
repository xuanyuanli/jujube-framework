package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * @author John Li
 */
public class OrderByHandler implements Handler {

    /**
     * OrderBy
     */
    public static final String ORDER_BY = "OrderBy";
    private static final String DESC = "Desc";
    private static final String ASC = "Asc";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.contains(ORDER_BY)) {
            int index = truncationMethodName.indexOf(ORDER_BY);
            String mname = truncationMethodName.substring(index + ORDER_BY.length());
            String[] sarr = Strings.splitByAnd(mname);
            for (String field : sarr) {
                processField(method, spec, field);
            }
            truncationMethodName = truncationMethodName.substring(0, index);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }

    private void processField(DaoMethod method, Spec spec, String field) {
        if (field.endsWith(DESC)) {
            field = getDbColumn(method, field.substring(0, field.length() - DESC.length()));
            spec.sort().desc(field);
        } else if (field.endsWith(ASC)) {
            spec.sort().asc(getDbColumn(method, field.substring(0, field.length() - ASC.length())));
        } else {
            spec.sort().asc(getDbColumn(method, field));
        }
    }

}
