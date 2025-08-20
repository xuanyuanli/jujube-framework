package cn.xuanyuanli.core.util.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Spring MVC Controller扩展工具
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Controllers {

    /**
     * 成功
     */
    public static final String SUCCESS = "success";
    /**
     * 失败
     */
    public static final String FAIL = "fail";

    /**
     * 重定向
     *
     * @param url url
     * @return {@link String}
     */
    public static String redirect(String url) {
        return "redirect:" + url;
    }

}
