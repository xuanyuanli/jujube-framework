package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JSON工具类测试
 *
 * @author xuanyuanli
 */
@DisplayName("JSON工具类测试")
@SuppressWarnings("DataFlowIssue")
public class JsonsTest {

    @Nested
    @DisplayName("parseJson方法测试 - JSON字符串解析为Java对象")
    class ParseJsonTests {

        @Test
        @DisplayName("解析正常双引号JSON字符串为对象")
        void parseJson_shouldReturnCorrectObject_whenJsonWithDoubleQuotes() {
            // Arrange
            String json = "{\"id\":null,\"_widget_1513666823960\":\"90\"}";

            // Act
            IdAndName result = Jsons.parseJson(json, IdAndName.class);

            // Assert
            assertThat(result.getName()).isEqualTo("90");
            assertThat(result.getId()).isNull();
        }

        @Test
        @DisplayName("解析单引号JSON字符串为对象")
        void parseJson_shouldReturnCorrectObject_whenJsonWithSingleQuotes() {
            // Arrange
            String json = "{'id':null,'_widget_1513666823960':'90'}";

            // Act
            IdAndName result = Jsons.parseJson(json, IdAndName.class);

            // Assert
            assertThat(result.getName()).isEqualTo("90");
            assertThat(result.getId()).isNull();
        }

        @Test
        @DisplayName("解析带JsonProperty注解的JSON字符串")
        void parseJson_shouldReturnCorrectObject_whenJsonWithJsonProperty() {
            // Arrange
            String json = "{\"RequestId\":\"6F5CFFB8-66D6-454C-8D4F-233343364DBA\",\"Message\":\"子账号没有权限\",\"Code\":\"10009\"}";

            // Act
            Response result = Jsons.parseJson(json, Response.class);

            // Assert
            assertThat(result.getRequestId()).isEqualTo("6F5CFFB8-66D6-454C-8D4F-233343364DBA");
            assertThat(result.getMessage()).isEqualTo("子账号没有权限");
            assertThat(result.getCode()).isEqualTo(10009);
        }

        @Test
        @DisplayName("解析基本类型JSON字符串")
        void parseJson_shouldReturnCorrectValue_whenPrimitiveJson() {
            // Arrange & Act
            String result1 = Jsons.parseJson("1", String.class);
            String result2 = Jsons.parseJson("1", (Type) String.class);

            // Assert
            assertThat(result1).isEqualTo("1");
            assertThat(result2).isEqualTo("1");
        }

        @Test
        @DisplayName("解析复杂嵌套对象JSON字符串")
        void parseJson_shouldReturnCorrectObject_whenComplexJson() {
            // Arrange
            String json = "{\"_widget_1541348776165\":{\"value\":\"abc\"}}";
            NameBean expected = new NameBean().setName(NameBean.NameValue.value("abc"));

            // Act
            NameBean result = Jsons.parseJson(json, NameBean.class);

            // Assert
            assertThat(Jsons.toJson(expected)).isEqualTo(json);
            assertThat(result.getName().getValue()).isEqualTo("abc");
        }

        @Test
        @DisplayName("使用TypeReference解析JSON数组")
        void parseJson_shouldReturnCorrectList_whenUsingTypeReference() {
            // Arrange
            @SuppressWarnings("Convert2Diamond")
            TypeReference<List<Long>> typeRef = new TypeReference<List<Long>>() {};

            // Act
            List<Long> result = Jsons.parseJson("[1,2,3]", typeRef);

            // Assert
            assertThat(result).containsSequence(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("toJson方法测试 - Java对象转换为JSON字符串")
    class ToJsonTests {

        @Test
        @DisplayName("转换基本类型为JSON字符串")
        void toJson_shouldReturnQuotedString_whenPrimitiveValue() {
            // Arrange
            String input = "1";

            // Act
            String result = Jsons.toJson(input);

            // Assert
            assertThat(result).isEqualTo("\"1\"");
        }

        @Test
        @DisplayName("转换普通对象为JSON字符串")
        void toJson_shouldReturnCorrectJson_whenNormalObject() {
            // Arrange
            IdAndName obj = new IdAndName().setId(1L).setName("jack");

            // Act & Assert
            assertThat(Jsons.toJson(null)).isNull();
            assertThat(Jsons.toJson(obj)).isEqualTo("{\"id\":1,\"_widget_1513666823960\":\"jack\"}");
        }

        @Test
        @DisplayName("转换对象为格式化JSON字符串")
        void toPrettyJson_shouldReturnFormattedJson_whenNormalObject() {
            // Arrange
            IdAndName obj = new IdAndName().setId(1L).setName("jack");
            String expected = """
                {
                  "id" : 1,
                  "_widget_1513666823960" : "jack"
                }""";

            // Act & Assert
            assertThat(Jsons.toPrettyJson(null)).isNull();
            assertThat(Jsons.toPrettyJson(obj).replace("\r", "")).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("parseJsonToList方法测试 - 解析JSON数组为List")
    class ParseJsonToListTests {

        @Test
        @DisplayName("解析JSON数组为Long类型List")
        void parseJsonToListLong_shouldReturnLongList_whenJsonArray() {
            // Arrange
            String json = "[1,2,3]";

            // Act
            List<Long> result = Jsons.parseJsonToListLong(json);

            // Assert
            assertThat(result).containsSequence(1L, 2L, 3L);
        }

        @Test
        @DisplayName("解析JSON数组为String类型List")
        void parseJsonToListString_shouldReturnStringList_whenJsonArray() {
            // Arrange
            String json = "[1,2,3]";

            // Act
            List<String> result = Jsons.parseJsonToListString(json);

            // Assert
            assertThat(result).containsSequence("1", "2", "3");
        }

        @Test
        @DisplayName("解析JSON数组为指定类型List")
        void parseJsonToList_shouldReturnCorrectTypeList_whenJsonArray() {
            // Arrange
            String numberJson = "[1,2,3]";
            String objectJson = "[{\"id\":1,\"_widget_1513666823960\":\"jack\"}]";

            // Act
            List<Integer> intList = Jsons.parseJsonToList(numberJson, Integer.class);
            List<String> stringList = Jsons.parseJsonToList(numberJson, String.class);
            List<IdAndName> objectList = Jsons.parseJsonToList(objectJson, IdAndName.class);

            // Assert
            assertThat(intList).containsSequence(1, 2, 3);
            assertThat(stringList).containsSequence("1", "2", "3");
            assertThat(objectList).hasSize(1);
            assertThat(objectList.get(0).getId()).isEqualTo(1L);
            assertThat(objectList.get(0).getName()).isEqualTo("jack");
        }
    }

    @Nested
    @DisplayName("parseJsonToMap方法测试 - 解析JSON为Map")
    class ParseJsonToMapTests {

        @Test
        @DisplayName("解析JSON对象为Map")
        void parseJsonToMap_shouldReturnCorrectMap_whenJsonObject() {
            // Arrange
            String json = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";

            // Act
            Record record = new Record(Jsons.parseJsonToMap(json));
            List<String> rtmpUrl = record.getListString("rtmp_url");

            // Assert
            assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
        }

        @Test
        @DisplayName("解析JSON对象为指定类型Map")
        void parseJsonToMap_shouldReturnCorrectTypedMap_whenJsonObject() {
            // Arrange
            String json = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";

            // Act
            Record record = new Record(Jsons.parseJsonToMap(json, String.class, Object.class));
            List<String> rtmpUrl = record.getListString("rtmp_url");

            // Assert
            assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
        }

        @Test
        @DisplayName("解析JSON数组为List<Map>")
        void parseJsonToListMap_shouldReturnCorrectListMap_whenJsonArray() {
            // Arrange
            String json = "[{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}]";

            // Act
            List<Map<String, Object>> result = Jsons.parseJsonToListMap(json);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("appid")).isEqualTo("207927587");
            if (result.get(0).get("rtmp_url") instanceof List<?> rtmpUrl) {
                assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
            }
        }
    }

    @Nested
    @DisplayName("parseJsonToRecord方法测试 - 解析JSON为Record")
    class ParseJsonToRecordTests {

        @Test
        @DisplayName("解析JSON对象为Record")
        void parseJsonToRecord_shouldReturnCorrectRecord_whenJsonObject() {
            // Arrange
            String json = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";

            // Act
            Record record = Jsons.parseJsonToRecord(json);
            List<String> rtmpUrl = record.getListString("rtmp_url");

            // Assert
            assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
        }
    }

    @Nested
    @DisplayName("JsonNode操作方法测试 - JSON节点操作")
    class JsonNodeOperationTests {

        @Test
        @DisplayName("读取JSON字符串为JsonNode")
        void readTree_shouldReturnCorrectJsonNode_whenValidJson() {
            // Arrange
            String json = "{\"id\":1,\"_widget\":\"jack\"}";

            // Act
            JsonNode result = Jsons.readTree(json);

            // Assert
            assertThat(result.get("id").asInt()).isEqualTo(1);
            assertThat(result.get("_widget").asText()).isEqualTo("jack");
        }

        @Test
        @DisplayName("读取无效JSON字符串返回null")
        void readTree_shouldReturnNull_whenInvalidJson() {
            // Arrange
            String invalidJson = "{\"id\":1,\"_widget\":\"jack\"";

            // Act
            JsonNode result = Jsons.readTree(invalidJson);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("格式化JSON字符串")
        void prettyPrint_shouldReturnFormattedJson_whenValidJson() {
            // Arrange
            String json = "{\"id\":1,\"_widget_1513666823960\":\"jack\"}";
            String expected = """
                {
                  "id" : 1,
                  "_widget_1513666823960" : "jack"
                }""";

            // Act
            String result = Jsons.prettyPrint(json);

            // Assert
            assertThat(result.replace("\r", "")).isEqualTo(expected);
        }
    }

    @Accessors(chain = true)
    @Data
    public static class IdAndName {

        private Long id;
        @JsonProperty("_widget_1513666823960")
        private String name;
    }

    @Accessors(chain = true)
    @Data
    public static class NameBean {

        @JsonProperty("_widget_1541348776165")
        private NameValue name;

        @Accessors(chain = true)
        @Data
        public static class NameValue {

            private String value;

            public static NameValue value(String value) {
                return new NameValue().setValue(value);
            }
        }
    }

    @Data
    public static class Response {

        @JsonProperty("RequestId")
        private String requestId;

        @JsonProperty("Message")
        private String message;
        @JsonProperty("Code")
        private Integer code;
    }
}
