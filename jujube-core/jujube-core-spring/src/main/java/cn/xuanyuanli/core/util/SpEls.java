package cn.xuanyuanli.core.util;

import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring表达式语言(SpEL,Spring Expression Language)工具类，提供简单易用的SpEL解析功能。
 * <p>
 * 该工具类封装了Spring的SpEL解析器，支持将包含SpEL表达式的文本内容解析为指定类型的结果。
 * 主要用于动态表达式计算、模板解析等场景。
 * <p>
 * 支持的表达式格式：{#变量名} 或 {表达式}
 * <p>
 * 使用示例：
 * <pre>{@code
 * Map<String, Object> variables = new HashMap<>();
 * variables.put("name", "张三");
 * variables.put("age", 25);
 * 
 * String result = SpEls.parse("你好，{#name}！你今年{#age}岁了。", variables, String.class);
 * // 结果：你好，张三！你今年25岁了。
 * }</pre>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class SpEls {

    /**
     * 私有构造方法，防止实例化工具类
     */
    private SpEls() {
    }

    /**
     * 使用SpEL表达式解析文本内容，并返回指定类型的结果。
     * <p>
     * 该方法将包含SpEL表达式的文本内容解析为指定类型的对象。文本中的表达式格式为 {#变量名} 或 {表达式}。
     * 表达式中可以引用通过 root 参数传入的变量，支持复杂的表达式计算。
     * <p>
     * 使用示例：
     * <pre>{@code
     * Map<String, Object> variables = new HashMap<>();
     * variables.put("user", "李四");
     * variables.put("score", 95);
     * 
     * // 简单变量替换
     * String text1 = SpEls.parse("学生{#user}的成绩是{#score}分", variables, String.class);
     * 
     * // 表达式计算
     * String text2 = SpEls.parse("成绩等级：{#score >= 90 ? '优秀' : '良好'}", variables, String.class);
     * }</pre>
     *
     * @param content 包含SpEL表达式的文本内容，不能为空
     * @param root    SpEL表达式中可以引用的变量集合，变量名作为键，变量值作为值
     * @param clazz   期望返回的数据类型，用于类型转换
     * @param <T>     返回值的泛型类型
     * @return 解析后的结果，类型为指定的 clazz 类型
     * @throws IllegalArgumentException 如果 content 参数为空或空白字符串
     * @throws org.springframework.expression.ExpressionException 如果表达式解析或计算过程中发生错误
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
     * SpEL 解析器上下文配置，用于定义表达式的模板格式。
     * <p>
     * 配置说明：
     * <ul>
     *   <li>isTemplate() = true：启用模板模式</li>
     *   <li>getExpressionPrefix() = "{"：表达式前缀为左花括号</li>
     *   <li>getExpressionSuffix() = "}"：表达式后缀为右花括号</li>
     * </ul>
     * 因此完整的表达式格式为：{#变量名} 或 {表达式}
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
