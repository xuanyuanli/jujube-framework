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
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.Test;

@SuppressWarnings("DataFlowIssue")
public class JsonsTest {

    @Test
    public void parseJsonClassNormal() {
        String text = "{\"id\":null,\"_widget_1513666823960\":\"90\"}";
        IdAndName user = Jsons.parseJson(text, IdAndName.class);
        assertThat(user.getName()).isEqualTo("90");
        assertThat(user.getId()).isNull();
    }

    @Test
    public void parseJsonClassSingleQuote() {
        String text = "{'id':null,'_widget_1513666823960':'90'}";
        IdAndName user = Jsons.parseJson(text, IdAndName.class);
        assertThat(user.getName()).isEqualTo("90");
        assertThat(user.getId()).isNull();
    }

    @Test
    public void parseJsonClassUseJsonProperty() {
        String val = "{\"RequestId\":\"6F5CFFB8-66D6-454C-8D4F-233343364DBA\",\"Message\":\"子账号没有权限\",\"Code\":\"10009\"}";
        Response response = Jsons.parseJson(val, Response.class);
        Assertions.assertThat(response.getRequestId()).isEqualTo("6F5CFFB8-66D6-454C-8D4F-233343364DBA");
        Assertions.assertThat(response.getMessage()).isEqualTo("子账号没有权限");
        Assertions.assertThat(response.getCode()).isEqualTo(10009);
    }

    @Test
    public void parseJsonClassPrimitive() {
        String json = Jsons.parseJson("1", String.class);
        Assertions.assertThat(json).isEqualTo("1");

        json = Jsons.parseJson("1", (Type) String.class);
        Assertions.assertThat(json).isEqualTo("1");
    }

    @Test
    public void parseJsonClassComplex() {
        String json = "{\"_widget_1541348776165\":{\"value\":\"abc\"}}";
        assertThat(Jsons.toJson(new NameBean().setName(NameBean.NameValue.value("abc")))).isEqualTo(json);
        NameBean user = Jsons.parseJson(json, NameBean.class);
        assertThat(user.getName().getValue()).isEqualTo("abc");
    }

    @Test
    public void toJsonPrimitive() {
        String json = Jsons.toJson("1");
        String expected = "\"1\"";
        Assertions.assertThat(json).isEqualTo(expected);
    }

    @Test
    public void toJsonNormal() {
        assertThat(Jsons.toJson(null)).isNull();
        assertThat(Jsons.toJson(new IdAndName().setId(1L).setName("jack"))).isEqualTo("{\"id\":1,\"_widget_1513666823960\":\"jack\"}");
    }

    @Test
    void toPrettyJson() {
        assertThat(Jsons.toPrettyJson(null)).isNull();
        assertThat(Jsons.toPrettyJson(new IdAndName().setId(1L).setName("jack")).replace("\r", "")).isEqualTo("""
                {
                  "id" : 1,
                  "_widget_1513666823960" : "jack"
                }""");
    }

    @Test
    void parseJsonToListLong() {
        List<Long> ids = Jsons.parseJsonToListLong("[1,2,3]");
        assertThat(ids).containsSequence(1L, 2L, 3L);
    }

    @Test
    void parseJsonToListList() {
        List<Integer> ids = Jsons.parseJsonToList("[1,2,3]", Integer.class);
        assertThat(ids).containsSequence(1, 2, 3);

        List<String> sids = Jsons.parseJsonToList("[1,2,3]", String.class);
        assertThat(sids).containsSequence("1", "2", "3");

        List<IdAndName> idAndNames = Jsons.parseJsonToList("[{\"id\":1,\"_widget_1513666823960\":\"jack\"}]", IdAndName.class);
        assertThat(idAndNames).hasSize(1);
        assertThat(idAndNames.get(0).getId()).isEqualTo(1L);
        assertThat(idAndNames.get(0).getName()).isEqualTo("jack");
    }

    @Test
    void parseJsonToListString() {
        List<String> ids = Jsons.parseJsonToListString("[1,2,3]");
        assertThat(ids).containsSequence("1", "2", "3");
    }

    @Test
    void parseJsonTypeReference() {
        List<Long> ids = Jsons.parseJson("[1,2,3]", new TypeReference<List<Long>>() {
        });
        assertThat(ids).containsSequence(1L, 2L, 3L);
    }

    @Test
    void readTree() {
        JsonNode jsonNode = Jsons.readTree("{\"id\":1,\"_widget\":\"jack\"}");
        assertThat(jsonNode.get("id").asInt()).isEqualTo(1);
        assertThat(jsonNode.get("_widget").asText()).isEqualTo("jack");

        assertThat(Jsons.readTree("{\"id\":1,\"_widget\":\"jack\"")).isNull();
    }

    @Test
    void prettyPrint() {
        assertThat(Jsons.prettyPrint("{\"id\":1,\"_widget_1513666823960\":\"jack\"}").replace("\r", "")).isEqualTo("""
                {
                  "id" : 1,
                  "_widget_1513666823960" : "jack"
                }""");
    }

    @Test
    public void parseJsonToMap() {
        String text = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";
        Record record = new Record(Jsons.parseJsonToMap(text));
        List<String> rtmpUrl = record.getListString("rtmp_url");
        Assertions.assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
    }

    @Test
    void parseJsonToRecord() {
        String text = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";
        Record record = Jsons.parseJsonToRecord(text);
        List<String> rtmpUrl = record.getListString("rtmp_url");
        Assertions.assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
    }

    @Test
    public void parseJsonToMap3() {
        String text = "{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}";
        Record record = new Record(Jsons.parseJsonToMap(text, String.class, Object.class));
        List<String> rtmpUrl = record.getListString("rtmp_url");
        Assertions.assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
    }

    @Test
    void parseJsonToListMap(){
        String text = "[{\"appid\":\"207927587\",\"channel_id\":\"room-142\",\"create_time\":\"1628837107\",\"create_timemillis\":\"1628837107255\",\"event\":\"stream_create\",\"extra_info\":\"\",\"hdl_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.flv\"],\"hls_url\":[\"http://play-qcloud-test.zego.im/live/zegotest-207927587-live-142.m3u8\"],\"nonce\":\"6995802105798954047\",\"pic_url\":[\"http://pic-qcloud-test.zego.im/live/zegotest-207927587-live-142.jpg\"],\"publish_id\":\"8\",\"publish_name\":\"user-8\",\"recreate\":\"0\",\"room_id\":\"room-142\",\"rtmp_url\":[\"rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142\"],\"signature\":\"681cfc650cc7a5016a681fd570289be6136f729b\",\"stream_alias\":\"zegotest-207927587-live-142\",\"stream_id\":\"zegotest-207927587-live-142\",\"stream_seq\":\"1\",\"stream_sid\":\"s-20792758716288371072250020000251426\",\"timestamp\":\"1628837107\",\"title\":\"\",\"user_id\":\"8\",\"user_name\":\"user-8\"}]";
        List<Map<String, Object>> maps = Jsons.parseJsonToListMap(text);
        Assertions.assertThat(maps).hasSize(1);
        Assertions.assertThat(maps.get(0).get("appid")).isEqualTo("207927587");
        if (maps.get(0).get("rtmp_url") instanceof List<?> rtmpUrl) {
            Assertions.assertThat(rtmpUrl.get(0)).isEqualTo("rtmp://play-qcloud-test.zego.im/live/zegotest-207927587-live-142");
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
