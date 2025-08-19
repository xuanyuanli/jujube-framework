package cn.xuanyuanli.jdbc.service;

import lombok.extern.slf4j.Slf4j;
import cn.xuanyuanli.jdbc.entity.User;
import cn.xuanyuanli.jdbc.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author John Li
 */
@Service
@Slf4j
public class TransactionalService {

    @Autowired
    private NormalService normalService;

    @Autowired
    private UserDao userDao;

    public void updateUser(User user) {
        try {
            normalService.updateUser(user);
        } catch (Exception e) {
            log.error("updateUser", e);
        }
    }
}
