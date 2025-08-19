package cn.xuanyuanli.jdbc.persistence;

import cn.xuanyuanli.jdbc.generator.GeneratorConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ServiceGeneratorTest {



    @Test
    void workspaceDir(){
        System.out.println(GeneratorConstants.WORKAPACE_DIR_PATH.getAbsolutePath());
    }
}
