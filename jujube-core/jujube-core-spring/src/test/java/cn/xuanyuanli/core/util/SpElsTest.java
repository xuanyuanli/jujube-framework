package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Spring表达式语言工具类测试")
class SpElsTest {

    @Nested
    @DisplayName("表达式解析")
    class ParseExpression {

        @Test
        @DisplayName("应该返回Map中的值 - 当解析Map类型表达式时")
        void parse_shouldReturnMapValue_whenParsingMapExpression() {
            // Arrange
            Map<String, Object> root = new HashMap<>(1);
            root.put("map", Collections3.newHashMap("_id", 12));
            String expression = "{#map[_id]}";

            // Act
            Integer result = SpEls.parse(expression, root, Integer.class);

            // Assert
            assertThat(result).isEqualTo(12);
        }

        @Test
        @DisplayName("应该返回实体属性值 - 当解析实体对象表达式时")
        void parse_shouldReturnEntityPropertyValue_whenParsingEntityExpression() {
            // Arrange
            Response response = new Response();
            response.setIndex(1);
            Map<String, Object> root = new HashMap<>(1);
            root.put("response", response);
            String expression = "{#response.index}";

            // Act
            Integer result = SpEls.parse(expression, root, Integer.class);

            // Assert
            assertThat(result).isEqualTo(1);
        }
    }

    @Data
    static class Response {
        private Integer index;
    }
}
