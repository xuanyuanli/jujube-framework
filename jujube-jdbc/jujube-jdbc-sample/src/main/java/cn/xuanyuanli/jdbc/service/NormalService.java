package cn.xuanyuanli.jdbc.service;

import cn.xuanyuanli.jdbc.entity.User;
import cn.xuanyuanli.jdbc.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author John Li
 */
@Service
public class NormalService {

    @Autowired
    private UserDao userDao;

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        userDao.update(user);
        throw new RuntimeException("更新失败");
    }
}
