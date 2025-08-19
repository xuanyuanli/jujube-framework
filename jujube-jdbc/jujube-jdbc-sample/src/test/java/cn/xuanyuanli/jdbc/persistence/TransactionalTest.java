package cn.xuanyuanli.jdbc.persistence;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import cn.xuanyuanli.jdbc.JujubeJdbcApp;
import cn.xuanyuanli.jdbc.entity.User;
import cn.xuanyuanli.jdbc.service.TransactionalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JujubeJdbcApp.class)
@ActiveProfiles({"test"})
public class TransactionalTest {

    @Autowired
    private TransactionalService transactionalService;

    @Autowired
    private UserDao userDao;

    @Test
    void tran() {
        transactionalService.updateUser(new User().setId(1L).setName("jack"));
        Assertions.assertEquals(userDao.findNameById(1L), "百度");
    }

    @Test
    void mutiThreadRead() {
        int max = 11;
        int priod = 20;
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "" + max * priod);
        LongStream.range(0, max * priod).parallel().forEach(i -> {
            long id = i % max + 1;
            Assertions.assertEquals(userDao.findById(id).getId(), id);
        });
    }

    @Test
    void mutiThreadWrite() {
        int priod = 100;
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "" + priod / 5);
        List<User> all = userDao.findAll();
        IntStream.range(0, priod).parallel().forEach(i -> {
            Long id = userDao.save(new User().setAge(i));
            User cuser = userDao.findById(id);
            Assertions.assertEquals(cuser.getAge(), i);
            userDao.deleteById(id);
        });
        Assertions.assertEquals(userDao.findAll(), all);
    }
}
