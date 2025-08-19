package cn.xuanyuanli.jdbc.generator;

import java.io.File;

import cn.xuanyuanli.core.util.Resources;

/**
 * 常量
 *
 * @author John Li
 */
public interface GeneratorConstants {

    File WORKAPACE_DIR_PATH = new File(Resources.getProjectPath()).getParentFile().getParentFile().getParentFile();
}
