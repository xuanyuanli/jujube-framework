package cn.xuanyuanli.jdbc.binding.fmtmethod;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xuanyuanli
 */
public class JoinMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleSequence sequence = (SimpleSequence) arguments.get(0);
        SimpleScalar str = (SimpleScalar) arguments.get(1);
        List<?> list = sequence.toList();
        String separator = str.getAsString();
        String collect = "";
        if (!list.isEmpty()) {
            if (list.get(0) instanceof String) {
                Stream<String> stream = list.stream().map(e -> "'" +  e + "'");
                collect = stream.collect(Collectors.joining(","));
            } else {
                collect = StringUtils.join(list.toArray(), separator);
            }
        }
        return collect;
    }
}
