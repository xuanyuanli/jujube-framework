package cn.xuanyuanli.playwrightstealth;

import com.microsoft.playwright.Response;
import java.util.stream.IntStream;
import cn.xuanyuanli.core.util.Images;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class PlaywrightBrowserManagerTest {

    @Test
    void imgMulti() {
        PlaywrightBrowserManager playwrightManager = new PlaywrightBrowserManager(null, 8);
        IntStream.range(0, 16).parallel().forEach(i -> playwrightManager.execute(null, page -> {
            String imageUrl = "https://image.invaluable.com/housePhotos/Applebrook/02/561002/H1209-L69424625.jpg";
            Response response = page.navigate(imageUrl);
            byte[] body = response.body();
            System.out.println(body.length + "\t" + Images.isImageByTika(body));
        }));
    }
}
