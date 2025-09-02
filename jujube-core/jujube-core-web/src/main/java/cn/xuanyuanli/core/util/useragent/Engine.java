package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.core.util.Texts;

/**
 * 浏览器引擎信息类
 * 
 * <p>用于识别和解析User-Agent字符串中的浏览器引擎信息，包括引擎名称和版本号。
 * 支持主流的浏览器引擎，如Webkit、Chrome、Opera、Gecko等。</p>
 * 
 * <p>该类继承自UserAgentInfo，提供了引擎版本提取功能。</p>
 *
 * @author looly
 */
public class Engine extends UserAgentInfo {
	@Serial
	private static final long serialVersionUID = 1L;

	/** 
	 * 未知引擎类型的默认实例
	 * <p>当无法识别User-Agent中的引擎信息时使用此实例</p>
	 */
	public static final Engine UNKNOWN = new Engine(NAME_UNKNOWN, null);

	/**
	 * 支持的浏览器引擎类型列表
	 * 
	 * <p>包含了主流浏览器引擎的预定义实例，包括：</p>
	 * <ul>
	 * <li>Trident - Internet Explorer引擎</li>
	 * <li>Webkit - Safari等浏览器使用的引擎</li>
	 * <li>Chrome - Chrome浏览器引擎</li>
	 * <li>Opera - Opera浏览器引擎</li>
	 * <li>Presto - 旧版Opera使用的引擎</li>
	 * <li>Gecko - Firefox浏览器引擎</li>
	 * <li>KHTML - Konqueror浏览器引擎</li>
	 * <li>Konqueror - Linux下的浏览器引擎</li>
	 * <li>MIDP - 移动设备上的引擎</li>
	 * </ul>
	 */
	public static final List<Engine> ENGINES = new ArrayList<>(Arrays.asList(
			new Engine("Trident", "trident"),
			new Engine("Webkit", "webkit"),
			new Engine("Chrome", "chrome"),
			new Engine("Opera", "opera"),
			new Engine("Presto", "presto"),
			new Engine("Gecko", "gecko"),
			new Engine("KHTML", "khtml"),
			new Engine("Konqueror", "konqueror"),
			new Engine("MIDP", "MIDP")
	));

	/**
	 * 版本号匹配模式
	 * <p>用于从User-Agent字符串中提取引擎版本号的正则表达式模式</p>
	 */
	private final String versionPattern;

	/**
	 * 构造浏览器引擎实例
	 * 
	 * <p>创建一个新的引擎实例，同时初始化版本匹配模式。</p>
	 *
	 * @param name 引擎名称，如"Chrome", "Webkit"等
	 * @param regex 用于在User-Agent字符串中识别该引擎的关键字或正则表达式
	 */
	public Engine(String name, String regex) {
		super(name, regex);
		this.versionPattern = name + "[/\\- ]([\\d\\w.\\-]+)";
	}

	/**
	 * 从User-Agent字符串中提取引擎版本号
	 * 
	 * <p>使用预定义的版本匹配模式从User-Agent字符串中解析出引擎的版本信息。
	 * 如果当前引擎为未知类型或无法匹配到版本信息，则返回null。</p>
	 *
	 * @param userAgentString User-Agent字符串，通常来自HTTP请求头
	 * @return 引擎版本号字符串，如"98.0.4758.102"，如果无法提取则返回null
	 */
	public String getVersion(String userAgentString) {
		if(isUnknown()){
			return null;
		}
		String[] groups = Texts.getGroups(versionPattern, userAgentString, true);
		return groups.length > 1 ? groups[1] : null;
	}
}
