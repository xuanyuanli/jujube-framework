package cn.xuanyuanli.core.util;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpElsTest {

    @Test
    public void parseMap() {
        Map<String, Object> root = new HashMap<>(1);
        root.put("map", Collections3.newHashMap("_id", 12));
        Integer result = SpEls.parse("{#map[_id]}", root, Integer.class);
        Assertions.assertThat(result).isEqualTo(12);
    }

    @Test
    public void parseEntity() {
        Response response = new Response();
        response.setIndex(1);
        Map<String, Object> root = new HashMap<>(1);
        root.put("response", response);
        Integer result = SpEls.parse("{#response.index}", root, Integer.class);
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Data
    static class Response {
        private Integer index;
    }
}
