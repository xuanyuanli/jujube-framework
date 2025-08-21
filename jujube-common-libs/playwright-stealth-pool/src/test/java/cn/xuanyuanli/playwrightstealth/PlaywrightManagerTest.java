package cn.xuanyuanli.playwrightstealth;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitUntilState;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import cn.xuanyuanli.core.util.Images;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class PlaywrightManagerTest {

    @Test
    void bonhams() {
        PlaywrightConfig config = new PlaywrightConfig();
        PlaywrightManager playwrightManager = new PlaywrightManager(1);
        playwrightManager.execute(config, page -> {
            page.navigate("https://www.bonhams.com/auction/31576/dr-and-mrs-hu-shih-chang-collection-of-chinese-art/",
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
            //            page.navigate("https://www.sothebys.com/en/buy/auction/2025/chinese-art", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            System.out.println(page.title());
            System.out.println(page.content());
        });
    }


    @Test
    void liveauctioneers() {
        PlaywrightConfig config = new PlaywrightConfig().setDisableImageRender(false);
        PlaywrightManager playwrightManager = new PlaywrightManager(1);
        playwrightManager.execute(config, page -> {
            // 创建一个 Promise 来等待关闭信号
            CompletableFuture<Void> closeSignal = new CompletableFuture<>();

            page.onResponse(response -> {
                if (response.url().contains("v2/subscribe")) {
                    System.out.println(response.url() + "\t" + response.text());

                    if (response.text().contains("exit")) {
                        closeSignal.complete(null); // 触发退出
                    }
                }
            });
            page.setDefaultNavigationTimeout(300000);
            // 设置其他操作的默认超时
            page.setDefaultTimeout(100000);
            page.navigate("https://www.liveauctioneers.com/console/367696", new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            // 阻塞直到收到退出信号
            try {
                closeSignal.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void img() {
        PlaywrightManager playwrightManager = new PlaywrightManager(1);
        playwrightManager.execute(null, page -> {
            String imageUrl = "https://image.invaluable.com/housePhotos/Applebrook/02/561002/H1209-L69424625.jpg";
            Response response = page.navigate(imageUrl);
            byte[] body = response.body();
            System.out.println(body.length);
            System.out.println(Images.isImageByTika(body));
        });
    }

    @Test
    void imgMulti() {
        PlaywrightManager playwrightManager = new PlaywrightManager(Runtime.getRuntime().availableProcessors());
        IntStream.range(0, 16).parallel().forEach(i -> playwrightManager.execute(null, page -> {
            String imageUrl = "https://image.invaluable.com/housePhotos/Applebrook/02/561002/H1209-L69424625.jpg";
            Response response = page.navigate(imageUrl);
            byte[] body = response.body();
            System.out.println(body.length + "\t" + Images.isImageByTika(body));
        }));
    }
}
