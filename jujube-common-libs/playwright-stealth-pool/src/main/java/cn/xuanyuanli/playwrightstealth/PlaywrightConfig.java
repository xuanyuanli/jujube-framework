package cn.xuanyuanli.playwrightstealth;

import cn.xuanyuanli.core.util.Jsons;
import com.microsoft.playwright.Browser.NewContextOptions;

import java.util.Objects;

import com.microsoft.playwright.options.Proxy;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author John Li
 */
@Data
@Accessors(chain = true)
public class PlaywrightConfig {

    /**
     * 禁用自动化控制。默认为true
     */
    private boolean disableAutomationControlled = true;
    /**
     * 禁用gpu。默认为true
     */
    private boolean disableGpu = true;
    /**
     * 禁用图像渲染。默认为true
     */
    private boolean disableImageRender = true;
    /**
     * 窗口最大化。默认为false
     */
    private boolean startMaximized = false;
    /**
     * 无头。默认为true
     */
    private boolean headless = true;
    /**
     * 启用沙箱模式。默认为false
     */
    private boolean chromiumSandbox = false;
    /**
     * 将 Playwright作减慢指定的毫秒数。很有用，这样您就可以看到发生了什么。
     */
    private Double slowMo;

    /**
     * 新上下文选项
     */
    private NewContextOptions newContextOptions = new NewContextOptions().setIgnoreHTTPSErrors(true)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36");
    /**
     * 代理
     */
    private Proxy proxy;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaywrightConfig config = (PlaywrightConfig) o;
        return disableAutomationControlled == config.disableAutomationControlled && disableGpu == config.disableGpu
                && disableImageRender == config.disableImageRender && startMaximized == config.startMaximized && headless == config.headless
                && chromiumSandbox == config.chromiumSandbox && Objects.equals(slowMo, config.slowMo) && Objects.equals(Jsons.toJson(newContextOptions),
                Jsons.toJson(config.newContextOptions)) && Objects.equals(Jsons.toJson(proxy), Jsons.toJson(config.proxy));
    }

    @Override
    public int hashCode() {
        return Objects.hash(disableAutomationControlled, disableGpu, disableImageRender, startMaximized, headless, chromiumSandbox, slowMo,
                Jsons.toJson(newContextOptions), Jsons.toJson(proxy));
    }
}
