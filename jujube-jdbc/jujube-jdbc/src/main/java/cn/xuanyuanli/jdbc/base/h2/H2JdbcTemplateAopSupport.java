package cn.xuanyuanli.jdbc.base.h2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import cn.xuanyuanli.core.lang.Record;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * h2数据库返回的字段名都为大写，跟mysql不兼容，此处做一下处理
 *
 * @author xuanyuanli
 */
@Aspect
public class H2JdbcTemplateAopSupport {

    /**
     * 查询列表后
     *
     * @param joinPoint 连接点
     * @return {@link Object}
     * @throws Throwable throwable
     */
    @Around("execution(* org.springframework.jdbc.core.JdbcTemplate.query(..))")
    public Object queryForListAfter(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof List && !((List<?>) result).isEmpty()) {
            Object first = ((List<?>) result).get(0);
            if (first instanceof Record) {
                result = convertListNewRecord2((List<Record>) result);
            } else if (first instanceof Map) {
                result = convertListNewRecord((List<Map<String, Object>>) result);
            }
        }
        return result;
    }

    Object convertListNewRecord2(List<Record> result) {
        return result.stream().map(this::convertNewRecord2).collect(Collectors.toList());
    }

    Map<String, Object> convertNewRecord2(Record result) {
        Record newResult = new Record();
        for (String key : result.keySet()) {
            newResult.put(key.toLowerCase(), result.get(key));
        }
        return newResult;
    }

    /**
     * 查询为map后
     *
     * @param joinPoint 连接点
     * @return {@link Object}
     * @throws Throwable throwable
     */
    @Around("execution(* org.springframework.jdbc.core.JdbcTemplate.queryForMap(..))")
    public Object queryForMapAfter(final ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, Object> result = (Map<String, Object>) joinPoint.proceed();
        return convertNewRecord(result);
    }

    List<Map<String, Object>> convertListNewRecord(List<Map<String, Object>> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(convertNewRecord(map));
        }
        return result;
    }

    static Map<String, Object> convertNewRecord(Map<String, Object> result) {
        Map<String, Object> newResult = new LinkedCaseInsensitiveMap<>();
        for (String key : result.keySet()) {
            newResult.put(key.toLowerCase(), result.get(key));
        }
        return newResult;
    }
}
