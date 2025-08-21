package cn.xuanyuanli.playwrightstealth;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import cn.xuanyuanli.playwrightstealth.pool.PlaywrightFactory;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author John Li
 */
public class PlaywrightManager implements AutoCloseable {

    private final GenericObjectPool<Playwright> pool;

    // 私有构造，禁止外部实例化
    public PlaywrightManager(int capacity) {
        GenericObjectPoolConfig<Playwright> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(capacity);
        config.setMinIdle(1);

        pool = new GenericObjectPool<>(new PlaywrightFactory(), config);
    }

    /**
     * 执行
     *
     * @param config       配置
     * @param pageConsumer 页面消费者
     */
    @SneakyThrows
    public void execute(PlaywrightConfig config, Consumer<Page> pageConsumer) {
        execute(config, null, pageConsumer);
    }

    /**
     * 执行
     *
     * @param config       配置
     * @param pageConsumer 页面消费者
     */
    @SneakyThrows
    public void execute(PlaywrightConfig config, Consumer<BrowserContext> browserContextConsumer, Consumer<Page> pageConsumer) {
        if (config == null) {
            config = new PlaywrightConfig();
        }

        Playwright playwright = pool.borrowObject();
        try {
            PlaywrightManager.execute(config, browserContextConsumer, pageConsumer, playwright);
        } finally {
            pool.returnObject(playwright);
        }
    }

    public static void execute(PlaywrightConfig config, Consumer<BrowserContext> browserContextConsumer, Consumer<Page> pageConsumer, Playwright playwright) {
        if (config == null) {
            config = new PlaywrightConfig();
        }

        try (Browser browser = getBrowser(config, playwright)) {
            execute(config, browserContextConsumer, pageConsumer, browser);
        }
    }

    public static void execute(PlaywrightConfig config, Consumer<BrowserContext> browserContextConsumer, Consumer<Page> pageConsumer, Browser browser) {
        if (config == null) {
            config = new PlaywrightConfig();
        }

        BrowserContext browserContext = browser.newContext(config.getNewContextOptions());
        if (browserContextConsumer != null) {
            browserContextConsumer.accept(browserContext);
        }
        Page page = browserContext.newPage();
        try {
            page.addInitScript(getInitScript());
            pageConsumer.accept(page);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            page.close();
            browserContext.close();
        }
    }

    public static Browser getBrowser(PlaywrightConfig config, Playwright playwright) {
        List<String> args = new ArrayList<>();
        if (config.isDisableImageRender()) {
            args.add("--blink-settings=imagesEnabled=false");
        }
        if (config.isDisableAutomationControlled()) {
            args.add("--disable-blink-features=AutomationControlled");
        }
        if (config.isDisableGpu()) {
            args.add("--disable-gpu");
        }
        if (config.isStartMaximized()) {
            args.add("--start-maximized");
        }
        LaunchOptions options = new LaunchOptions().setArgs(args);
        options.setHeadless(config.isHeadless()).setChromiumSandbox(config.isChromiumSandbox());
        if (config.getSlowMo() != null) {
            options.setSlowMo(config.getSlowMo());
        }
        if (config.getProxy() != null) {
            options.setProxy(config.getProxy());
        }
        return playwright.chromium().launch(options);
    }

    /**
     * 获取初始化脚本
     * <p>
     * 该方法返回一个复杂的JavaScript字符串，用于在Web环境中执行一系列的属性定义和模拟
     * <br>
     * 主要目的是为了在自动化测试中修改或模拟浏览器的某些属性和行为，以绕过各种检测机制
     * </p>
     *
     * @return JavaScript初始化脚本的字符串表示
     */
    private static String getInitScript() {
        return """
                // 1. 修改 navigator.webdriver
                  Object.defineProperty(navigator, 'webdriver', {
                    get: () => undefined,
                  });
                
                  // 2. 模拟 navigator.languages
                  Object.defineProperty(navigator, 'languages', {
                    get: () => ['en-US', 'en'], // 可根据需要修改
                  });
                
                  // 3. 模拟 navigator.plugins (和 mimeTypes)
                  const mockPlugins = [
                    { name: 'Chrome PDF Viewer', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' },
                    // ... 可以添加更多常见插件
                  ];
                
                  const mockMimeTypes = [
                      { type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format', enabledPlugin: mockPlugins[0] },
                      { type: 'application/x-nacl', suffixes: '', description: 'Native Client Executable', enabledPlugin: mockPlugins[1] },
                      { type: 'application/x-pnacl', suffixes: '', description: 'Portable Native Client Executable', enabledPlugin: mockPlugins[1] },
                      // ... 可以添加更多常见 MIME 类型
                  ];
                
                  Object.defineProperty(navigator, 'plugins', {
                    get: () => mockPlugins,
                  });
                
                  Object.defineProperty(navigator, 'mimeTypes', {
                      get: () => mockMimeTypes,
                  });
                
                  // 添加 plugins 和 mimeTypes 的 length, item, namedItem 方法
                  Object.defineProperty(navigator.plugins, 'length', {
                    get: () => navigator.plugins.length,
                  });
                  navigator.plugins.item = function(index) {
                    return this[index] || null;
                  };
                  navigator.plugins.namedItem = function(name) {
                    for (let i = 0; i < this.length; i++) {
                      if (this[i].name === name) {
                        return this[i];
                      }
                    }
                    return null;
                  };
                
                    Object.defineProperty(navigator.mimeTypes, 'length', {
                      get: () => navigator.mimeTypes.length,
                    });
                    navigator.mimeTypes.item = function(index) {
                      return this[index] || null;
                    };
                    navigator.mimeTypes.namedItem = function(name) {
                      for (let i = 0; i < this.length; i++) {
                        if (this[i].type === name) {
                          return this[i];
                        }
                      }
                      return null;
                    };
                
                
                  // 4. 模拟 navigator.platform (根据你的 userAgent 修改)
                  Object.defineProperty(navigator, 'platform', {
                    get: () => 'Win32',  // 示例，根据你的 userAgent 调整 (Win32, MacIntel, Linux x86_64 等)
                  });
                
                  // 5. 模拟 navigator.hardwareConcurrency
                  Object.defineProperty(navigator, 'hardwareConcurrency', {
                    get: () => 4, // 或其他合理的值，如 2, 8
                  });
                
                  // 6. 模拟 navigator.deviceMemory
                  Object.defineProperty(navigator, 'deviceMemory', {
                    get: () => 8, // 或其他合理的值，如 4, 16
                  });
                
                  // 7. 其他 navigator 属性 (可选，根据需要添加)
                  Object.defineProperty(navigator, 'appName', {
                    get: () => 'Netscape',
                  });
                
                   Object.defineProperty(navigator, 'product', {
                     get: () => 'Gecko',
                   });
                
                   Object.defineProperty(navigator, 'productSub', {
                     get: () => '20030107',
                   });
                
                  // 注意：userAgent 应该在 context 级别设置，而不是在这里
                  // 如果你非要在这里设置，也可以，但不推荐
                  // Object.defineProperty(navigator, 'userAgent', {
                  //   get: () => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36',
                  // });
                
                  // 8. 修复一些 WebGL 的检测 (有些网站会通过 WebGL 检测)
                    const getParameter = WebGLRenderingContext.prototype.getParameter;
                    WebGLRenderingContext.prototype.getParameter = function(parameter) {
                        // 假装 UNMASKED_VENDOR_WEBGL 是 Google
                        if (parameter === 37445) {
                            return 'Google Inc. (NVIDIA)';
                        }
                        // 假装 UNMASKED_RENDERER_WEBGL 是 NVIDIA 的显卡
                        if (parameter === 37446) {
                            return 'ANGLE (NVIDIA, NVIDIA GeForce GTX 1060 Direct3D11 vs_5_0 ps_5_0, D3D11-27.21.14.5671)';
                        }
                    return getParameter.call(this, parameter);
                  };
                
                  // 9. 修复一些 AudioContext 的检测
                  const original_AudioContext = window.AudioContext || window.webkitAudioContext;
                  if(original_AudioContext)
                  {
                      window.AudioContext = function() {
                          const context = new original_AudioContext(...arguments);
                          Object.defineProperty(context.baseLatency, 'value', {
                            get: () => 0.0025 // 模拟一个合理的延迟
                          });
                          return context;
                      }
                      window.webkitAudioContext = window.AudioContext; // 如果有 webkitAudioContext 也一样处理
                  }
                """;
    }

    @Override
    public void close() {
        pool.close();
    }
}

