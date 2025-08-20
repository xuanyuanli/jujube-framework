package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.core.util.Texts;

/**
 * 浏览器对象
 *
 * @author looly
 * @since 4.2.1
 */
public class Browser extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 未知
     */
    public static final Browser UNKNOWN = new Browser(NAME_UNKNOWN, null, null);
    /**
     * 其它版本
     */
    public static final String OTHER_VERSION = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * 支持的浏览器类型
     */
    public static final List<Browser> BROWSERS = new ArrayList<>(Arrays.asList(
            // 部分特殊浏览器是基于安卓、Iphone等的，需要优先判断
            // 企业微信 企业微信使用微信浏览器内核,会包含 MicroMessenger 所以要放在前面
            new Browser("wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"),
            // 微信
            new Browser("MicroMessenger", "MicroMessenger", OTHER_VERSION),
            // 微信小程序
            new Browser("miniProgram", "miniProgram", OTHER_VERSION),
            // QQ浏览器
            new Browser("QQBrowser", "MQQBrowser", "MQQBrowser\\/([\\d\\w\\.\\-]+)"),
            // 钉钉内置浏览器
            new Browser("DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"),
            // 支付宝内置浏览器
            new Browser("Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"),
            // 淘宝内置浏览器
            new Browser("Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"),
            // UC浏览器
            new Browser("UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"),
            // 夸克浏览器
            new Browser("Quark", "Quark", OTHER_VERSION),

            new Browser("MSEdge", "Edge|Edg", "(?:edge|Edg)\\/([\\d\\w\\.\\-]+)"),
            new Browser("Chrome", "chrome", OTHER_VERSION),
            new Browser("Firefox", "firefox", OTHER_VERSION),
            new Browser("IEMobile", "iemobile", OTHER_VERSION),
            new Browser("Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"),
            new Browser("Opera", "opera", OTHER_VERSION),
            new Browser("Konqueror", "konqueror", OTHER_VERSION),
            new Browser("PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"),
            new Browser("PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"),
            new Browser("Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"),
            new Browser("Thunderbird", "thunderbird", OTHER_VERSION),
            new Browser("Netscape", "netscape", OTHER_VERSION),
            new Browser("Seamonkey", "seamonkey", OTHER_VERSION),
            new Browser("Outlook", "microsoft.outlook", OTHER_VERSION),
            new Browser("Evolution", "evolution", OTHER_VERSION),
            new Browser("MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"),
            new Browser("MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"),
            new Browser("Gabble", "Gabble", OTHER_VERSION),
            new Browser("Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"),
            new Browser("Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"),
            new Browser("Apache HTTP Client", "Apache\\\\-HttpClient", "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"),
            new Browser("BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)")
    ));

    /**
     * 添加自定义的浏览器类型
     *
     * @param name         浏览器名称
     * @param regex        关键字或表达式
     * @param versionRegex 匹配版本的正则
     * @since 5.7.4
     */
    @SuppressWarnings("unused")
    synchronized public static void addCustomBrowser(String name, String regex, String versionRegex) {
        BROWSERS.add(new Browser(name, regex, versionRegex));
    }

    private final String versionPattern;

    /**
     * 构造
     *
     * @param name         浏览器名称
     * @param regex        关键字或表达式
     * @param versionRegex 匹配版本的正则
     */
    public Browser(String name, String regex, String versionRegex) {
        super(name, regex);
        if (OTHER_VERSION.equals(versionRegex)) {
            versionRegex = name + versionRegex;
        }
        versionPattern = versionRegex;
    }

    /**
     * 获取浏览器版本
     *
     * @param userAgentString User-Agent字符串
     * @return 版本
     */
    public String getVersion(String userAgentString) {
        if (isUnknown()) {
            return null;
        }
        String[] groups = Texts.getGroups(versionPattern, userAgentString, true);
        return groups.length > 1 ? groups[1] : null;
    }

    /**
     * 是否移动浏览器
     *
     * @return 是否移动浏览器
     */
    public boolean isMobile() {
        final String name = this.getName();
        return "PSP".equals(name) ||
                "Yammer Mobile".equals(name) ||
                "Android Browser".equals(name) ||
                "IEMobile".equals(name) ||
                "MicroMessenger".equals(name) ||
                "miniProgram".equals(name) ||
                "DingTalk".equals(name);
    }
}
