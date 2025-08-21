package cn.xuanyuanli.playwrightstealth;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import cn.xuanyuanli.playwrightstealth.pool.PlaywrightBrowserFactory;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author John Li
 */
public class PlaywrightBrowserManager implements AutoCloseable{

    private final GenericObjectPool<Browser> pool;
    private final PlaywrightConfig playwrightConfig;

    public PlaywrightBrowserManager(PlaywrightConfig playwrightConfig, int capacity) {
        this(null, playwrightConfig, capacity);
    }

    public PlaywrightBrowserManager(GenericObjectPoolConfig<Browser> config, PlaywrightConfig playwrightConfig, int capacity) {
        if (config == null) {
            config = new GenericObjectPoolConfig<>();
            config.setTestOnBorrow(true);
            config.setMaxTotal(capacity);
            config.setTimeBetweenEvictionRuns(Duration.ofMinutes(30));
        }
        this.playwrightConfig = playwrightConfig;
        pool = new GenericObjectPool<>(new PlaywrightBrowserFactory(playwrightConfig), config);
    }


    /**
     * 执行
     *
     * @param pageConsumer 页面消费者
     */
    @SneakyThrows
    public void execute(Consumer<Page> pageConsumer) {
        execute(null, pageConsumer);
    }

    /**
     * 执行
     *
     * @param pageConsumer 页面消费者
     */
    @SneakyThrows
    public void execute(Consumer<BrowserContext> browserContextConsumer, Consumer<Page> pageConsumer) {
        Browser browser = pool.borrowObject();
        try {
            PlaywrightManager.execute(playwrightConfig, browserContextConsumer, pageConsumer, browser);
        } finally {
            pool.returnObject(browser);
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}

