package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * User-Agent信息对象
 * 
 * <p>用于封装从HTTP请求头中解析出的User-Agent字符串信息，包括：</p>
 * <ul>
 *   <li>浏览器类型和版本信息</li>
 *   <li>操作系统类型和版本信息</li>
 *   <li>渲染引擎类型和版本信息</li>
 *   <li>平台类型（桌面/移动设备）信息</li>
 * </ul>
 * 
 * <p>该类实现了{@link Serializable}接口，支持序列化操作。</p>
 * <p>使用Lombok注解自动生成getter/setter方法。</p>
 *
 * @author looly
 */
@Setter
@Getter
public class UserAgent implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 是否为移动平台
     */
    private boolean mobile;
	/**
	 * 浏览器类型
     */
	private Browser browser;
	/**
	 * 浏览器版本
     */
	private String version;

	/**
	 * 平台类型
     */
	private Platform platform;

	/**
	 * 系统类型
     */
	private OS os;
	/**
	 * 系统版本
     */
	private String osVersion;

	/**
	 * 引擎类型
     */
	private Engine engine;
	/**
	 * 引擎版本
     */
	private String engineVersion;

}
