package cn.xuanyuanli.jdbc;

import java.util.List;
import java.util.Map;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;

public class Test {

    public static void main(String[] args) {
        Map<String, List<String>> methodSql = DaoSqlRegistry.getMethodSql();
        methodSql.forEach((key, value) -> {
            System.out.println(key + "\t" + String.join(" ", value));
        });
    }

}
