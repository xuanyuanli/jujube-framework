package cn.xuanyuanli.jdbc.persistence;

import java.math.BigInteger;
import java.util.List;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.jdbc.JujubeJdbcApp;
import cn.xuanyuanli.jdbc.entity.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JujubeJdbcApp.class)
@ActiveProfiles({"test"})
public class DepartmentDaoTest {

    public static final int COUNT = 5;
    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void saveOrUpdate() {
        Department department = new Department();
        department.setName("abc");
        BigInteger id = departmentDao.saveOrUpdate(department);
        Assertions.assertThat(departmentDao.getCountByIdGt(0)).isEqualTo(COUNT + 1);
        departmentDao.deleteById(id);
        Assertions.assertThat(departmentDao.getCountByIdGt(0)).isEqualTo(COUNT);
    }

    @Test
    public void save() {
        Department department = new Department();
        department.setName("abc");
        BigInteger id = departmentDao.save(department);
        Assertions.assertThat(departmentDao.getCountByIdGt(0)).isEqualTo(COUNT + 1);
        departmentDao.deleteById(id);
        Assertions.assertThat(departmentDao.getCountByIdGt(0)).isEqualTo(COUNT);
    }

    @Test
    void findIdByNameLike() {
        List<Long> ids = departmentDao.findIdByNameLike("%'%");
        Assertions.assertThat(ids).hasSize(1);

        // H2数据查询斜杠的时候，因为这一原因，要输出4个斜杠；mysql不用如此
        ids = departmentDao.findIdByNameLike("%\\\\%");
        Assertions.assertThat(ids).hasSize(2);
    }

    @Test
    void findIdByNameLikeAndIdIn() {
        List<Long> ids = departmentDao.findIdByNameLikeAndIdIn("%\\\\%", List.of(3L, 4L));
        Assertions.assertThat(ids).hasSize(1);

        ids = departmentDao.findIdByNameLikeAndIdIn("%\\\\%", List.of(3L, 4L, 5L));
        Assertions.assertThat(ids).hasSize(2);
    }

    @Test
    void findIdByNameLikeAndIdNotIn() {
        List<Long> ids = departmentDao.findIdByNameLikeAndIdNotIn("%\\\\%", List.of(3L, 4L));
        Assertions.assertThat(ids).hasSize(1);

        ids = departmentDao.findIdByNameLikeAndIdNotIn("%\\\\%", List.of(3L, 4L, 5L));
        Assertions.assertThat(ids).hasSize(0);
    }
}
