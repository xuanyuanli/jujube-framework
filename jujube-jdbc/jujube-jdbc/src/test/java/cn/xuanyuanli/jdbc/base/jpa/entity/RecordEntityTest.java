package cn.xuanyuanli.jdbc.base.jpa.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("RecordEntity 测试")
class RecordEntityTest {

    @Nested
    @DisplayName("构造函数 RecordEntity(Map<String, Object> map)")
    class ConstructorWithMapTest {

        @ParameterizedTest
        @MethodSource("provideMapData")
        @DisplayName("使用 Map 构造 RecordEntity")
        void testConstructorWithMap(Map<String, Object> inputMap, Map<String, Object> expectedMap) {
            RecordEntity recordEntity = new RecordEntity(inputMap);
            assertThat(recordEntity).isNotNull();
            assertThat(recordEntity).isEqualTo(expectedMap);
        }

        private static Stream<Arguments> provideMapData() {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("key1", "value1");
            map1.put("key2", 123);

            Map<String, Object> map2 = new HashMap<>(); // 空Map

            return Stream.of(
                    Arguments.of(map1, map1),
                    Arguments.of(map2, map2),
                    Arguments.of(null, new Record()) // null Map
            );
        }
    }


    @Nested
    @DisplayName("无参构造函数 RecordEntity()")
    class NoArgsConstructorTest {
        @Test
        @DisplayName("无参构造函数")
        void testNoArgsConstructor() {
            RecordEntity recordEntity = new RecordEntity();
            assertThat(recordEntity).isNotNull();
            assertThat(recordEntity).isEmpty();
        }
    }

    @Test
    @DisplayName("测试 serialVersionUID")
    void testSerialVersionUID() {
        assertThat(RecordEntity.class).hasDeclaredFields("serialVersionUID");
    }
}
