package cn.xuanyuanli.core.util.useragent;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import cn.xuanyuanli.core.util.Texts;

/**
 * User-agent信息
 *
 * @author looly
 * @since 4.2.1
 */
public class UserAgentInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名字未知
     */
    public static final String NAME_UNKNOWN = "Unknown";

    /**
     * 信息名称
     */
    @Getter
    private final String name;
    /**
     * 信息匹配模式
     */
    private final String pattern;

    /**
     * 构造
     *
     * @param name  名字
     * @param regex 表达式
     */
    public UserAgentInfo(String name, String regex) {
        this.name = name;
        this.pattern = regex;
    }

    /**
     * 指定内容中是否包含匹配此信息的内容
     *
     * @param content User-Agent字符串
     * @return 是否包含匹配此信息的内容
     */
    public boolean isMatch(String content) {
        return Texts.find(content, this.pattern, true);
    }

    /**
     * 是否为Unknown
     *
     * @return 是否为Unknown
     */
    public boolean isUnknown() {
        return NAME_UNKNOWN.equals(this.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserAgentInfo other = (UserAgentInfo) obj;
        if (name == null) {
            return other.name == null;
        } else {
            return name.equals(other.name);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
