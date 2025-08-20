package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.Test;

public class Collections3Test {

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class User {

        private Long id;
        private String name;
        private Integer age;
        private String blogType;
        private String log_type;

        @SuppressWarnings({"unused", "EmptyMethod"})
        public void setT(String tname, int tage, double tprice) {
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class ChildUser extends User {

        private Long cardId;
        private Double price;
    }

    @Test
    public void testExtractToListString() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        List<String> list = Collections3.extractToListString(data, "name");
        assertThat(list).hasSize(3).contains("bc", "ef", "df");
    }

    @Test
    public void testGetOne() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.getOne(data, "cardId", 123L)).isEqualTo(user);

        Record record = new Record().set("id", 34L);
        List<Record> list = new ArrayList<>(Collections.singletonList(record));
        long id = 34;
        assertThat(Collections3.getOne(list, "id", id)).hasSize(1);

    }

    @Test
    public void testGetPart() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.getPart(data, "cardId", 12L)).hasSize(2).contains(user, user2);
    }

    @Test
    public void testToDiffArray() {
        String[] arr = {"1", "1", "2"};
        assertThat(Collections3.toDiffArray(arr)).hasSize(2);
    }

    @Test
    public void testExtractToMap() {
        User user = new ChildUser().setCardId(12L).setName("bc");
        User user2 = new ChildUser().setCardId(123L).setName("ef");
        User user3 = new ChildUser().setCardId(36L).setName("df");
        List<User> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.extractToMap(data, "cardId", "name")).hasSize(3).containsEntry(12L, "bc");
    }

    @Test
    public void testExtractToList() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.extractToList(data, "name")).hasSize(3).contains("bc", "df", "ef");
    }

    @Test
    public void testExtractToList2() {
        assertThat(Collections3.extractToList(null, "cardId", int.class)).isNull();

        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.extractToList(data, "cardId", int.class)).hasSize(3).contains(12, 123, 36);
    }

    @Test
    public void testUnion() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Collections.singletonList(user));
        List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));
        assertThat(Collections3.union(data, data2)).hasSize(3).contains(user, user2, user3);
    }

    @Test
    public void testSubtract() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2));
        List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));
        assertThat(Collections3.subtract(data, data2)).hasSize(1).contains(user);
    }

    @Test
    public void testIntersection() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2));
        List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));
        assertThat(Collections3.intersection(data, data2)).hasSize(1).contains(user2);
    }

    @Test
    public void testContainsFieldValue() {
        assertThat(Collections3.containsFieldValue(null, "", "")).isFalse();

        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        assertThat(Collections3.containsFieldValue(data, "name", "df")).isTrue();
        assertThat(Collections3.containsFieldValue(data, "name", "df3")).isFalse();
    }

    @Test
    public void distinctByProperty() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(123L).setName("df");
        List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));
        List<ChildUser> list = Collections3.deWeight(data, ChildUser::getCardId);
        assertThat(list).hasSize(2);
    }

    @Test
    public void newHashMap() {
        assertThat(Collections3.newHashMap()).isEmpty();
        assertThatThrownBy(() -> Collections3.newHashMap(1)).hasMessage("参数必须成对出现").isInstanceOf(IllegalArgumentException.class);

        Map<String, Double> map = Collections3.newHashMap("A", 1D, "C", 8D);
        assertThat(map).hasSize(2);
        assertThat(map.get("A")).isEqualTo(1D);

        Map<String, Object> map2 = Collections3.newHashMap("A", 1D, "C", "4");
        assertThat(map2).hasSize(2);
        assertThat(map2.get("C")).isEqualTo("4");

        Map<String, Object> map3 = Collections3.newHashMap(102L, 1D, 202, "4");
        assertThat(map3).hasSize(2);
        assertThat(map3.get("202")).isEqualTo("4");
    }

    @Test
    void extractToString() {
        ArrayList<User> list = new ArrayList<>(Arrays.asList(new User().setName("a"), new User().setName("b")));
        assertThat(Collections3.extractToString(list, "name", ",")).isEqualTo("a,b");
        assertThat(Collections3.extractToString(list, "id", ",")).isEqualTo(",");
    }

    @Test
    void isEmptyCollection() {
        assertThat(Collections3.isEmpty(new ArrayList<>())).isTrue();
        assertThat(Collections3.isEmpty(new HashSet<>())).isTrue();
        //noinspection ConstantValue
        assertThat(Collections3.isEmpty((Collection<?>) null)).isTrue();
        assertThat(Collections3.isEmpty(new ArrayList<>(List.of(1)))).isFalse();
    }

    @Test
    void isEmptyMap() {
        assertThat(Collections3.isEmpty(new HashMap<>())).isTrue();
        //noinspection ConstantValue
        assertThat(Collections3.isEmpty((Map<?,?>) null)).isTrue();
        TreeMap<String,Object> treeMap = new TreeMap<>();
        treeMap.put("1", 1);
        assertThat(Collections3.isEmpty(treeMap)).isFalse();
    }

    @Test
    void isNotEmpty() {
        assertThat(Collections3.isNotEmpty(new ArrayList<>())).isFalse();
        assertThat(Collections3.isNotEmpty(new HashSet<>())).isFalse();
        //noinspection ConstantValue
        assertThat(Collections3.isNotEmpty(null)).isFalse();
        assertThat(Collections3.isNotEmpty(new ArrayList<>(List.of(1)))).isTrue();
    }

    @Test
    void enumerationToList() {
        Hashtable<String,Object> hashtable = new Hashtable<>();
        hashtable.put("1", 1);
        hashtable.put("2", 2);
        assertThat(Collections3.enumerationToList(hashtable.keys())).contains("1", "2");
        assertThat(Collections3.enumerationToList(hashtable.elements())).contains(1, 2);
        assertThat(Collections3.enumerationToList(null)).isNull();
        assertThat(Collections3.enumerationToList(new Hashtable<>().keys())).isEmpty();
    }

    @Test
    void sortMapByKey() {
        Map<String, Integer> map = new HashMap<>();
        map.put("10", 10);
        map.put("101", 101);
        map.put("1", 1);
        Map<String, Integer> rmap = Collections3.sortMapByKey(map, String::compareTo);
        assertThat(rmap.getClass()).isEqualTo(LinkedHashMap.class);
        assertThat(rmap.keySet()).containsExactly("1", "10", "101");
        assertThat(rmap.values()).containsExactly(1, 10, 101);
    }

    @Test
    void sortMapByValue() {
        Map<String, Integer> map = new HashMap<>();
        map.put("10", 10);
        map.put("101", 101);
        map.put("1", 1);
        Map<String, Integer> rmap = Collections3.sortMapByValue(map, Integer::compareTo);
        assertThat(rmap.getClass()).isEqualTo(LinkedHashMap.class);
        assertThat(rmap.keySet()).containsExactly("1", "10", "101");
        assertThat(rmap.values()).containsExactly(1, 10, 101);
    }

    @Test
    void getListFromIterator() {
        ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
        ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
        ChildUser user3 = (ChildUser) new ChildUser().setCardId(123L).setName("df");
        List<ChildUser> list = Collections3.getListFromIterator(new ArrayList<>(Arrays.asList(user, user2, user3)).iterator());
        assertThat(list).hasSize(3);
        assertThat(list).containsExactly(user, user2, user3);
    }
}
