package cn.xuanyuanli.jdbc.base.h2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class H2JdbcTemplateAopSupportTest {

    private H2JdbcTemplateAopSupport aopSupport;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aopSupport = new H2JdbcTemplateAopSupport();
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    void testQueryForListAfterWithRecord() throws Throwable {
        List<Record> records = new ArrayList<>();
        Record record = new Record();
        record.put("KEY1", "value1");
        record.put("KEY2", "value2");
        records.add(record);

        when(joinPoint.proceed()).thenReturn(records);

        Object result = aopSupport.queryForListAfter(joinPoint);

        assertInstanceOf(List.class, result);
        List<Record> resultList = (List<Record>) result;
        assertEquals(1, resultList.size());
        assertTrue(resultList.get(0).containsKey("key1"));
        assertTrue(resultList.get(0).containsKey("key2"));
    }

    @Test
    void testQueryForListAfterWithMap() throws Throwable {
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("KEY1", "value1");
        map.put("KEY2", "value2");
        maps.add(map);

        when(joinPoint.proceed()).thenReturn(maps);

        Object result = aopSupport.queryForListAfter(joinPoint);

        assertInstanceOf(List.class, result);
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;
        assertEquals(1, resultList.size());
        assertTrue(resultList.get(0).containsKey("key1"));
        assertTrue(resultList.get(0).containsKey("key2"));
    }

    @Test
    void testQueryForMapAfter() throws Throwable {
        Map<String, Object> map = new HashMap<>();
        map.put("KEY1", "value1");
        map.put("KEY2", "value2");

        when(joinPoint.proceed()).thenReturn(map);

        Object result = aopSupport.queryForMapAfter(joinPoint);

        assertInstanceOf(Map.class, result);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("key1"));
        assertTrue(resultMap.containsKey("key2"));
    }

    @Test
    void testConvertListNewRecord2() {
        List<Record> records = new ArrayList<>();
        Record record = new Record();
        record.put("KEY1", "value1");
        record.put("KEY2", "value2");
        records.add(record);

        List<Map<String, Object>> result = (List<Map<String, Object>>) aopSupport.convertListNewRecord2(records);

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("key1"));
        assertTrue(result.get(0).containsKey("key2"));
    }

    @Test
    void testConvertNewRecord2() {
        Record record = new Record();
        record.put("KEY1", "value1");
        record.put("KEY2", "value2");

        Map<String, Object> result = aopSupport.convertNewRecord2(record);

        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2"));
    }

    @Test
    void testConvertListNewRecord() {
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("KEY1", "value1");
        map.put("KEY2", "value2");
        maps.add(map);

        List<Map<String, Object>> result = aopSupport.convertListNewRecord(maps);

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("key1"));
        assertTrue(result.get(0).containsKey("key2"));
    }

    @Test
    void testConvertNewRecord() {
        Map<String, Object> map = new HashMap<>();
        map.put("KEY1", "value1");
        map.put("KEY2", "value2");

        Map<String, Object> result = H2JdbcTemplateAopSupport.convertNewRecord(map);

        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2"));
    }
}
