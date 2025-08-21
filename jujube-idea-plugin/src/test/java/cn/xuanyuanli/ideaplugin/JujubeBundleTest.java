package cn.xuanyuanli.ideaplugin;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for JujubeBundle internationalization
 * 
 * @author xuanyuanli
 */
public class JujubeBundleTest {

    @Test
    public void testChineseMessages() {
        // Test Chinese (default) messages
        Locale.setDefault(Locale.CHINA);
        String message = JujubeBundle.getText("quick.fix.map.method.call");
        assertEquals("修复Map方法调用", message);
        
        message = JujubeBundle.getText("quick.fix.jpa.method.params");
        assertEquals("填充JPA方法参数", message);
    }

    @Test
    public void testEnglishMessages() {
        // Test English messages
        Locale.setDefault(Locale.ENGLISH);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        String message = bundle.getString("quick.fix.map.method.call");
        assertEquals("Fix Map Method Call", message);
        
        message = bundle.getString("quick.fix.jpa.method.params");
        assertEquals("Fill JPA Method Parameters", message);
    }

    @Test
    public void testActionMessages() {
        String message = JujubeBundle.getText("action.sql.to.po");
        assertEquals("根据SQL生成PO", message);
        
        message = JujubeBundle.getText("action.map.to.bean");
        assertEquals("转换Map为Bean", message);
    }

    @Test
    public void testErrorMessages() {
        String message = JujubeBundle.getText("error.parse.sql");
        assertEquals("解析Sql出错，请确认选择了正确的sql语句", message);
        
        message = JujubeBundle.getText("error.no.fields");
        assertEquals("未获取到任何字段，无法继续", message);
    }
}