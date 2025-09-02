package cn.xuanyuanli.core.util.useragent;

/**
 * User-Agent 字符串解析工具类
 * <p>
 * 提供对 HTTP User-Agent 字符串的解析功能，能够从 User-Agent 中识别和提取
 * 浏览器信息、操作系统信息、渲染引擎信息等详细的客户端环境数据。
 * </p>
 * 
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>浏览器识别：</strong>识别 Chrome、Firefox、Safari、Edge、IE 等主流浏览器</li>
 * <li><strong>版本检测：</strong>提取浏览器和操作系统的详细版本信息</li>
 * <li><strong>操作系统识别：</strong>支持 Windows、macOS、Linux、Android、iOS 等系统</li>
 * <li><strong>渲染引擎分析：</strong>识别 WebKit、Gecko、Blink、Trident 等引擎</li>
 * <li><strong>设备类型判断：</strong>区分桌面设备、移动设备、平板设备</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 从 HTTP 请求中获取 User-Agent
 * String userAgentString = request.getHeader("User-Agent");
 * 
 * // 解析 User-Agent
 * UserAgent userAgent = UserAgentUtil.parse(userAgentString);
 * 
 * // 获取浏览器信息
 * Browser browser = userAgent.getBrowser();
 * System.out.println("浏览器: " + browser.getName());
 * System.out.println("版本: " + browser.getVersion());
 * 
 * // 获取操作系统信息
 * OS os = userAgent.getOs();
 * System.out.println("操作系统: " + os.getName());
 * 
 * // 获取渲染引擎
 * Engine engine = userAgent.getEngine();
 * System.out.println("引擎: " + engine.getName());
 * 
 * // 判断设备类型
 * Platform platform = userAgent.getPlatform();
 * if (platform.isMobile()) {
 *     System.out.println("移动设备访问");
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>应用场景：</strong>
 * <ul>
 * <li><strong>访问统计：</strong>网站访问用户的浏览器和系统分布统计</li>
 * <li><strong>兼容性处理：</strong>根据浏览器类型提供不同的页面或功能</li>
 * <li><strong>移动适配：</strong>检测移动设备并重定向到移动版网站</li>
 * <li><strong>安全分析：</strong>识别可疑的或过时的浏览器进行安全处理</li>
 * <li><strong>用户体验优化：</strong>根据设备特性提供最佳的用户界面</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>技术特点：</strong>
 * <ul>
 * <li><strong>高效解析：</strong>使用正则表达式和模式匹配，性能优秀</li>
 * <li><strong>规则更新：</strong>支持新浏览器和设备的识别规则扩展</li>
 * <li><strong>容错处理：</strong>对格式异常的 User-Agent 字符串提供降级处理</li>
 * </ul>
 * </p>
 *
 * @author looly
 * @see UserAgent
 * @see UserAgentParser
 * @see Browser
 * @see OS
 * @see Engine
 * @see Platform
 */
public class UserAgentUtil {

	/**
	 * 解析 User-Agent 字符串
	 * <p>
	 * 将 HTTP 请求中的 User-Agent 字符串解析为结构化的 UserAgent 对象，
	 * 提取其中包含的浏览器、操作系统、渲染引擎和平台信息。
	 * </p>
	 * 
	 * <p>
	 * <strong>解析内容包括：</strong>
	 * <ul>
	 * <li><strong>浏览器信息：</strong>名称、版本、厂商等</li>
	 * <li><strong>操作系统：</strong>系统名称、版本、架构信息</li>
	 * <li><strong>渲染引擎：</strong>引擎类型和版本</li>
	 * <li><strong>设备平台：</strong>桌面、移动、平板设备类型</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <strong>使用示例：</strong>
	 * <pre>{@code
	 * String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
	 *            "AppleWebKit/537.36 (KHTML, like Gecko) " +
	 *            "Chrome/91.0.4472.124 Safari/537.36";
	 * 
	 * UserAgent userAgent = UserAgentUtil.parse(ua);
	 * System.out.println(userAgent.getBrowser().getName()); // Chrome
	 * System.out.println(userAgent.getOs().getName());      // Windows 10
	 * System.out.println(userAgent.getEngine().getName());  // WebKit
	 * }</pre>
	 * </p>
	 * 
	 * <p>
	 * <strong>异常处理：</strong>
	 * <ul>
	 * <li>如果输入为 null 或空字符串，返回包含未知信息的 UserAgent 对象</li>
	 * <li>对于无法识别的 User-Agent 格式，提供默认值和降级处理</li>
	 * <li>解析过程中的异常不会中断程序，而是返回部分解析结果</li>
	 * </ul>
	 * </p>
	 *
	 * @param userAgentString HTTP User-Agent 头字符串，可以为 null
	 * @return 解析后的 UserAgent 对象，包含浏览器、系统、引擎等信息，不会返回 null
	 * @see UserAgent
	 * @see UserAgentParser#parse(String)
	 */
	public static UserAgent parse(String userAgentString) {
		return UserAgentParser.parse(userAgentString);
	}

}
