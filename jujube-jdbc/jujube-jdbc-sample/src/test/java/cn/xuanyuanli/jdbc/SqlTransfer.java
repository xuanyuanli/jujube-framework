package cn.xuanyuanli.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.exception.RepeatException;
import cn.xuanyuanli.core.util.Resources;
import cn.xuanyuanli.core.util.Texts;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * @author xuanyuanli
 * @date 2023/4/21
 */
public class SqlTransfer {

    public static void main(String[] args) {
        Map<String, List<String>> methodSql = getMethodSql();
        Map<String, List<String>> sqlFile = new LinkedHashMap<>();
        methodSql.forEach((key, value) -> {
            String[] arr = key.split("\\.");
            String fileName = arr[0];
            String methodName = arr[1];

            List<String> list = new ArrayList<>();
            list.add(0, "<@" + methodName + ">");
            list.addAll(value);
            list.add("</@" + methodName + ">");
            list.add("\n");

            sqlFile.computeIfAbsent(fileName, k -> new ArrayList<>()).addAll(list);
        });
        sqlFile.forEach((key, value) -> {
            try {
                File file = new File("D:\\workspace\\taopai\\2nd-lib\\jujube-jdbc-modules\\jujube-jdbc-sample\\src\\main\\resources\\dao-sql", key + ".sql");
                FileUtils.writeLines(file, StandardCharsets.UTF_8.name(), value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Map<String, List<String>> getMethodSql() {
        Resource[] sqlResources = Resources.getClassPathAllResources(ClassUtils.convertClassNameToResourcePath("dao-sql") + "/**/*.sql");
        Map<String, List<String>> result = new LinkedHashMap<>(sqlResources.length * 2);
        for (Resource sqlResource : sqlResources) {
            try (InputStream inputStream = sqlResource.getInputStream()) {
                String filename = sqlResource.getFilename();
                if (filename == null) {
                    continue;
                }
                filename = filename.substring(0, filename.length() - 4);
                List<String> lines = IOUtils.readLines(inputStream, Charsets.UTF_8.name());
                Map<String, List<String>> group;
                try {
                    group = Texts.group(lines, t -> t.startsWith("##") ? t.substring(2).trim() : "", false);
                } catch (RepeatException e) {
                    throw new RepeatException("同一个Dao Sql中不允许存在同名方法：" + filename + "." + e.getMessage());
                }
                for (String key : group.keySet()) {
                    String key1 = filename + "." + key;
                    result.put(key1, group.get(key));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
