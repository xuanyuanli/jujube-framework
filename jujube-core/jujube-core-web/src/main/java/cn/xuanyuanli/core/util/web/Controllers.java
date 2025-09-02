package cn.xuanyuanli.core.util.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Spring MVC Controller 扩展工具类
 * <p>
 * 提供 Spring MVC Controller 开发中的常用工具方法和常量定义，简化控制器层代码编写。
 * 主要面向 Web 开发中的页面跳转、响应状态管理等场景，提供标准化的解决方案。
 * </p>
 * 
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>重定向工具：</strong>提供简洁的页面重定向方法</li>
 * <li><strong>状态常量：</strong>定义标准的成功/失败状态字符串</li>
 * <li><strong>响应简化：</strong>统一的响应格式和状态管理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * @Controller
 * public class UserController {
 *     
 *     @PostMapping("/user/save")
 *     public String saveUser(User user) {
 *         try {
 *             userService.save(user);
 *             // 保存成功后重定向到列表页
 *             return Controllers.redirect("/user/list");
 *         } catch (Exception e) {
 *             // 处理失败情况
 *             return Controllers.FAIL;
 *         }
 *     }
 *     
 *     @ResponseBody
 *     @PostMapping("/api/user/update")
 *     public Map<String, Object> updateUser(User user) {
 *         Map<String, Object> result = new HashMap<>();
 *         try {
 *             userService.update(user);
 *             result.put("status", Controllers.SUCCESS);
 *             result.put("message", "更新成功");
 *         } catch (Exception e) {
 *             result.put("status", Controllers.FAIL);
 *             result.put("message", "更新失败: " + e.getMessage());
 *         }
 *         return result;
 *     }
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>设计理念：</strong>
 * <ul>
 * <li><strong>约定优于配置：</strong>提供标准化的状态值和跳转方式</li>
 * <li><strong>简化开发：</strong>减少重复代码，提高开发效率</li>
 * <li><strong>统一规范：</strong>保证项目中控制器层代码的一致性</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>最佳实践：</strong>
 * <ul>
 * <li>使用 SUCCESS/FAIL 常量统一 API 响应状态</li>
 * <li>使用 redirect() 方法进行页面重定向，避免硬编码</li>
 * <li>结合其他 Spring MVC 特性，如 @ResponseBody、ModelAndView 等</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Controllers {

    /**
     * 成功状态常量
     * <p>
     * 用于标识操作成功的标准字符串。在 API 响应、页面跳转等场景中
     * 作为统一的成功标识，提高代码的一致性和可维护性。
     * </p>
     * 
     * @see #FAIL
     */
    public static final String SUCCESS = "success";
    /**
     * 失败状态常量
     * <p>
     * 用于标识操作失败的标准字符串。在异常处理、错误响应等场景中
     * 作为统一的失败标识，方便前端和其他系统识别处理。
     * </p>
     * 
     * @see #SUCCESS
     */
    public static final String FAIL = "fail";

    /**
     * 生成 Spring MVC 重定向字符串
     * <p>
     * 将给定的 URL 转换为 Spring MVC 可识别的重定向指令。
     * 使用 "redirect:" 前缀，告诉 Spring MVC 框架执行 HTTP 302 重定向。
     * </p>
     * 
     * <p>
     * <strong>使用场景：</strong>
     * <ul>
     * <li>表单提交成功后跳转到结果页面</li>
     * <li>用户登录成功后重定向到主页</li>
     * <li>权限检查失败重定向到登录页</li>
     * <li>RESTful API 中的资源创建成功后的跳转</li>
     * </ul>
     * </p>
     * 
     * <p>
     * <strong>注意事项：</strong>
     * <ul>
     * <li>重定向会导致客户端发起新的 HTTP 请求</li>
     * <li>POST 请求的表单数据在重定向后会丢失</li>
     * <li>可以重定向到绝对 URL 或相对 URL</li>
     * </ul>
     * </p>
     *
     * @param url 目标 URL，可以是相对路径或绝对路径
     * @return Spring MVC 重定向指令字符串，格式为 "redirect:" + url
     * @see org.springframework.web.servlet.mvc.support.RedirectAttributes
     */
    public static String redirect(String url) {
        return "redirect:" + url;
    }

}
