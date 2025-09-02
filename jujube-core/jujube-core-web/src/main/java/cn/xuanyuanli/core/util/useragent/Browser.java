package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.core.util.Texts;

/**
 * 浏览器信息类，用于解析和识别User-Agent字符串中的浏览器类型和版本信息
 * <p>
 * 支持识别主流浏览器包括：Chrome、Firefox、Safari、Edge、IE、Opera等，
 * 以及移动端浏览器和国内主流应用内置浏览器如微信、QQ、UC、钉钉、支付宝等
 * </p>
 *
 * @author looly
 */
public class Browser extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 未知浏览器常量，当无法识别User-Agent字符串时使用
     */
    public static final Browser UNKNOWN = new Browser(NAME_UNKNOWN, null, null);
    
    /**
     * 其它版本号匹配正则表达式模板，用于匹配常见的版本号格式
     */
    public static final String OTHER_VERSION = "[\\/ ]([\\d\\w\\.\\-]+)";

    /**
     * 支持的浏览器类型列表，包含所有已知浏览器的识别规则
     * <p>
     * 列表中的浏览器按优先级排序，特殊浏览器（如企业微信、微信等）需要优先匹配
     * </p>
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
     * 添加自定义的浏览器类型到支持列表中
     *
     * @param name         浏览器名称，用于标识浏览器类型
     * @param regex        用于匹配User-Agent字符串的正则表达式或关键字
     * @param versionRegex 用于提取浏览器版本号的正则表达式
     */
    @SuppressWarnings("unused")
    synchronized public static void addCustomBrowser(String name, String regex, String versionRegex) {
        BROWSERS.add(new Browser(name, regex, versionRegex));
    }

    /**
     * 版本号匹配正则表达式
     */
    private final String versionPattern;

    /**
     * 构造浏览器对象
     *
     * @param name         浏览器名称，用于标识浏览器类型
     * @param regex        用于匹配User-Agent字符串的正则表达式或关键字
     * @param versionRegex 用于提取浏览器版本号的正则表达式，如果传入OTHER_VERSION则会自动拼接浏览器名称
     */
    public Browser(String name, String regex, String versionRegex) {
        super(name, regex);
        if (OTHER_VERSION.equals(versionRegex)) {
            versionRegex = name + versionRegex;
        }
        versionPattern = versionRegex;
    }

    /**
     * 从User-Agent字符串中提取浏览器版本号
     *
     * @param userAgentString User-Agent字符串，通常来自HTTP请求头
     * @return 浏览器版本号字符串，如果无法提取或为未知浏览器则返回null
     */
    public String getVersion(String userAgentString) {
        if (isUnknown()) {
            return null;
        }
        String[] groups = Texts.getGroups(versionPattern, userAgentString, true);
        return groups.length > 1 ? groups[1] : null;
    }

    /**
     * 判断当前浏览器是否为移动端浏览器
     * <p>
     * 移动端浏览器包括：PSP、Yammer Mobile、Android Browser、IEMobile、
     * 微信浏览器、微信小程序、钉钉内置浏览器等
     * </p>
     *
     * @return 如果是移动端浏览器返回true，否则返回false
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
