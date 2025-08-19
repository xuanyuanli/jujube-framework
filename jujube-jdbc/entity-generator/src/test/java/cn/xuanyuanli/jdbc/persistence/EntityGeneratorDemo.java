package cn.xuanyuanli.jdbc.persistence;

import java.util.List;
import cn.xuanyuanli.jdbc.client.local.LocalJdbcTemplate;
import cn.xuanyuanli.jdbc.generator.EntityGenerator;
import cn.xuanyuanli.jdbc.generator.GeneratorConstants;
import cn.xuanyuanli.core.util.Jsons;
import cn.xuanyuanli.core.util.Resources;

public class EntityGeneratorDemo {

    public static void main(String[] args) {
        generateOneEntity();
    }

    public static void getTables() {
        List<String> db = LocalJdbcTemplate.getTables("main");
        System.out.println(Jsons.toJson(db));
    }

    public static void generateEntity() {
        List<String> db = LocalJdbcTemplate.getTables("main");
        for (String tableName : db) {
            EntityGenerator.Config config = new EntityGenerator.Config(tableName, Resources.getProjectPath() + "\\src\\main\\java", "com.example.entity",
                    "com.example.persistence");
            config.setForceCoverDao(false);
            config.setForceCoverEntity(true);
            config.setCreateDao(true);
            config.setCreateDaoSqlFile(true);
            EntityGenerator.generateEntity(config);
            System.out.println();
        }
    }

    public static void generateOneEntityService() {
        String projectPath = GeneratorConstants.WORKAPACE_DIR_PATH.getParentFile().getAbsolutePath() + "\\service";
        EntityGenerator.Config config = new EntityGenerator.Config("product", projectPath + "\\src\\main\\java",
                "com.example.entity", "com.example.persistence");
        config.setForceCoverDao(false);
        config.setForceCoverEntity(true);
        config.setCreateDao(true);
        config.setCreateDaoSqlFile(true);
        config.setRemoveColumnPrefix(false);
        config.setColumnPrefix("");
        config.setRemoveColumnSuffix(false);
        config.setColumnSuffix("");
        config.setAddColumnAnnotation(false);
        EntityGenerator.generateEntity(config);
    }

    public static void generateOneEntity() {
        String projectPath = GeneratorConstants.WORKAPACE_DIR_PATH.getAbsolutePath() + "\\service\\impl";
        EntityGenerator.Config config = new EntityGenerator.Config("product", projectPath + "\\src\\main\\java", "com.example.entity",
                "com.example.persistence");
        config.setForceCoverDao(false);
        config.setForceCoverEntity(true);
        config.setCreateDao(true);
        config.setCreateDaoSqlFile(true);
        config.setRemoveColumnPrefix(false);
        config.setColumnPrefix("");
        config.setRemoveColumnSuffix(false);
        config.setColumnSuffix("");
        config.setAddColumnAnnotation(false);
        EntityGenerator.generateEntity(config);
    }
}
