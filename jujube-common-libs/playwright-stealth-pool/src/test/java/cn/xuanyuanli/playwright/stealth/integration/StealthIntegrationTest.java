package cn.xuanyuanli.playwright.stealth.integration;

import cn.xuanyuanli.playwright.stealth.config.PlaywrightConfig;
import cn.xuanyuanli.playwright.stealth.config.StealthMode;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightBrowserManager;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightManager;
import cn.xuanyuanli.playwright.stealth.stealth.StealthScriptProvider;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.*;

/**
 * 反检测功能集成测试
 *
 * <p>测试反检测功能与其他组件的集成，包括：</p>
 * <ul>
 *   <li>反检测脚本与Manager的集成</li>
 *   <li>不同StealthMode的集成效果</li>
 *   <li>配置与脚本注入的集成</li>
 *   <li>Browser实例中脚本的执行效果</li>
 * </ul>
 *
 * @author xuanyuanli
 */
@DisplayName("反检测功能集成测试")
class StealthIntegrationTest {

    @Nested
    @DisplayName("PlaywrightManager与反检测集成测试")
    class PlaywrightManagerStealthIntegrationTests {

        @Test
        @DisplayName("DISABLED模式应该不注入反检测脚本")
        void shouldNotInjectScriptInDisabledMode() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.DISABLED);
                    
            try (PlaywrightManager manager = new PlaywrightManager(2)) {
                manager.execute(config, page -> {
                    // 在DISABLED模式下，webdriver属性应该存在
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // 在无头模式下，webdriver通常为true
                    assertThat(webdriver).isNotNull();
                });
            }
        }

        @Test
        @DisplayName("LIGHT模式应该注入轻量级反检测脚本")
        void shouldInjectLightScriptInLightMode() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.LIGHT);
                    
            try (PlaywrightManager manager = new PlaywrightManager(2)) {
                manager.execute(config, page -> {
                    // 在LIGHT模式下，webdriver属性应该被隐藏
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    // 验证languages属性被修改
                    Object languages = page.evaluate("navigator.languages");
                    assertThat(languages.toString()).contains("zh-CN");
                    
                    // 验证platform属性被修改
                    Object platform = page.evaluate("navigator.platform");
                    assertThat(platform).isEqualTo("Win32");
                });
            }
        }

        @Test
        @DisplayName("FULL模式应该注入完整反检测脚本")
        void shouldInjectFullScriptInFullMode() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(2)) {
                manager.execute(config, page -> {
                    // 验证webdriver属性被隐藏
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    // 验证plugins被模拟
                    Object pluginsLength = page.evaluate("navigator.plugins.length");
                    assertThat(pluginsLength).isNotNull();
                    int pluginCount = Integer.parseInt(pluginsLength.toString());
                    assertThat(pluginCount).isGreaterThan(0);
                    
                    // 验证hardwareConcurrency被设置
                    Object hardwareConcurrency = page.evaluate("navigator.hardwareConcurrency");
                    assertThat(hardwareConcurrency).isEqualTo(8);
                    
                    // 验证deviceMemory被设置
                    Object deviceMemory = page.evaluate("navigator.deviceMemory");
                    assertThat(deviceMemory).isEqualTo(8);
                });
            }
        }

        @Test
        @DisplayName("自定义浏览器上下文应该与反检测脚本兼容")
        void shouldBeCompatibleWithCustomBrowserContext() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(2)) {
                manager.execute(config, context -> {
                    // 设置自定义User-Agent
                    context.setExtraHTTPHeaders(java.util.Map.of("Custom-Header", "TestValue"));
                }, page -> {
                    // 反检测脚本应该仍然生效
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    // 自定义头信息也应该生效
                    // 注意：这里我们无法直接验证HTTP头，但可以验证脚本注入正常
                    Object pluginsLength = page.evaluate("navigator.plugins.length");
                    assertThat(pluginsLength).isNotNull();
                });
            }
        }
    }

    @Nested
    @DisplayName("PlaywrightBrowserManager与反检测集成测试")
    class PlaywrightBrowserManagerStealthIntegrationTests {

        @Test
        @DisplayName("Browser连接池中的反检测脚本应该正确工作")
        void shouldWorkCorrectlyInBrowserPool() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightBrowserManager manager = new PlaywrightBrowserManager(config, 2)) {
                // 多次执行，验证反检测脚本在连接池中的一致性
                for (int i = 0; i < 3; i++) {
                    manager.execute(page -> {
                        Object webdriver = page.evaluate("navigator.webdriver");
                        // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                        
                        Object pluginsLength = page.evaluate("navigator.plugins.length");
                        assertThat(pluginsLength).isNotNull();
                        int pluginCount = Integer.parseInt(pluginsLength.toString());
                        assertThat(pluginCount).isGreaterThan(0);
                    });
                }
            }
        }

        @Test
        @DisplayName("并发执行时反检测脚本应该稳定工作")
        void shouldWorkStablyInConcurrentExecution() throws InterruptedException {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.LIGHT);
                    
            try (PlaywrightBrowserManager manager = new PlaywrightBrowserManager(config, 2)) {
                
                int threadCount = 4;
                Thread[] threads = new Thread[threadCount];
                Exception[] exceptions = new Exception[threadCount];
                
                for (int i = 0; i < threadCount; i++) {
                    final int index = i;
                    threads[i] = new Thread(() -> {
                        try {
                            manager.execute(page -> {
                                Object webdriver = page.evaluate("navigator.webdriver");
                                // webdriver应该被隐藏(null, undefined, 或 false)
                                assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                                
                                Object languages = page.evaluate("navigator.languages");
                                assertThat(languages.toString()).contains("zh-CN");
                            });
                        } catch (Exception e) {
                            exceptions[index] = e;
                        }
                    });
                }
                
                // 启动所有线程
                for (Thread thread : threads) {
                    thread.start();
                }
                
                // 等待所有线程完成
                for (Thread thread : threads) {
                    thread.join(5000);
                }
                
                // 验证没有异常
                for (int i = 0; i < threadCount; i++) {
                    assertThat(exceptions[i]).isNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("脚本注入机制集成测试")
    class ScriptInjectionIntegrationTests {

        @Test
        @DisplayName("轻量级脚本应该正确注入并生效")
        void shouldCorrectlyInjectLightScript() {
            String lightScript = StealthScriptProvider.getLightStealthScript();
            assertThat(lightScript).isNotNull();
            assertThat(lightScript).isNotEmpty();
            
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.LIGHT);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证轻量级脚本的效果
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    Object platform = page.evaluate("navigator.platform");
                    assertThat(platform).isEqualTo("Win32");
                    
                    Object languages = page.evaluate("navigator.languages");
                    assertThat(languages.toString()).contains("zh-CN");
                    assertThat(languages.toString()).contains("en");
                });
            }
        }

        @Test
        @DisplayName("完整脚本应该正确注入并生效")
        void shouldCorrectlyInjectFullScript() {
            String fullScript = StealthScriptProvider.getStealthScript();
            assertThat(fullScript).isNotNull();
            assertThat(fullScript).isNotEmpty();
            
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证完整脚本的效果
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    // 验证plugins模拟
                    Object pluginsLength = page.evaluate("navigator.plugins.length");
                    assertThat(pluginsLength).isNotNull();
                    int pluginCount = Integer.parseInt(pluginsLength.toString());
                    assertThat(pluginCount).isGreaterThan(0);
                    
                    // 验证Chrome PDF Viewer插件
                    Object pdfViewer = page.evaluate("""
                        Array.from(navigator.plugins).find(p => p.name === 'Chrome PDF Viewer')
                    """);
                    assertThat(pdfViewer).isNotNull();
                    
                    // 验证hardwareConcurrency
                    Object hardwareConcurrency = page.evaluate("navigator.hardwareConcurrency");
                    assertThat(hardwareConcurrency).isEqualTo(8);
                    
                    // 验证deviceMemory
                    Object deviceMemory = page.evaluate("navigator.deviceMemory");
                    assertThat(deviceMemory).isEqualTo(8);
                    
                    // 验证appName
                    Object appName = page.evaluate("navigator.appName");
                    assertThat(appName).isEqualTo("Netscape");
                });
            }
        }

        @Test
        @DisplayName("脚本注入不应该影响页面正常功能")
        void shouldNotAffectNormalPageFunctionality() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证基本DOM操作仍然正常
                    page.setContent("<html><body><div id='test'>Hello World</div></body></html>");
                    
                    String text = page.textContent("#test");
                    assertThat(text).isEqualTo("Hello World");
                    
                    // 验证JavaScript执行仍然正常
                    Object result = page.evaluate("1 + 1");
                    assertThat(result).isEqualTo(2);
                    
                    // 验证反检测脚本仍然生效
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                });
            }
        }
    }

    @Nested
    @DisplayName("WebGL和AudioContext集成测试")
    class WebGLAudioContextIntegrationTests {

        @Test
        @DisplayName("WebGL指纹修复应该正确集成")
        void shouldCorrectlyIntegrateWebGLFingerprinting() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证WebGL渲染器信息被修改
                    Object webglVendor = page.evaluate("""
                        (() => {
                            const canvas = document.createElement('canvas');
                            const gl = canvas.getContext('webgl');
                            if (gl) {
                                const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
                                if (debugInfo) {
                                    return gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL);
                                }
                            }
                            return null;
                        })()
                    """);
                    
                    if (webglVendor != null) {
                        assertThat(webglVendor).isEqualTo("Intel Inc.");
                    }
                    
                    Object webglRenderer = page.evaluate("""
                        (() => {
                            const canvas = document.createElement('canvas');
                            const gl = canvas.getContext('webgl');
                            if (gl) {
                                const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
                                if (debugInfo) {
                                    return gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
                                }
                            }
                            return null;
                        })()
                    """);
                    
                    if (webglRenderer != null) {
                        assertThat(webglRenderer).isEqualTo("Intel(R) UHD Graphics 630");
                    }
                });
            }
        }

        @Test
        @DisplayName("AudioContext指纹修复应该正确集成")
        void shouldCorrectlyIntegrateAudioContextFingerprinting() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证AudioContext baseLatency被修改
                    Object baseLatency = page.evaluate("""
                        (() => {
                            try {
                                const context = new (window.AudioContext || window.webkitAudioContext)();
                                return context.baseLatency;
                            } catch (e) {
                                return null;
                            }
                        })()
                    """);
                    
                    if (baseLatency != null) {
                        assertThat(Double.parseDouble(baseLatency.toString())).isEqualTo(0.00512);
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("权限和Chrome Runtime集成测试")
    class PermissionsAndChromeRuntimeIntegrationTests {

        @Test
        @DisplayName("权限API修复应该正确集成")
        void shouldCorrectlyIntegratePermissionsAPI() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证通知权限被设置为granted
                    Object notificationPermission = page.evaluate("""
                        navigator.permissions.query({name: 'notifications'})
                            .then(result => result.state)
                            .catch(() => null)
                    """);
                    
                    if (notificationPermission != null) {
                        assertThat(notificationPermission).isEqualTo("granted");
                    }
                });
            }
        }

        @Test
        @DisplayName("Chrome Runtime清理应该正确集成")
        void shouldCorrectlyIntegrateChromeRuntimeCleanup() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            try (PlaywrightManager manager = new PlaywrightManager(1)) {
                manager.execute(config, page -> {
                    // 验证chrome.runtime被清理
                    Object chromeRuntime = page.evaluate("""
                        typeof chrome !== 'undefined' && chrome.runtime
                    """);
                    
                    assertThat(chromeRuntime).isEqualTo(false);
                });
            }
        }
    }

    @Nested
    @DisplayName("静态方法集成测试")
    class StaticMethodIntegrationTests {

        @Test
        @DisplayName("静态方法executeWithPlaywright应该正确集成反检测")
        void shouldCorrectlyIntegrateStealthInStaticMethod() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.LIGHT);
                    
            Playwright playwright = null;
            try {
                playwright = Playwright.create();
                
                PlaywrightManager.executeWithPlaywright(config, null, page -> {
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    Object platform = page.evaluate("navigator.platform");
                    assertThat(platform).isEqualTo("Win32");
                }, playwright);
                
            } finally {
                if (playwright != null) {
                    playwright.close();
                }
            }
        }

        @Test
        @DisplayName("静态方法executeWithBrowser应该正确集成反检测")
        void shouldCorrectlyIntegrateStealthInBrowserStaticMethod() {
            PlaywrightConfig config = new PlaywrightConfig()
                    .setHeadless(true)
                    .setStealthMode(StealthMode.FULL);
                    
            Playwright playwright = null;
            Browser browser = null;
            try {
                playwright = Playwright.create();
                browser = PlaywrightManager.createBrowser(config, playwright);
                
                PlaywrightManager.executeWithBrowser(config, null, page -> {
                    Object webdriver = page.evaluate("navigator.webdriver");
                    // webdriver应该被隐藏(null, undefined, 或 false)
                    assertThat(webdriver == null || webdriver.equals(false) || "undefined".equals(webdriver.toString())).isTrue();
                    
                    Object pluginsLength = page.evaluate("navigator.plugins.length");
                    assertThat(pluginsLength).isNotNull();
                    int pluginCount = Integer.parseInt(pluginsLength.toString());
                    assertThat(pluginCount).isGreaterThan(0);
                }, browser);
                
            } finally {
                if (browser != null) {
                    browser.close();
                }
                if (playwright != null) {
                    playwright.close();
                }
            }
        }
    }
}