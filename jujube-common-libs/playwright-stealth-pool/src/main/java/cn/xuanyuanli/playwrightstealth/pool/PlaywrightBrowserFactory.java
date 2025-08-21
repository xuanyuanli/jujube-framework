package cn.xuanyuanli.playwrightstealth.pool;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import cn.xuanyuanli.playwrightstealth.PlaywrightConfig;
import cn.xuanyuanli.playwrightstealth.PlaywrightManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import cn.xuanyuanli.core.util.Envs;

/**
 * @author John Li
 */
@Slf4j
public class PlaywrightBrowserFactory extends BasePooledObjectFactory<Browser> {

    PlaywrightConfig config;
    private static final ConcurrentMap<Browser, Playwright> CACHE = new ConcurrentHashMap<>();

    public PlaywrightBrowserFactory(PlaywrightConfig config) {
        if (config == null) {
            config = new PlaywrightConfig();
        }
        this.config = config;
    }

    @Override
    public Browser create() {
        Playwright playwright = Playwright.create();
        if (Envs.getEnv("playwright.sdk.debug") != null) {
            log.info("初始化playwright browser，config：{}", config);
        }
        Browser browser = PlaywrightManager.getBrowser(config, playwright);
        CACHE.put(browser, playwright);
        return browser;
    }

    @Override
    public PooledObject<Browser> wrap(Browser browser) {
        return new DefaultPooledObject<>(browser);
    }

    @Override
    public boolean validateObject(PooledObject<Browser> p) {
        Browser browser = p.getObject();
        try {
            browser.contexts();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void destroyObject(PooledObject<Browser> p) {
        Browser object = p.getObject();
        CACHE.get(object).close();
        CACHE.remove(object);
        object.close();
    }
}
