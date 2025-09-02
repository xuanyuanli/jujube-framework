package cn.xuanyuanli.core.util.useragent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UserAgentUtil 测试")
class UserAgentUtilTest {

    @Nested
    @DisplayName("桌面浏览器解析测试")
    class DesktopBrowserParsingTests {

        @Test
        @DisplayName("parse_应该正确解析Chrome浏览器_当提供Windows Chrome User-Agent时")
        void parse_shouldParseChromeBrowser_whenWindowsChromeUserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Chrome");
            assertThat(ua.getVersion()).isEqualTo("14.0.835.163");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("535.1");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 7 or Windows Server 2008R2");
            assertThat(ua.getOsVersion()).isEqualTo("6.1");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析Windows 10 Chrome_当提供Windows 10 Chrome User-Agent时")
        void parse_shouldParseWindows10Chrome_whenWindows10ChromeUserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Chrome");
            assertThat(ua.getVersion()).isEqualTo("70.0.3538.102");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 10 or Windows Server 2016");
            assertThat(ua.getOsVersion()).isEqualTo("10.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析IE11浏览器_当提供IE11 User-Agent时")
        void parse_shouldParseIe11Browser_whenIe11UserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MSIE11");
            assertThat(ua.getVersion()).isEqualTo("11.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Trident");
            assertThat(ua.getEngineVersion()).isEqualTo("7.0");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 10 or Windows Server 2016");
            assertThat(ua.getOsVersion()).isEqualTo("10.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析Edge浏览器_当提供Edge User-Agent时")
        void parse_shouldParseEdgeBrowser_whenEdgeUserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.69 Safari/537.36 Edg/81.0.416.34";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MSEdge");
            assertThat(ua.getVersion()).isEqualTo("81.0.416.34");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 10 or Windows Server 2016");
            assertThat(ua.getOsVersion()).isEqualTo("10.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("移动端浏览器解析测试")
    class MobileBrowserParsingTests {

        @Test
        @DisplayName("parse_应该正确解析iPhone Safari_当提供iPhone Safari User-Agent时")
        void parse_shouldParseiPhoneSafari_wheniPhoneSafariUserAgentProvided() {
            // Arrange
            String uaStr = "User-Agent:Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Safari");
            assertThat(ua.getVersion()).isEqualTo("5.0.2");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("533.17.9");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getOsVersion()).isEqualTo("4_3_3");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析Android Chrome_当提供Miui10 Chrome User-Agent时")
        void parse_shouldParseAndroidChrome_whenMiui10ChromeUserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Linux; Android 9; MIX 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.80 Mobile Safari/537.36";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Chrome");
            assertThat(ua.getVersion()).isEqualTo("70.0.3538.80");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("9");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析华为手机原生浏览器_当提供华为原生浏览器User-Agent时")
        void parse_shouldParseHuaweiNativeBrowser_whenHuaweiNativeBrowserUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Linux; Android 10; EML-AL00 Build/HUAWEIEML-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Android Browser");
            assertThat(ua.getVersion()).isEqualTo("4.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("10");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析三星手机原生浏览器_当提供三星原生浏览器User-Agent时")
        void parse_shouldParseSamsungNativeBrowser_whenSamsungNativeBrowserUserAgentProvided() {
            // Arrange
            String uaString = "Dalvik/2.1.0 (Linux; U; Android 9; SM-G950U Build/PPR1.180610.011)";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Android Browser");
            assertThat(ua.getVersion()).isNull();
            assertThat(ua.getEngine().toString()).isEqualTo("Unknown");
            assertThat(ua.getEngineVersion()).isNull();
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("9");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }
    }

    @Nested
    @DisplayName("Windows Phone浏览器解析测试")
    class WindowsPhoneBrowserParsingTests {

        @Test
        @DisplayName("parse_应该正确解析Windows Phone IEMobile_当提供Lumia 520 User-Agent时")
        void parse_shouldParseWindowsPhoneIeMobile_whenLumia520UserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Mobile; Windows Phone 8.1; Android 4.0; ARM; Trident/7.0; Touch; rv:11.0; IEMobile/11.0; NOKIA; Lumia 520) like iPhone OS 7_0_3 Mac OS X AppleWebKit/537 (KHTML, like Gecko) Mobile Safari/537 ";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("IEMobile");
            assertThat(ua.getVersion()).isEqualTo("11.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Trident");
            assertThat(ua.getEngineVersion()).isEqualTo("7.0");
            assertThat(ua.getOs().toString()).isEqualTo("Windows Phone");
            assertThat(ua.getOsVersion()).isEqualTo("8.1");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows Phone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析Windows Phone Edge_当提供Lumia 950XL User-Agent时")
        void parse_shouldParseWindowsPhoneEdge_whenLumia950XLUserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows Phone 10.0; Android 6.0.1; Microsoft; Lumia 950XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Mobile Safari/537.36 Edge/15.14900";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MSEdge");
            assertThat(ua.getVersion()).isEqualTo("15.14900");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows Phone");
            assertThat(ua.getOsVersion()).isEqualTo("10.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows Phone");
            assertThat(ua.isMobile()).isTrue();
        }
    }

    @Nested
    @DisplayName("IE浏览器解析测试")
    class IEBrowserParsingTests {

        @Test
        @DisplayName("parse_应该正确解析IE8模拟器_当提供IE8 User-Agent时")
        void parse_shouldParseIe8Emulator_whenIe8UserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MSIE");
            assertThat(ua.getVersion()).isEqualTo("8.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Trident");
            assertThat(ua.getEngineVersion()).isEqualTo("4.0");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 7 or Windows Server 2008R2");
            assertThat(ua.getOsVersion()).isEqualTo("6.1");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("服务器系统浏览器解析测试")
    class ServerSystemBrowserParsingTests {

        @Test
        @DisplayName("parse_应该正确解析Windows Server 2012R2上的Chrome_当提供Server 2012R2 User-Agent时")
        void parse_shouldParseWindowsServer2012R2Chrome_whenServer2012R2UserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Chrome");
            assertThat(ua.getVersion()).isEqualTo("63.0.3239.132");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 8.1 or Winsows Server 2012R2");
            assertThat(ua.getOsVersion()).isEqualTo("6.3");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析Windows Server 2008R2上的IE11_当提供Server 2008R2 User-Agent时")
        void parse_shouldParseWindowsServer2008R2Ie11_whenServer2008R2UserAgentProvided() {
            // Arrange
            String uaStr = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaStr);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MSIE11");
            assertThat(ua.getVersion()).isEqualTo("11.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Trident");
            assertThat(ua.getEngineVersion()).isEqualTo("7.0");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 7 or Windows Server 2008R2");
            assertThat(ua.getOsVersion()).isEqualTo("6.1");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("中国APP浏览器解析测试")
    class ChineseAppBrowserParsingTests {

        /**
         * <a href="https://github.com/looly/hutool/issues/1177">UserAgentUtil无法解析微信浏览器ua</a>
         */
        @Test
        @DisplayName("parse_应该正确解析微信浏览器_当提供微信User-Agent时")
        void parse_shouldParseMicroMessenger_whenMicroMessengerUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Mobile/15A372 MicroMessenger/7.0.17(0x17001127) NetType/WIFI Language/zh_CN";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("MicroMessenger");
            assertThat(ua.getVersion()).isEqualTo("7.0.17");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("604.1.38");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getOsVersion()).isEqualTo("11_0");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析企业微信_当提供企业微信User-Agent时")
        void parse_shouldParseWorkWx_whenWorkWxUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 wxwork/3.0.31 MicroMessenger/7.0.1 Language/zh";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("wxwork");
            assertThat(ua.getVersion()).isEqualTo("3.0.31");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("605.1.15");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析QQ浏览器_当提供QQ浏览器User-Agent时")
        void parse_shouldParseQqBrowser_whenQqBrowserUserAgentProvided() {
            // Arrange
            String uaString = "User-Agent: MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("QQBrowser");
            assertThat(ua.getVersion()).isEqualTo("26");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("533.1");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("2.3.7");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析钉钉浏览器_当提供钉钉User-Agent时")
        void parse_shouldParseDingTalk_whenDingTalkUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/18A373 AliApp(DingTalk/5.1.33) com.laiwang.DingTalk/13976299 Channel/201200 language/zh-Hans-CN WK";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("DingTalk");
            assertThat(ua.getVersion()).isEqualTo("5.1.33");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("605.1.15");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getOsVersion()).isEqualTo("14_0");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析支付宝浏览器_当提供支付宝User-Agent时")
        void parse_shouldParseAlipay_whenAlipayUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Linux; U; Android 7.0; zh-CN; FRD-AL00 Build/HUAWEIFRD-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.3.8.909 UWS/2.10.2.5 Mobile Safari/537.36 UCBS/2.10.2.5 Nebula AlipayDefined(nt:WIFI,ws:360|0|3.0) AliApp(AP/10.0.18.062203) AlipayClient/10.0.18.062203 Language/zh-Hans useStatusBar/true";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Alipay");
            assertThat(ua.getVersion()).isEqualTo("10.0.18.062203");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("7.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析淘宝浏览器_当提供淘宝User-Agent时")
        void parse_shouldParseTaobao_whenTaobaoUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; MI 2C Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36 AliApp(TB/4.9.2) WindVane/5.2.2 TBANDROID/700342@taobao_android_4.9.2 720X1280";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Taobao");
            assertThat(ua.getVersion()).isEqualTo("4.9.2");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("4.4.4");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析UC浏览器桌面版_当提供UC桌面版User-Agent时")
        void parse_shouldParseUcBrowserDesktop_whenUcDesktopUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 UBrowser/4.0.3214.0 Safari/537.36";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("UCBrowser");
            assertThat(ua.getVersion()).isEqualTo("4.0.3214.0");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 7 or Windows Server 2008R2");
            assertThat(ua.getOsVersion()).isEqualTo("6.1");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析UC浏览器移动版_当提供UC移动版User-Agent时")
        void parse_shouldParseUcBrowserMobile_whenUcMobileUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X; zh-CN) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/16G102 UCBrowser/12.7.6.1251 Mobile AliApp(TUnionSDK/0.1.20.3)";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("UCBrowser");
            assertThat(ua.getVersion()).isEqualTo("12.7.6.1251");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.51.1");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getOsVersion()).isEqualTo("12_4_1");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析夸克浏览器_当提供夸克浏览器User-Agent时")
        void parse_shouldParseQuarkBrowser_whenQuarkUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X; zh-cn) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/16G102 Quark/3.6.2.993 Mobile";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("Quark");
            assertThat(ua.getVersion()).isEqualTo("3.6.2.993");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("601.1.46");
            assertThat(ua.getOs().toString()).isEqualTo("iPhone");
            assertThat(ua.getOsVersion()).isEqualTo("12_4_1");
            assertThat(ua.getPlatform().toString()).isEqualTo("iPhone");
            assertThat(ua.isMobile()).isTrue();
        }

        @Test
        @DisplayName("parse_应该正确解析企业微信桌面版_当提供企业微信桌面版User-Agent时")
        void parse_shouldParseWxworkDesktop_whenWxworkDesktopUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36 QBCore/4.0.1326.400 QQBrowser/9.0.2524.400 Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36 wxwork/3.1.10 (MicroMessenger/6.2) WindowsWechat";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("wxwork");
            assertThat(ua.getVersion()).isEqualTo("3.1.10");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Windows 10 or Windows Server 2016");
            assertThat(ua.getOsVersion()).isEqualTo("10.0");
            assertThat(ua.getPlatform().toString()).isEqualTo("Windows");
            assertThat(ua.isMobile()).isFalse();
        }

        @Test
        @DisplayName("parse_应该正确解析企业微信移动版_当提供企业微信移动版User-Agent时")
        void parse_shouldParseWxworkMobile_whenWxworkMobileUserAgentProvided() {
            // Arrange
            String uaString = "Mozilla/5.0 (Linux; Android 10; JSN-AL00 Build/HONORJSN-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045710 Mobile Safari/537.36 wxwork/3.1.10 ColorScheme/Light MicroMessenger/7.0.1 NetType/WIFI Language/zh Lang/zh";

            // Act
            UserAgent ua = UserAgentUtil.parse(uaString);

            // Assert
            assertThat(ua.getBrowser().toString()).isEqualTo("wxwork");
            assertThat(ua.getVersion()).isEqualTo("3.1.10");
            assertThat(ua.getEngine().toString()).isEqualTo("Webkit");
            assertThat(ua.getEngineVersion()).isEqualTo("537.36");
            assertThat(ua.getOs().toString()).isEqualTo("Android");
            assertThat(ua.getOsVersion()).isEqualTo("10");
            assertThat(ua.getPlatform().toString()).isEqualTo("Android");
            assertThat(ua.isMobile()).isTrue();
        }
    }

    @Nested
    @DisplayName("边界情况和异常处理测试")
    class EdgeCaseAndExceptionTests {

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        @DisplayName("parse_应该返回null_当提供无效User-Agent时")
        void parse_shouldReturnNull_whenInvalidUserAgentProvided(String input) {
            // Act
            UserAgent ua = UserAgentUtil.parse(input);

            // Assert
            assertThat(ua).isNull();
        }

        @Test
        @DisplayName("parse_应该返回null_当提供null参数时")
        void parse_shouldReturnNull_whenNullParameterProvided() {
            // Act
            UserAgent ua = UserAgentUtil.parse(null);

            // Assert
            assertThat(ua).isNull();
        }
    }
}
