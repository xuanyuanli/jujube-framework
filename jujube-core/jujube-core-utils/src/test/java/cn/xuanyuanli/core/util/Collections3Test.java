package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Collections3 集合工具类测试")
class Collections3Test {

    @Nested
    @DisplayName("集合提取测试")
    class ExtractionTests {

        @Test
        @DisplayName("extractToListString_应该提取字段为字符串列表_当给定字段名时")
        void extractToListString_shouldExtractFieldToStringList_whenGivenFieldName() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            List<String> result = Collections3.extractToListString(data, "name");

            // Assert
            assertThat(result).hasSize(3).contains("bc", "ef", "df");
        }

        @Test
        @DisplayName("extractToList_应该提取字段值列表_当给定字段名时")
        void extractToList_shouldExtractFieldValueList_whenGivenFieldName() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            List<Object> result = Collections3.extractToList(data, "name");

            // Assert
            assertThat(result).hasSize(3).contains("bc", "df", "ef");
        }

        @SuppressWarnings("ConstantValue")
        @Test
        @DisplayName("extractToList_应该返回null_当输入为null时")
        void extractToList_shouldReturnNull_whenInputIsNull() {
            // Act
            List<Integer> result = Collections3.extractToList(null, "cardId", int.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("extractToList_应该提取指定类型字段值_当给定字段名和类型时")
        void extractToList_shouldExtractSpecificTypeFieldValues_whenGivenFieldNameAndType() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            List<Integer> result = Collections3.extractToList(data, "cardId", int.class);

            // Assert
            assertThat(result).hasSize(3).contains(12, 123, 36);
        }

        @Test
        @DisplayName("extractToMap_应该提取为映射_当给定键值字段名时")
        void extractToMap_shouldExtractToMap_whenGivenKeyAndValueFieldNames() {
            // Arrange
            User user = new ChildUser().setCardId(12L).setName("bc");
            User user2 = new ChildUser().setCardId(123L).setName("ef");
            User user3 = new ChildUser().setCardId(36L).setName("df");
            List<User> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            Map<Object, Object> result = Collections3.extractToMap(data, "cardId", "name");

            // Assert
            assertThat(result).hasSize(3).containsEntry(12L, "bc");
        }

        @Test
        @DisplayName("extractToString_应该提取为分隔字符串_当给定分隔符时")
        void extractToString_shouldExtractToDelimitedString_whenGivenDelimiter() {
            // Arrange
            ArrayList<User> list = new ArrayList<>(Arrays.asList(new User().setName("a"), new User().setName("b")));

            // Act & Assert
            assertThat(Collections3.extractToString(list, "name", ",")).isEqualTo("a,b");
            assertThat(Collections3.extractToString(list, "id", ",")).isEqualTo(",");
        }
    }

    @Nested
    @DisplayName("集合查找测试")
    class SearchTests {

        @Test
        @DisplayName("getOne_应该返回匹配对象_当找到匹配项时")
        void getOne_shouldReturnMatchingObject_whenMatchFound() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(123L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act & Assert
            assertThat(Collections3.getOne(data, "cardId", 123L)).isEqualTo(user);
        }

        @Test
        @DisplayName("getOne_应该返回匹配集合_当处理Record类型时")
        void getOne_shouldReturnMatchingCollection_whenHandlingRecordType() {
            // Arrange
            Record record = new Record().set("id", 34L);
            List<Record> list = new ArrayList<>(Collections.singletonList(record));
            long id = 34;

            // Act
            Object result = Collections3.getOne(list, "id", id);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getPart_应该返回匹配部分_当有多个匹配项时")
        void getPart_shouldReturnMatchingPart_whenMultipleMatchesFound() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            Collection<ChildUser> result = Collections3.getPart(data, "cardId", 12L);

            // Assert
            assertThat(result).hasSize(2).contains(user, user2);
        }

        @Test
        @DisplayName("containsFieldValue_应该正确判断是否包含字段值_当检查集合时")
        void containsFieldValue_shouldCorrectlyCheckIfContainsFieldValue_whenCheckingCollection() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act & Assert
            assertThat(Collections3.containsFieldValue(null, "", "")).isFalse();
            assertThat(Collections3.containsFieldValue(data, "name", "df")).isTrue();
            assertThat(Collections3.containsFieldValue(data, "name", "df3")).isFalse();
        }
    }

    @Nested
    @DisplayName("集合操作测试")
    class CollectionOperationTests {

        @Test
        @DisplayName("union_应该返回并集_当合并两个集合时")
        void union_shouldReturnUnion_whenMergingTwoCollections() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Collections.singletonList(user));
            List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));

            // Act
            Collection<ChildUser> result = Collections3.union(data, data2);

            // Assert
            assertThat(result).hasSize(3).contains(user, user2, user3);
        }

        @Test
        @DisplayName("subtract_应该返回差集_当从集合中减去另一集合时")
        void subtract_shouldReturnDifference_whenSubtractingOneCollectionFromAnother() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2));
            List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));

            // Act
            Collection<ChildUser> result = Collections3.subtract(data, data2);

            // Assert
            assertThat(result).hasSize(1).contains(user);
        }

        @Test
        @DisplayName("intersection_应该返回交集_当获取两个集合的交集时")
        void intersection_shouldReturnIntersection_whenGettingIntersectionOfTwoCollections() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(123L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(36L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2));
            List<ChildUser> data2 = new ArrayList<>(Arrays.asList(user2, user3));

            // Act
            Collection<ChildUser> result = Collections3.intersection(data, data2);

            // Assert
            assertThat(result).hasSize(1).contains(user2);
        }

        @Test
        @DisplayName("deWeight_应该按属性去重_当使用属性提取器时")
        void deWeight_shouldDeduplicateByProperty_whenUsingPropertyExtractor() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(123L).setName("df");
            List<ChildUser> data = new ArrayList<>(Arrays.asList(user, user2, user3));

            // Act
            List<ChildUser> result = Collections3.deWeight(data, ChildUser::getCardId);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("toDiffArray_应该去除重复元素_当处理数组时")
        void toDiffArray_shouldRemoveDuplicates_whenProcessingArray() {
            // Arrange
            String[] arr = {"1", "1", "2"};

            // Act
            Object[] result = Collections3.toDiffArray(arr);

            // Assert
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Map操作测试")
    class MapOperationTests {

        @Test
        @DisplayName("newHashMap_应该创建空Map_当无参数时")
        void newHashMap_shouldCreateEmptyMap_whenNoParameters() {
            // Act
            Map<String, Object> result = Collections3.newHashMap();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("newHashMap_应该抛出异常_当参数个数为奇数时")
        void newHashMap_shouldThrowException_whenOddNumberOfParameters() {
            // Act & Assert
            assertThatThrownBy(() -> Collections3.newHashMap(1))
                    .hasMessage("参数必须成对出现")
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("newHashMap_应该创建Map_当给定键值对时")
        void newHashMap_shouldCreateMap_whenGivenKeyValuePairs() {
            // Act
            Map<String, Double> map = Collections3.newHashMap("A", 1D, "C", 8D);
            Map<String, Object> map2 = Collections3.newHashMap("A", 1D, "C", "4");
            Map<String, Object> map3 = Collections3.newHashMap(102L, 1D, 202, "4");

            // Assert
            assertThat(map).hasSize(2);
            assertThat(map.get("A")).isEqualTo(1D);
            assertThat(map2).hasSize(2);
            assertThat(map2.get("C")).isEqualTo("4");
            assertThat(map3).hasSize(2);
            assertThat(map3.get("202")).isEqualTo("4");
        }

        @Test
        @DisplayName("sortMapByKey_应该按键排序_当给定比较器时")
        void sortMapByKey_shouldSortByKey_whenGivenComparator() {
            // Arrange
            Map<String, Integer> map = new HashMap<>();
            map.put("10", 10);
            map.put("101", 101);
            map.put("1", 1);

            // Act
            Map<String, Integer> result = Collections3.sortMapByKey(map, String::compareTo);

            // Assert
            assertThat(result.getClass()).isEqualTo(LinkedHashMap.class);
            assertThat(result.keySet()).containsExactly("1", "10", "101");
            assertThat(result.values()).containsExactly(1, 10, 101);
        }

        @Test
        @DisplayName("sortMapByValue_应该按值排序_当给定比较器时")
        void sortMapByValue_shouldSortByValue_whenGivenComparator() {
            // Arrange
            Map<String, Integer> map = new HashMap<>();
            map.put("10", 10);
            map.put("101", 101);
            map.put("1", 1);

            // Act
            Map<String, Integer> result = Collections3.sortMapByValue(map, Integer::compareTo);

            // Assert
            assertThat(result.getClass()).isEqualTo(LinkedHashMap.class);
            assertThat(result.keySet()).containsExactly("1", "10", "101");
            assertThat(result.values()).containsExactly(1, 10, 101);
        }
    }

    @Nested
    @DisplayName("集合状态检查测试")
    class CollectionStateTests {

        @Test
        @DisplayName("isEmpty_应该正确判断集合是否为空_当检查Collection时")
        void isEmpty_shouldCorrectlyDetermineIfEmpty_whenCheckingCollection() {
            // Act & Assert
            assertThat(Collections3.isEmpty(new ArrayList<>())).isTrue();
            assertThat(Collections3.isEmpty(new HashSet<>())).isTrue();
            //noinspection ConstantValue
            assertThat(Collections3.isEmpty((Collection<?>) null)).isTrue();
            assertThat(Collections3.isEmpty(new ArrayList<>(List.of(1)))).isFalse();
        }

        @Test
        @DisplayName("isEmpty_应该正确判断Map是否为空_当检查Map时")
        void isEmpty_shouldCorrectlyDetermineIfEmpty_whenCheckingMap() {
            // Arrange
            TreeMap<String, Object> treeMap = new TreeMap<>();
            treeMap.put("1", 1);

            // Act & Assert
            assertThat(Collections3.isEmpty(new HashMap<>())).isTrue();
            //noinspection ConstantValue
            assertThat(Collections3.isEmpty((Map<?, ?>) null)).isTrue();
            assertThat(Collections3.isEmpty(treeMap)).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty_应该正确判断集合是否非空_当检查集合时")
        void isNotEmpty_shouldCorrectlyDetermineIfNotEmpty_whenCheckingCollection() {
            // Act & Assert
            assertThat(Collections3.isNotEmpty(new ArrayList<>())).isFalse();
            assertThat(Collections3.isNotEmpty(new HashSet<>())).isFalse();
            //noinspection ConstantValue
            assertThat(Collections3.isNotEmpty(null)).isFalse();
            assertThat(Collections3.isNotEmpty(new ArrayList<>(List.of(1)))).isTrue();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("enumerationToList_应该转换枚举为列表_当给定枚举时")
        void enumerationToList_shouldConvertEnumerationToList_whenGivenEnumeration() {
            // Arrange
            Hashtable<String, Object> hashtable = new Hashtable<>();
            hashtable.put("1", 1);
            hashtable.put("2", 2);

            // Act & Assert
            assertThat(Collections3.enumerationToList(hashtable.keys())).contains("1", "2");
            assertThat(Collections3.enumerationToList(hashtable.elements())).contains(1, 2);
            assertThat(Collections3.enumerationToList(null)).isNull();
            assertThat(Collections3.enumerationToList(new Hashtable<>().keys())).isEmpty();
        }

        @Test
        @DisplayName("getListFromIterator_应该从迭代器创建列表_当给定迭代器时")
        void getListFromIterator_shouldCreateListFromIterator_whenGivenIterator() {
            // Arrange
            ChildUser user = (ChildUser) new ChildUser().setCardId(12L).setName("bc");
            ChildUser user2 = (ChildUser) new ChildUser().setCardId(12L).setName("ef");
            ChildUser user3 = (ChildUser) new ChildUser().setCardId(123L).setName("df");

            // Act
            List<ChildUser> result = Collections3.getListFromIterator(new ArrayList<>(Arrays.asList(user, user2, user3)).iterator());

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(user, user2, user3);
        }
    }

    // 测试数据类定义
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
}