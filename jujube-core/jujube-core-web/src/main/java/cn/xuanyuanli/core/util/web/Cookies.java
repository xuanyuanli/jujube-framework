package cn.xuanyuanli.core.util.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;
import cn.xuanyuanli.core.util.Dates;

/**
 * HTTP Cookie 操作工具类
 * <p>
 * 基于 Jakarta Servlet API 实现的 Cookie 管理工具，提供简洁易用的 Cookie 读取、设置、
 * 删除等操作。封装了常见的 Cookie 使用场景，简化 Web 应用中的状态管理和用户会话处理。
 * </p>
 * 
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>Cookie 读取：</strong>从 HTTP 请求中获取指定名称的 Cookie 值</li>
 * <li><strong>Cookie 设置：</strong>向 HTTP 响应中添加 Cookie，支持自定义过期时间</li>
 * <li><strong>会话 Cookie：</strong>创建浏览器关闭时自动失效的会话级 Cookie</li>
 * <li><strong>定时 Cookie：</strong>设置在特定时间（如当日结束）自动过期的 Cookie</li>
 * <li><strong>Cookie 删除：</strong>通过设置过期时间为 0 来删除现有 Cookie</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * @Controller
 * public class UserController {
 *     
 *     @PostMapping("/login")
 *     public String login(HttpServletRequest request, HttpServletResponse response) {
 *         // 用户登录成功后设置记住密码的 Cookie
 *         Cookies.addCookieForDayEnded(response, "remember_user", "12345");
 *         
 *         // 设置会话级的登录状态 Cookie
 *         Cookies.addCookieForSession(response, "login_status", "true");
 *         
 *         return "redirect:/dashboard";
 *     }
 *     
 *     @GetMapping("/profile")
 *     public String profile(HttpServletRequest request, Model model) {
 *         // 读取用户偏好设置
 *         String theme = Cookies.getCookie(request, "user_theme");
 *         if (theme != null) {
 *             model.addAttribute("theme", theme);
 *         }
 *         
 *         return "profile";
 *     }
 *     
 *     @PostMapping("/logout")
 *     public String logout(HttpServletResponse response) {
 *         // 删除登录相关的 Cookie
 *         Cookies.delCookie(response, "remember_user");
 *         Cookies.delCookie(response, "login_status");
 *         
 *         return "redirect:/login";
 *     }
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>安全特性：</strong>
 * <ul>
 * <li><strong>HTTPS 安全：</strong>自动为 Cookie 设置 Secure 标志，确保仅在 HTTPS 连接中传输</li>
 * <li><strong>参数验证：</strong>对 Cookie 名称进行空值检查，避免无效操作</li>
 * <li><strong>标准兼容：</strong>遵循 HTTP Cookie 规范，确保跨浏览器兼容性</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>应用场景：</strong>
 * <ul>
 * <li><strong>用户会话：</strong>存储登录状态、会话标识等临时信息</li>
 * <li><strong>用户偏好：</strong>保存主题设置、语言偏好、显示配置等</li>
 * <li><strong>购物车：</strong>临时存储商品选择和购买意图</li>
 * <li><strong>访问跟踪：</strong>记录用户访问历史和行为分析数据</li>
 * <li><strong>自动登录：</strong>实现"记住密码"功能的安全令牌存储</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>Cookie 大小限制通常为 4KB，不适合存储大量数据</li>
 * <li>敏感信息应加密后再存储到 Cookie 中</li>
 * <li>设置了 Secure 标志的 Cookie 仅在 HTTPS 环境下有效</li>
 * <li>浏览器可能限制单个域名下的 Cookie 数量</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 * @see jakarta.servlet.http.Cookie
 * @see jakarta.servlet.http.HttpServletRequest#getCookies()
 * @see jakarta.servlet.http.HttpServletResponse#addCookie(Cookie)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Cookies {

    /**
     * 从 HTTP 请求中获取指定名称的 Cookie 值
     * <p>
     * 遍历请求中的所有 Cookie，查找与指定名称匹配的 Cookie 并返回其值。
     * 如果存在多个同名的 Cookie，返回第一个匹配的值。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * String userId = Cookies.getCookie(request, "user_id");
     * if (userId != null) {
     *     // 处理用户ID
     *     User user = userService.getUserById(userId);
     * }
     * }</pre>
     * </p>
     *
     * @param request HTTP 请求对象，不可为 null
     * @param key     要查找的 Cookie 名称，不可为空
     * @return Cookie 的值，如果未找到则返回 null
     * @throws IllegalArgumentException 当 key 为空时
     * @see Cookie#getName()
     * @see Cookie#getValue()
     */
    public static String getCookie(HttpServletRequest request, String key) {
        Validate.notBlank(key);

        Cookie[] cookies = request.getCookies();
        Cookie ck = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    ck = cookie;
                    break;
                }
            }
        }
        return ck != null ? ck.getValue() : null;
    }

    /**
     * 向 HTTP 响应中添加 Cookie
     * <p>
     * 创建一个新的 Cookie 并添加到 HTTP 响应中。Cookie 会自动设置 Secure 标志，
     * 确保仅在 HTTPS 连接中传输，提高安全性。
     * </p>
     * 
     * <p>
     * <strong>maxAge 参数说明：</strong>
     * <ul>
     * <li><strong>正数：</strong>指定 Cookie 的存活时间（秒）</li>
     * <li><strong>0：</strong>立即删除 Cookie</li>
     * <li><strong>-1：</strong>会话级 Cookie，浏览器关闭时删除</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 设置一个保存 24 小时的 Cookie
     * Cookies.addCookie(response, "user_pref", "dark_theme", 24 * 60 * 60);
     * 
     * // 设置会话级 Cookie
     * Cookies.addCookie(response, "session_token", "abc123", -1);
     * }</pre>
     * </p>
     *
     * @param response HTTP 响应对象，不可为 null
     * @param key      Cookie 名称，不可为空
     * @param value    Cookie 值，可以为 null 或空字符串
     * @param maxAge   Cookie 存活时间（秒），-1 表示会话级，0 表示立即删除
     * @throws IllegalArgumentException 当 key 为空时
     * @see Cookie#setMaxAge(int)
     * @see Cookie#setSecure(boolean)
     */
    public static void addCookie(HttpServletResponse response, String key, String value, int maxAge) {
        Validate.notBlank(key);

        Cookie ck = new Cookie(key, value);
        ck.setSecure(true);
        ck.setMaxAge(maxAge);
        response.addCookie(ck);
    }

    /**
     * 添加会话级 Cookie
     * <p>
     * 创建一个仅在当前浏览器会话期间有效的 Cookie。当用户关闭浏览器时，
     * 该 Cookie 会被自动删除，适合存储临时的会话信息。
     * </p>
     * 
     * <p>
     * <strong>常用场景：</strong>
     * <ul>
     * <li>用户登录状态标记</li>
     * <li>购物车临时数据</li>
     * <li>表单填写进度保存</li>
     * <li>临时的用户偏好设置</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 用户登录成功后设置会话标记
     * Cookies.addCookieForSession(response, "login_status", "true");
     * 
     * // 保存临时的用户选择
     * Cookies.addCookieForSession(response, "temp_language", "en");
     * }</pre>
     * </p>
     *
     * @param response HTTP 响应对象，不可为 null
     * @param key      Cookie 名称，不可为空
     * @param value    Cookie 值，可以为 null 或空字符串
     * @see #addCookie(HttpServletResponse, String, String, int)
     */
    public static void addCookieForSession(HttpServletResponse response, String key, String value) {
        addCookie(response, key, value, -1);
    }

    /**
     * 添加当日结束时过期的 Cookie
     * <p>
     * 创建一个在当天结束时自动过期的 Cookie。实际过期时间为当天 23:59:59，
     * 适合存储当日有效的临时数据或状态信息。
     * </p>
     * 
     * <p>
     * <strong>应用场景：</strong>
     * <ul>
     * <li>每日签到状态记录</li>
     * <li>当日有效的优惠券或活动标记</li>
     * <li>临时的访问权限或状态</li>
     * <li>当日首次访问的欢迎消息控制</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 设置每日签到标记
     * Cookies.addCookieForDayEnded(response, "daily_checkin", "completed");
     * 
     * // 设置当日有效的特殊状态
     * Cookies.addCookieForDayEnded(response, "promo_viewed", "true");
     * }</pre>
     * </p>
     *
     * @param response HTTP 响应对象，不可为 null
     * @param key      Cookie 名称，不可为空
     * @param value    Cookie 值，可以为 null 或空字符串
     * @see Dates#endOfToday()
     * @see #addCookie(HttpServletResponse, String, String, int)
     */
    public static void addCookieForDayEnded(HttpServletResponse response, String key, String value) {
        addCookie(response, key, value, (int) Dates.endOfToday());
    }

    /**
     * 删除指定名称的 Cookie
     * <p>
     * 通过设置 Cookie 的过期时间为 0 来删除现有的 Cookie。
     * 浏览器接收到过期时间为 0 的 Cookie 后会立即将其从本地存储中移除。
     * </p>
     * 
     * <p>
     * <strong>常用场景：</strong>
     * <ul>
     * <li>用户退出登录时清除登录状态</li>
     * <li>取消“记住密码”功能</li>
     * <li>清除过期或无效的用户数据</li>
     * <li>重置用户偏好设置</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * // 用户退出时清除相关 Cookie
     * Cookies.delCookie(response, "user_token");
     * Cookies.delCookie(response, "remember_me");
     * Cookies.delCookie(response, "user_preferences");
     * }</pre>
     * </p>
     *
     * @param response HTTP 响应对象，不可为 null
     * @param key      要删除的 Cookie 名称，不可为空
     * @see #addCookie(HttpServletResponse, String, String, int)
     */
    public static void delCookie(HttpServletResponse response, String key) {
        addCookie(response, key, "", 0);
    }
}
