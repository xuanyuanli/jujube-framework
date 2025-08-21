package cn.xuanyuanli.playwrightstealth.pool;

import com.microsoft.playwright.Playwright;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author John Li
 */
public class PlaywrightFactory extends BasePooledObjectFactory<Playwright> {


    @Override
    public Playwright create() {
        return Playwright.create();
    }

    @Override
    public PooledObject<Playwright> wrap(Playwright browser) {
        return new DefaultPooledObject<>(browser);
    }

    @Override
    public void destroyObject(PooledObject<Playwright> p) {
        p.getObject().close();
    }
}
