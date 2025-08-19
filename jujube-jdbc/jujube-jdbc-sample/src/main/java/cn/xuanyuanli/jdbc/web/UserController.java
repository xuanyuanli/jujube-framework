package cn.xuanyuanli.jdbc.web;

import cn.xuanyuanli.jdbc.persistence.UserDao;
import cn.xuanyuanli.core.util.Jsons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author John Li
 */
@RestController
public class UserController {

    @Autowired
    UserDao userDao;

    @RequestMapping("/list")
    public String test() {
        return Jsons.toPrettyJson(userDao.findAll());
    }
}
