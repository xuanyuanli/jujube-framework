package cn.xuanyuanli.core.util;

import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring表达式语言(SpEL,Spring Expression Language)的一个简单封装 <br>
 * <br>
 * 格式为：{#value}
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class SpEls {

    /**
     * sp els
     */
    private SpEls() {
    }

    /**
     * 将文本内容通过SpELl解析为需要的内容
     *
     * @param content 文本
     * @param root    EvaluationContext的变量内容
     * @param clazz   要返回的数据类型
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T parse(String content, Map<String, Object> root, Class<T> clazz) {
        Validate.notBlank(content);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(root);
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(content, PARSER_CONTEXT);
        return expression.getValue(context, clazz);
    }

    /**
     * 解析器上下文
     */
    static final ParserContext PARSER_CONTEXT = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "{";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    };
}
