package cn.xuanyuanli.jdbc.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.entity.User;
import cn.xuanyuanli.jdbc.base.annotation.SelectField;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.jdbc.pagination.Pageable;
import cn.xuanyuanli.jdbc.pagination.PageableRequest;

/**
 * @author xuanyuanli
 */
public interface UserDao extends BaseDao<User, Long> {

    /**
     * 获得表名字
     *
     * @return {@link String}
     */
    @Override
    default String getTableName() {
        return "user";
    }

    /**
     * 获得名字
     *
     * @param id id
     * @return {@link String}
     */
    default String getName(long id) {
        return findNameById(id);
    }

    /**
     * 获得名字通过id
     *
     * @param id id
     * @return {@link String}
     */
    default String getNameById(long id) {
        return findNameById(id);
    }

    /**
     * 根据查询条件获得 {@link User#getName name}
     *
     * @param id    {@link User#getId() id}
     * @return {@link User#getName name}
     */
    String findNameById(long id);

    /**
     * 根据查询条件获得 {@link User}
     *
     * @param id    {@link User#getId() id}
     * @return {@link User}
     */
    User findNameAndAgeById(long id);

    /**
     * 根据查询条件获得 {@link BigDecimal}
     *
     * @param id    {@link User#getId() id}
     * @return {@link BigDecimal}
     */
    BigDecimal findAgeById(long id);

    /**
     * 根据查询条件获得 {@link User#getDepartmentId departmentId}
     *
     * @param id    {@link User#getId() id}
     * @return {@link User#getDepartmentId departmentId}
     */
    Long findDepartmentIdById(long id);

    /**
     * 根据查询条件获得对象集合
     *
     * @param age    {@link User#getAge() age}
     * @return {@link List}<{@link String}>
     */
    List<String> findNameByAge(int age);

    /**
     * 根据查询条件获得 {@link User#getId id}
     *
     * @param age    {@link User#getAge() age}
     * @return {@link User#getId id}
     */
    long findIdByAge(int age);

    /**
     * 根据查询条件获得 {@link User}
     *
     * @param name    {@link User#getName() name}
     * @return {@link User}
     */
    User findByName(String name);

    /**
     * 根据查询条件获得 {@link User}
     *
     * @param name    {@link User#getName() name}
     * @param age    {@link User#getAge() age}
     * @return {@link User}
     */
    User findByNameAndAge(String name, int age);

    /**
     * 根据查询条件获得对象集合
     *
     * @param name    {@link User#getName() name}
     * @return {@link List}<{@link User}>
     */
    List<User> findByNameLike(String name);

    /**
     * 根据查询条件获得对象集合
     *
     * @param name    {@link User#getName() name}
     * @return {@link List}<{@link User}>
     */
    List<User> findByNameNotLike(String name);

    /**
     * 根据查询条件获得对象集合
     *
     * @param dids    {@link User#getDepartmentId() departmentId}
     * @return {@link List}<{@link User}>
     */
    List<User> findByDepartmentIdIn(List<Long> dids);

    /**
     * 根据查询条件获得对象集合
     *
     * @param i    {@link User#getId() id}
     * @return {@link List}<{@link User}>
     */
    List<User> findByIdGtOrderByAgeDesc(int i);

    /**
     * 根据查询条件获得对象集合
     *
     * @param i    {@link User#getId() id}
     * @return {@link List}<{@link User}>
     */
    List<User> findByIdGtOrderByAgeAsc(int i);

    /**
     * 根据查询条件获得对象集合
     *
     * @param i    {@link User#getId() id}
     * @return {@link List}<{@link User}>
     */
    List<User> findByIdGtOrderById(int i);

    /**
     * 根据查询条件获得对象集合
     *
     * @param i    {@link User#getId() id}
     * @return {@link List}<{@link User}>
     */
    List<User> findByIdGtGroupById(int i);

    /**
     * 根据查询条件获得对象集合
     *
     * @param arr    {@link User#getId() id}
     * @return {@link List}<{@link User}>
     */
    List<User> findByIdIn(long[] arr);

    /**
     * 根据查询条件获得对象集合
     *
     * @param arr    {@link User#getAge() age}
     * @return {@link List}<{@link User}>
     */
    List<User> findByAgeIn(List<Integer> arr);

    /**
     * 根据查询条件获得对象集合
     *
     * @return {@link List}<{@link User}>
     */
    List<User> findAllGroupById();

    /**
     * 根据查询条件获得 {@link User}
     *
     * @return {@link User}
     */
    User findAllGroupByIdLimit1();

    /**
     * 根据查询条件获得对象集合
     *
     * @param limit    条数限制
     * @return {@link List}<{@link User}>
     */
    List<User> findAllGroupByIdLimit(int limit);

    /**
     * 根据查询条件获得 总数
     *
     * @param name    {@link User#getName() name}
     * @return 总数
     */
    int getCountByNameLike(String name);

    /**
     * 根据查询条件获得  {@link User#getAge age} 总和
     *
     * @param name    {@link User#getName() name}
     * @return  {@link User#getAge age} 总和
     */
    double getSumOfAgeByNameLike(String name);

    /**
     * 根据查询条件获得  {@link User#getAge age} 总和
     *
     * @param name    {@link User#getName() name}
     * @return  {@link User#getAge age} 总和
     */
    int getSumOfAgeByName(String name);

    /**
     * 根据查询条件获得 总数
     *
     * @param name    {@link User#getName() name}
     * @return 总数
     */
    long getCountByNameLikeGroupById(String name);

    /**
     * 查询年龄统计
     *
     * @param age          年龄
     * @param departmentId 部门id
     * @return long
     */
    long queryAgeCount(long age, long departmentId);

    /**
     * 查询年龄统计2
     *
     * @param age 年龄
     * @return int
     */
    int queryAgeCount2(long age);

    /**
     * 查询用户名
     *
     * @param id id
     * @return {@link String}
     */
    String queryUserName(long id);

    /**
     * 查询用户年龄
     *
     * @param age 年龄
     * @return {@link QueryUserAgePO}
     */
    QueryUserAgePO queryUserAge(long age);

    /**
     * 查询用户由部门id
     *
     * @param departmentId 部门id
     * @return {@link List}<{@link UserPO}>
     */
    List<UserPO> queryUserByDepartmentId(long departmentId);

    /**
     * 查询id
     *
     * @param departmentId 部门id
     * @return {@link List}<{@link Long}>
     */
    List<Long> queryIdByDepartmentId(long departmentId);

    /**
     * 查询用户id
     *
     * @param ids id
     * @return {@link List}<{@link UserPO}>
     */
    List<UserPO> queryUserByIds(List<Long> ids);

    /**
     * 用户列表
     *
     * @param queryMap 查询地图
     * @param request  请求
     * @return {@link Pageable}<{@link UserPO}>
     */
    Pageable<UserPO> pageForUserList(Map<String, Object> queryMap, PageableRequest request);

    /**
     * 用户列表
     *
     * @param queryMap 查询地图
     * @param request  请求
     * @return {@link Pageable}<{@link UserPO}>
     */
    Pageable<UserPO> pageForUserListOfOrder(Map<String, Object> queryMap, PageableRequest request);

    /**
     * 用户列表
     *
     * @param queryMap 查询地图
     * @param request  请求
     * @return {@link Pageable}<{@link UserPO}>
     */
    Pageable<UserPO> pageForUserUnionQuery2(Map<String, Object> queryMap, PageableRequest request);

    /**
     * 用户列表
     *
     * @param queryMap 查询地图
     * @param request  请求
     * @return {@link Pageable}<{@link UserPO}>
     */
    Pageable<UserPO> pageForUserUnionQuery3(Map<String, Object> queryMap, PageableRequest request);

    /**
     * 获得用户部门通过id
     *
     * @param uid uid
     * @return {@link UserDepartmentPO}
     */
    UserDepartmentPO getUserDepartById(long uid);

    /**
     * 获得用户通过部门id
     *
     * @param departId 离开id
     * @return {@link List}<{@link GetUsersByDepartIdPO}>
     */
    List<GetUsersByDepartIdPO> getUsersByDepartId(long departId);

    /**
     * 根据查询条件获得对象集合
     *
     * @param departId    {@link User#getDepartmentId() departmentId}
     * @return {@link List}<{@link User}>
     */
    @SelectField({"id user_id", "name user_iame", "(select d.`name` from `department` d where ${department_id}=d.id) depart_name"})
    List<UserDepartmentPO> findAny100ByDepartmentId(long departId);

    /**
     * 根据查询条件获得对象集合
     *
     * @return {@link List}<{@link Long}>
     */
    @SelectField("department_id")
    List<Long> findAllGroupByDepartmentId();

    /**
     * 根据查询条件获得对象集合
     *
     * @param age    {@link User#getAge() age}
     * @return {@link List}<{@link Long}>
     */
    @SelectField("id")
    List<Long> findAny21ByAge(int age);

    /**
     * 根据查询条件获得字段
     *
     * @param sex    {@link User#getSex() sex}
     * @return java.lang.Integer
     */
    @SelectField("sex")
    Integer findAny1BySex(int sex);

    /**
     * 根据查询条件获得字段
     *
     * @param sex    {@link User#getSex() sex}
     * @param age    {@link User#getAge() age}
     * @return int
     */
    @SelectField("sex")
    int findAny1BySexAndAge(int sex, int age);

    /**
     * 根据查询条件获得 {@link SexAndAgePO}
     *
     * @param id    {@link User#getId() id}
     * @return {@link SexAndAgePO}
     */
    SexAndAgePO findSexAndAgeById(long id);

    /**
     * 根据查询条件获得对象集合
     *
     * @param departId    {@link User#getDepartmentId() departmentId}
     * @return {@link List}<{@link User}>
     */
    List<SexAndAgePO> findSexAndAgeByDepartmentId(long departId);

    /**
     * @author generator
     */
    @Data
    class SexAndAgePO implements BaseEntity {

        private Integer age;
        private Integer sex;
    }

    /**
     * @author generator
     */
    @Data
    class UserDepartmentPO implements BaseEntity {

        private Long userId;
        private Integer departId;
        private String userName;
        private String departName;
    }

    /**
     * @author xuanyuanli
     */
    @Data
    @Accessors(chain = true)
    class UserPO implements BaseEntity {
        private Long id;
        private String name;
        private Integer age;
        private Long departmentId;
        private Integer sex;
    }

    @Data
    class QueryUserAgePO {
        private Long id;
        private String name;
        private String age;
        private Long departmentId;
        private String sex;
    }

    @Data
    class GetUsersByDepartIdPO {
        private Long userId;
        private Long departId;
        private String userName;
        private String departName;
    }
}
