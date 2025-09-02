package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.core.util.Texts;

/**
 * 操作系统信息解析类
 * <p>用于从User-Agent字符串中识别和解析操作系统类型及版本信息</p>
 * <p>支持识别包括Windows、Mac OSX、Android、Linux、iOS等主流操作系统</p>
 * <p>继承自{@link UserAgentInfo}，提供操作系统特定的解析功能</p>
 *
 * @author looly
 */
public class OS extends UserAgentInfo {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 未知操作系统类型的默认实例
	 * <p>当无法识别User-Agent中的操作系统信息时返回此实例</p>
	 */
	public static final OS UNKNOWN = new OS(NAME_UNKNOWN, null);

	/**
	 * 支持的操作系统类型列表
	 * <p>包含了常见的操作系统及其识别规则，用于从User-Agent字符串中匹配操作系统信息</p>
	 * <p>支持的操作系统包括：Windows系列、Mac OSX、Android、Linux、iOS设备等</p>
	 */
	public static final List<OS> OSES = new ArrayList<>(Arrays.asList(
			//
			new OS("Windows 10 or Windows Server 2016", "windows nt 10\\.0", "windows nt (10\\.0)"),
			//
			new OS("Windows 8.1 or Winsows Server 2012R2", "windows nt 6\\.3", "windows nt (6\\.3)"),
			//
			new OS("Windows 8 or Winsows Server 2012", "windows nt 6\\.2", "windows nt (6\\.2)"),
			//
			new OS("Windows Vista", "windows nt 6\\.0", "windows nt (6\\.0)"),
			//
			new OS("Windows 7 or Windows Server 2008R2", "windows nt 6\\.1", "windows nt (6\\.1)"),
			//
			new OS("Windows 2003", "windows nt 5\\.2", "windows nt (5\\.2)"),
			//
			new OS("Windows XP", "windows nt 5\\.1", "windows nt (5\\.1)"),
			//
			new OS("Windows 2000", "windows nt 5\\.0", "windows nt (5\\.0)"),
			//
			new OS("Windows Phone", "windows (ce|phone|mobile)( os)?", "windows (?:ce|phone|mobile) (\\d+([._]\\d+)*)"),
			//
			new OS("Windows", "windows"),
			//
			new OS("OSX", "os x (\\d+)[._](\\d+)", "os x (\\d+([._]\\d+)*)"),
			//
			new OS("Android", "Android", "Android (\\d+([._]\\d+)*)"),
			//
			new OS("Linux", "linux"),
			//
			new OS("Wii", "wii", "wii libnup/(\\d+([._]\\d+)*)"),
			//
			new OS("PS3", "playstation 3", "playstation 3; (\\d+([._]\\d+)*)"),
			//
			new OS("PSP", "playstation portable", "Portable\\); (\\d+([._]\\d+)*)"),
			//
			new OS("iPad", "\\(iPad.*os (\\d+)[._](\\d+)", "\\(iPad.*os (\\d+([._]\\d+)*)"),
			//
			new OS("iPhone", "\\(iPhone.*os (\\d+)[._](\\d+)", "\\(iPhone.*os (\\d+([._]\\d+)*)"),
			//
			new OS("YPod", "iPod touch[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPod touch[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
			//
			new OS("YPad", "iPad[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPad[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
			//
			new OS("YPhone", "iPhone[\\s\\;]+iPhone.*os (\\d+)[._](\\d+)", "iPhone[\\s\\;]+iPhone.*os (\\d+([._]\\d+)*)"),
			//
			new OS("Symbian", "symbian(os)?"),
			//
			new OS("Darwin", "Darwin\\/([\\d\\w\\.\\-]+)", "Darwin\\/([\\d\\w\\.\\-]+)"),
			//
			new OS("Adobe Air", "AdobeAir\\/([\\d\\w\\.\\-]+)", "AdobeAir\\/([\\d\\w\\.\\-]+)"),
			//
			new OS("Java", "Java[\\s]+([\\d\\w\\.\\-]+)", "Java[\\s]+([\\d\\w\\.\\-]+)")
	));

	/**
	 * 添加自定义的操作系统类型
	 * <p>允许用户扩展支持的操作系统类型，添加自定义的识别规则</p>
	 * <p>此方法是线程安全的，使用synchronized修饰</p>
	 *
	 * @param name         操作系统名称
	 * @param regex        用于匹配User-Agent的关键字或正则表达式
	 * @param versionRegex 用于提取版本信息的正则表达式，可以为null
	 */
	@SuppressWarnings("unused")
	synchronized public static void addCustomOs(String name, String regex, String versionRegex) {
		OSES.add(new OS(name, regex, versionRegex));
	}

	/**
	 * 版本号匹配的正则表达式模式
	 * <p>用于从User-Agent字符串中提取操作系统的版本信息</p>
	 */
	private String versionPattern;

	/**
	 * 构造操作系统对象
	 * <p>创建一个不包含版本信息匹配规则的操作系统对象</p>
	 *
	 * @param name  操作系统名称
	 * @param regex 用于匹配User-Agent的关键字或正则表达式
	 */
	public OS(String name, String regex) {
		this(name, regex, null);
	}

	/**
	 * 构造操作系统对象
	 * <p>创建一个包含版本信息匹配规则的操作系统对象</p>
	 *
	 * @param name         操作系统名称
	 * @param regex        用于匹配User-Agent的关键字或正则表达式
	 * @param versionRegex 用于提取版本信息的正则表达式，可以为null
	 */
	public OS(String name, String regex, String versionRegex) {
		super(name, regex);
		if (null != versionRegex) {
			this.versionPattern = versionRegex;
		}
	}

	/**
	 * 获取操作系统版本信息
	 * <p>从给定的User-Agent字符串中提取操作系统的版本号</p>
	 * <p>如果当前操作系统类型为未知或未设置版本匹配规则，则返回null</p>
	 *
	 * @param userAgentString User-Agent字符串
	 * @return 操作系统版本号，如果无法获取则返回null
	 */
	public String getVersion(String userAgentString) {
		if(isUnknown() || null == this.versionPattern){
			// 无版本信息
			return null;
		}
		String[] groups = Texts.getGroups(versionPattern, userAgentString, true);
		return groups.length > 1 ? groups[1] : null;
	}
}
