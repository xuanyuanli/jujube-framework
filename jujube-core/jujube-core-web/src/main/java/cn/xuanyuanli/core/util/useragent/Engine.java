package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.core.util.Texts;

/**
 * 引擎对象
 *
 * @author looly
 * @since 4.2.1
 */
public class Engine extends UserAgentInfo {
	@Serial
	private static final long serialVersionUID = 1L;

	/** 未知 */
	public static final Engine UNKNOWN = new Engine(NAME_UNKNOWN, null);

	/**
	 * 支持的引擎类型
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

	private final String versionPattern;

	/**
	 * 构造
	 *
	 * @param name 引擎名称
	 * @param regex 关键字或表达式
	 */
	public Engine(String name, String regex) {
		super(name, regex);
		this.versionPattern = name + "[/\\- ]([\\d\\w.\\-]+)";
	}

	/**
	 * 获取引擎版本
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 版本
	 * @since 5.7.4
	 */
	public String getVersion(String userAgentString) {
		if(isUnknown()){
			return null;
		}
		String[] groups = Texts.getGroups(versionPattern, userAgentString, true);
		return groups.length > 1 ? groups[1] : null;
	}
}
