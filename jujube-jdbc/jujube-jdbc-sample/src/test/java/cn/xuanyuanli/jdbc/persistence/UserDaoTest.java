package cn.xuanyuanli.jdbc.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.xuanyuanli.jdbc.exception.DaoProxyException;
import cn.xuanyuanli.jdbc.JujubeJdbcApp;
import cn.xuanyuanli.jdbc.entity.User;
import cn.xuanyuanli.jdbc.persistence.UserDao.GetUsersByDepartIdPO;
import cn.xuanyuanli.jdbc.persistence.UserDao.QueryUserAgePO;
import cn.xuanyuanli.jdbc.persistence.UserDao.SexAndAgePO;
import cn.xuanyuanli.jdbc.persistence.UserDao.UserPO;
import cn.xuanyuanli.jdbc.persistence.UserDao.UserDepartmentPO;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.jdbc.pagination.Pageable;
import cn.xuanyuanli.jdbc.pagination.PageableRequest;
import cn.xuanyuanli.core.util.Collections3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JujubeJdbcApp.class)
@ActiveProfiles({"test"})
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void save() {
        long id = 9999L;
        int fInfoId = 12345555;
        userDao.save(new User().setFInfoId(fInfoId).setId(id));
        User user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        userDao.update(user.setFInfoId(10));
        user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(10);
        userDao.deleteById(id);
        user = userDao.findById(id);
        assertThat(user).isNull();
    }

    @Test
    public void updateNull() {
        long id = 9998L;
        userDao.save(new User().setName("name").setId(id));
        User user = userDao.findById(id);
        assertThat(user.getName()).isEqualTo("name");
        userDao.update(new User().setName(BaseEntity.STRING_NULL).setId(id));
        user = userDao.findById(id);
        assertThat(user.getName()).isNull();
        userDao.deleteById(id);
    }

    @Test
    public void updateExc() {
        assertThat(catchThrowable(() -> userDao.update(new User().setName("abc")))).isInstanceOf(DaoProxyException.class)
                .hasMessageContaining("Proxy class:cn.xuanyuanli.jdbc.base.BaseDao,method:update");
    }

    @Test
    public void saveOrUpdate() {
        Long uid = userDao.saveOrUpdate(new User().setName("abc"));
        assertThat(userDao.findNameById(uid)).isEqualTo("abc");
        userDao.saveOrUpdate(new User().setName("word").setId(uid));
        assertThat(userDao.findNameById(uid)).isEqualTo("word");
        userDao.deleteById(uid);
    }

    @Test
    public void batchUpdate() {
        List<User> users = new ArrayList<>();
        users.add(new User().setId(1001L).setName("1"));
        users.add(new User().setId(1002L).setName("2"));
        users.add(new User().setId(1003L).setName("3"));
        users.add(new User().setId(1004L).setName("4"));
        for (User user : users) {
            userDao.save(user);
        }
        users.clear();
        users.add(new User().setId(1001L).setName("112345678911234567891123456789").setAge(344444));
        users.add(new User().setId(1002L).setName("2"));
        users.add(new User().setId(1003L).setName("3"));
        users.add(new User().setId(1004L).setName("4"));
        userDao.batchUpdate(users);

        assertThat(userDao.findById(1001L).getAge()).isEqualTo(344444);
        assertThat(userDao.findNameById(1001L)).isEqualTo("112345678911234567891123456789");
        userDao.deleteById(1001L);
        userDao.deleteById(1002L);
        userDao.deleteById(1003L);
        userDao.deleteById(1004L);
    }

    @Test
    public void update() {
        long id = 9998L;
        int fInfoId = 12345555;
        userDao.save(new User().setFInfoId(fInfoId).setId(id).setAge(10).setName("John"));
        User user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        assertThat(user.getAge()).isEqualTo(10);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getId()).isEqualTo(id);

        userDao.update(new User().setId(id).setAge(18));
        user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        assertThat(user.getAge()).isEqualTo(18);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getId()).isEqualTo(id);
        userDao.deleteById(id);
        user = userDao.findById(id);
        assertThat(user).isNull();
    }

    @Test
    public void getName() {
        String name = userDao.getName(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void findNameById() {
        String name = userDao.findNameById(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void findNameAndAgeById() {
        User user = userDao.findNameAndAgeById(1);
        assertThat(user.getName()).isEqualTo("百度");
        assertThat(user.getAge()).isEqualTo(10);
    }

    @Test
    public void getNameById() {
        String name = userDao.getNameById(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void findNameByAge() {
        List<String> names = userDao.findNameByAge(5);
        assertThat(names).hasSize(2).contains("新浪", "人人网");
    }

    @Test
    public void findIdByAge() {
        long id = userDao.findIdByAge(5);
        assertThat(id).isEqualTo(6);
    }

    @Test
    public void findByName() {
        User user = userDao.findByName("宇宙女人");
        assertThat(user.getId()).isEqualTo(4);
    }

    @Test
    public void findByNameAndAge() {
        User user = userDao.findByNameAndAge("宇宙女人", 999);
        assertThat(user.getId()).isEqualTo(4);
    }

    @Test
    public void findByNameAndAgeEx() {
        Assertions.assertThrows(DaoProxyException.class, () -> {
            User user = userDao.findByNameAndAge("", 999);
            assertThat(user.getId()).isEqualTo(4);
        });
    }

    @Test
    public void findByNameLike() {
        List<User> users = userDao.findByNameLike("%人%");
        assertThat(users).hasSize(3);
        List<String> names = Collections3.extractToListString(users, "name");
        assertThat(names).contains("女人宇宙", "宇宙女人", "人人网");
    }

    @Test
    public void findByNameNotLike() {
        List<User> users = userDao.findByNameNotLike("%人%");
        assertThat(users).hasSize(8);
        List<String> names = Collections3.extractToListString(users, "name");
        assertThat(names).contains("百度", "阿里");
    }

    @Test
    public void findByDepartmentIdIn() {
        List<User> users = userDao.findByDepartmentIdIn(Lists.newArrayList(2L, 3L));
        assertThat(users).hasSize(7);
    }

    @Test
    public void findByIdGtOrderByAgeDesc() {
        List<User> users = userDao.findByIdGtOrderByAgeDesc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("长白山");
    }

    @Test
    public void findByIdGtOrderByAgeAsc() {
        List<User> users = userDao.findByIdGtOrderByAgeAsc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("日本");
    }

    @Test
    public void findByIdGtOrderById() {
        List<User> users = userDao.findByIdGtOrderById(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("女人宇宙");
    }

    @Test
    public void findByIdGtGroupById() {
        List<User> users = userDao.findByIdGtGroupById(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("女人宇宙");
    }

    @Test
    public void findAllGroupById() {
        List<User> list = userDao.findAllGroupById();
        assertThat(list.size()).isEqualTo(11);
    }

    @Test
    public void findAllGroupByIdLimit1() {
        User user = userDao.findAllGroupByIdLimit1();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void findAllGroupByIdLimit() {
        List<User> users = userDao.findAllGroupByIdLimit(2);
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void getCountByNameLike() {
        int count = userDao.getCountByNameLike("%人%");
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void getCountByNameLikeGroupById() {
        long count = userDao.getCountByNameLikeGroupById("%人%");
        assertThat(count).isEqualTo(3L);
    }

    @Test
    public void getSumOfAgeByNameLike() {
        double sumOfAge = userDao.getSumOfAgeByNameLike("%人%");
        assertThat(sumOfAge).isEqualTo(1013);
    }

    @Test
    public void getSumOfAgeByName() {
        int sumOfAge = userDao.getSumOfAgeByName("女人宇宙");
        assertThat(sumOfAge).isEqualTo(9);
    }

    @Test
    public void queryAgeCount() {
        long count = userDao.queryAgeCount(10, 1);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void queryAgeCount2() {
        int count2 = userDao.queryAgeCount2(10);
        assertThat(count2).isEqualTo(3);
    }

    @Test
    public void queryUserName() {
        String name = userDao.queryUserName(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void queryUserAge() {
        QueryUserAgePO record = userDao.queryUserAge(10);
        assertThat(record.getId()).isEqualTo(1L);
    }

    @Test
    public void queryUserDepartment() {
        List<UserPO> records = userDao.queryUserByDepartmentId(1);
        assertThat(records.size()).isEqualTo(4);
    }

    @Test
    public void queryIdDepartment() {
        List<Long> records = userDao.queryIdByDepartmentId(1);
        assertThat(records.size()).isEqualTo(4);
    }

    @Test
    public void pageForUserList() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "人");
        map.put("age", 1);
        map.put("ids", Lists.newArrayList(1, 2, 3, 4, 5, 6));
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<UserPO> pageable = userDao.pageForUserList(map, request);
        assertThat(pageable.getTotalElements()).isEqualTo(2);
        assertThat(pageable.getData().get(0).toString()).isEqualTo("UserDao.UserPO(id=3, name=女人宇宙, age=9, departmentId=3, sex=1)");
        assertThat(pageable.getData().get(0).getId()).isEqualTo(3L);
        assertThat(pageable.getData().get(1).getId()).isEqualTo(4L);
    }

    @Test
    public void pageForUserListOfOrder() {
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<UserPO> pageable = userDao.pageForUserListOfOrder(new HashMap<>(), request);
        assertThat(pageable.getTotalElements()).isEqualTo(11L);
        assertThat(pageable.getData().get(0).getId()).isEqualTo(11L);
    }

    @Test
    public void findAgeById() {
        BigDecimal age = userDao.findAgeById(1);
        assertThat(age).isEqualTo(new BigDecimal("10"));

        age = userDao.findAgeById(11111111);
        assertThat(age).isNull();
    }

    @Test
    public void findDepartmentIdById() {
        Long departmentId = userDao.findDepartmentIdById(1);
        assertThat(departmentId).isEqualTo(1);

        departmentId = userDao.findDepartmentIdById(1111111111);
        assertThat(departmentId).isNull();
    }

    @Test
    public void findByIdIn() {
        List<User> list = userDao.findByIdIn(new long[]{1, 2, 3});
        assertThat(list).isNotNull();
    }

    @Test
    public void findByAgeIn() {
        List<User> list = userDao.findByAgeIn(Lists.newArrayList(10, 20, 9));
        assertThat(list).isNotNull();
    }

    @Test
    public void queryUserByIds() {
        List<UserPO> list = userDao.queryUserByIds(Lists.newArrayList(10L));
        assertThat(list.get(0).getName()).isEqualTo("美国");
    }

    @Test
    public void pageForUserUnionQuery2() {
        PageableRequest request = new PageableRequest(1, 10);
        HashMap<String, Object> map = new HashMap<>();
        map.put("age", 20);
        map.put("departmentId", 1);
        Pageable<UserPO> page = userDao.pageForUserUnionQuery2(map, request);
        assertThat(page.getData().size()).isEqualTo(6);
    }

    @Test
    public void pageForUserUnionQuery3() {
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<UserPO> records = userDao.pageForUserUnionQuery3(new HashMap<>(), request);
        assertThat(records.getData().size()).isEqualTo(9);
    }

    @Test
    public void getUserDepartById() {
        UserDepartmentPO bo = userDao.getUserDepartById(1);
        assertThat(bo.getUserId()).isEqualTo(1);
        assertThat(bo.getDepartId()).isEqualTo(1);
        assertThat(bo.getDepartName()).isEqualTo("公司");
    }

    @Test
    public void getUsersByDepartId() {
        List<GetUsersByDepartIdPO> list = userDao.getUsersByDepartId(1);
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.get(0).getUserId()).isEqualTo(1);
        assertThat(list.get(1).getDepartId()).isEqualTo(1);
        assertThat(list.get(2).getDepartName()).isEqualTo("公司");
    }

    @Test
    public void findAny100ByDepartmentId() {
        List<UserDepartmentPO> list = userDao.findAny100ByDepartmentId(1);
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.get(0).getUserId()).isEqualTo(1);
        assertThat(list.get(2).getDepartName()).isEqualTo("公司");
    }

    @Test
    public void findAllGroupByDepartmentId() {
        List<Long> list = userDao.findAllGroupByDepartmentId();
        assertThat(list.size()).isEqualTo(3);
    }

    @Test
    public void findAny21ByAge() {
        List<Long> list = userDao.findAny21ByAge(10);
        assertThat(list.size()).isEqualTo(3);
    }

    @Test
    public void findAny1BySex() {
        Integer sex = userDao.findAny1BySex(1);
        assertThat(sex).isEqualTo(1);

        sex = userDao.findAny1BySex(2);
        assertThat(sex).isNull();
    }

    @Test
    public void findAny1BySexAndAge() {
        int sex = userDao.findAny1BySexAndAge(1, 9);
        assertThat(sex).isEqualTo(1);

        sex = userDao.findAny1BySexAndAge(2, 10);
        assertThat(sex).isEqualTo(0);
    }

    @Test
    void findSexAndAgeById() {
        SexAndAgePO sexAndAge = userDao.findSexAndAgeById(1);
        assertThat(sexAndAge.getSex()).isEqualTo(0);
        assertThat(sexAndAge.getAge()).isEqualTo(10);
    }

    @Test
    void findSexAndAgeByDepartmentId() {
        List<SexAndAgePO> list = userDao.findSexAndAgeByDepartmentId(1);
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.get(0).getSex()).isEqualTo(0);
        assertThat(list.get(1).getAge()).isEqualTo(20);
    }

    @Test
    void findByIds(){
        List<Long> ids = userDao.findIds();
        assertThat(ids.size()).isEqualTo(11);
    }
}
