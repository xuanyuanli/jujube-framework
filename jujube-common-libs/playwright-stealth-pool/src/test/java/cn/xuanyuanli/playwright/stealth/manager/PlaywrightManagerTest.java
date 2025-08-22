package cn.xuanyuanli.playwright.stealth.manager;

import cn.xuanyuanli.playwright.stealth.config.PlaywrightConfig;
import cn.xuanyuanli.playwright.stealth.config.StealthMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

/**
 * PlaywrightManager测试类
 * 
 * <p>提供PlaywrightManager的功能测试，包括基本功能和配置测试</p>
 *
 * @author xuanyuanli
 */
class PlaywrightManagerTest {

    private PlaywrightManager playwrightManager;
    private PlaywrightConfig config;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        config = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.FULL);
        
        // 创建Playwright管理器，池大小为4
        playwrightManager = new PlaywrightManager(4);
    }

    @AfterEach
    void tearDown() {
        if (playwrightManager != null) {
            playwrightManager.close();
        }
    }

    /**
     * 测试基本的页面访问功能
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testBasicPageAccess() {
        playwrightManager.execute(config, page -> {
            page.navigate("https://www.baidu.com");
            String title = page.title();
            System.out.println("页面标题: " + title);
            assert title.contains("百度");
        });
    }

    /**
     * 测试使用默认配置
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testDefaultConfiguration() {
        // 使用默认配置（传入null）
        playwrightManager.execute(page -> {
            page.navigate("https://httpbin.org/get");
            String content = page.content();
            System.out.println("页面内容长度: " + content.length());
        });
    }

    /**
     * 测试自定义浏览器上下文配置
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testCustomBrowserContext() {
        playwrightManager.execute(config, context -> {
            // 设置额外的权限
            context.grantPermissions(java.util.Arrays.asList("geolocation"));
            
            // 设置地理位置
            context.setGeolocation(new com.microsoft.playwright.options.Geolocation(39.9042, 116.4074));
            
        }, page -> {
            page.navigate("https://httpbin.org/get");
            
            // 检查地理位置权限
            Object geolocationPermission = page.evaluate("""
                navigator.permissions.query({name: 'geolocation'}).then(result => result.state)
            """);
            System.out.println("地理位置权限: " + geolocationPermission);
        });
    }

    /**
     * 测试不同StealthMode配置
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testStealthModeConfiguration() {
        // 测试禁用反检测
        PlaywrightConfig disabledConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.DISABLED);
                
        playwrightManager.execute(disabledConfig, page -> {
            page.navigate("https://httpbin.org/get");
            Object webdriver = page.evaluate("navigator.webdriver");
            System.out.println("DISABLED模式 - navigator.webdriver: " + webdriver);
            // 在禁用模式下，webdriver属性应该存在
        });

        // 测试轻量级反检测
        PlaywrightConfig lightConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.LIGHT);
                
        playwrightManager.execute(lightConfig, page -> {
            page.navigate("https://httpbin.org/get");
            Object webdriver = page.evaluate("navigator.webdriver");
            Object languages = page.evaluate("navigator.languages");
            System.out.println("LIGHT模式 - navigator.webdriver: " + webdriver);
            System.out.println("LIGHT模式 - navigator.languages: " + languages);
            assert webdriver == null || webdriver.toString().equals("undefined");
        });

        // 测试完整反检测
        PlaywrightConfig fullConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.FULL);
                
        playwrightManager.execute(fullConfig, page -> {
            page.navigate("https://httpbin.org/get");
            Object webdriver = page.evaluate("navigator.webdriver");
            Object pluginsLength = page.evaluate("navigator.plugins.length");
            Object hardwareConcurrency = page.evaluate("navigator.hardwareConcurrency");
            
            System.out.println("FULL模式 - navigator.webdriver: " + webdriver);
            System.out.println("FULL模式 - navigator.plugins.length: " + pluginsLength);
            System.out.println("FULL模式 - navigator.hardwareConcurrency: " + hardwareConcurrency);
            
            assert webdriver == null || webdriver.toString().equals("undefined");
            assert pluginsLength != null && Integer.parseInt(pluginsLength.toString()) > 0;
        });
    }

    /**
     * 测试反检测脚本配置（向后兼容）
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用") 
    void testStealthScriptConfiguration() {
        // 测试启用反检测脚本
        PlaywrightConfig stealthConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.FULL);
                
        playwrightManager.execute(stealthConfig, page -> {
            page.navigate("https://httpbin.org/get");
            
            // 检查webdriver属性是否被隐藏
            Object webdriver = page.evaluate("navigator.webdriver");
            System.out.println("启用反检测 - navigator.webdriver: " + webdriver);
            assert webdriver == null || webdriver.toString().equals("undefined");
        });

        // 测试禁用反检测脚本  
        PlaywrightConfig noStealthConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setStealthMode(StealthMode.DISABLED);
                
        playwrightManager.execute(noStealthConfig, page -> {
            page.navigate("https://httpbin.org/get");
            
            // webdriver属性应该存在
            Object webdriver = page.evaluate("navigator.webdriver");
            System.out.println("禁用反检测 - navigator.webdriver: " + webdriver);
            // 注意：在无头模式下，webdriver可能仍为true
        });
    }

    /**
     * 测试并发执行
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testConcurrentExecution() {
        System.out.println("开始并发测试，初始状态: " + playwrightManager.getPoolStatus());
        
        // 并发执行12个任务
        IntStream.range(0, 12).parallel().forEach(i -> {
            playwrightManager.execute(config, page -> {
                page.navigate("https://httpbin.org/delay/1");
                String content = page.textContent("body");
                System.out.printf("任务 %d 完成，响应包含delay: %s%n", 
                                 i, content.contains("delay"));
            });
        });
        
        System.out.println("并发测试完成，最终状态: " + playwrightManager.getPoolStatus());
    }

    /**
     * 测试不同浏览器配置
     */
    @Test
    @Disabled("需要网络连接的集成测试，默认禁用")
    void testDifferentBrowserConfigurations() {
        // 测试有头模式（仅在开发环境测试）
        PlaywrightConfig headfulConfig = new PlaywrightConfig()
                .setHeadless(false)
                .setStartMaximized(true)
                .setSlowMo(100.0);
                
        // 这个测试在CI环境中应该跳过
        if (System.getProperty("CI") == null) {
            playwrightManager.execute(headfulConfig, page -> {
                page.navigate("https://www.baidu.com");
                page.waitForTimeout(2000); // 等待观察
                System.out.println("有头模式测试完成");
            });
        }

        // 测试禁用GPU
        PlaywrightConfig noGpuConfig = new PlaywrightConfig()
                .setHeadless(true)
                .setDisableGpu(true)
                .setDisableImageRender(true);
                
        playwrightManager.execute(noGpuConfig, page -> {
            page.navigate("https://httpbin.org/get");
            System.out.println("无GPU模式测试完成");
        });
    }

    /**
     * 测试错误处理
     */
    @Test
    void testErrorHandling() {
        try {
            playwrightManager.execute(config, page -> {
                // 故意触发一个错误
                throw new RuntimeException("测试异常处理");
            });
            assert false : "应该抛出异常";
        } catch (RuntimeException e) {
            System.out.println("成功捕获异常: " + e.getMessage());
            assert e.getMessage().contains("测试异常处理");
        }
        
        // 确保连接池状态正常
        System.out.println("异常处理后连接池状态: " + playwrightManager.getPoolStatus());
    }
}