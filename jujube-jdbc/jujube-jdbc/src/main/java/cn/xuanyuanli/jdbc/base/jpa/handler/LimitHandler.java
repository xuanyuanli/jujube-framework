package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * @author xuanyuanli
 */
public class LimitHandler implements Handler {

    private static final String LIMIT_D = "Limit(\\d+)$";
    private static final String LIMIT = "Limit";

    /**
     * 清除
     *
     * @param methodName 方法名称
     * @return {@link String}
     */
    public static String clear(String methodName) {
        return methodName.substring(0, methodName.lastIndexOf(LIMIT));
    }

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String[] groups = Strings.getGroups(LIMIT_D, truncationMethodName);
        if (groups.length > 1) {
            int limit = NumberUtils.toInt(groups[1]);
            spec.limit(limit);
            truncationMethodName = clear(truncationMethodName);
        } else if (truncationMethodName.endsWith(LIMIT)) {
            int index = args.size() - 1;
            spec.limit((int) args.get(index));
            args.remove(index);
            truncationMethodName = clear(truncationMethodName);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }
}
