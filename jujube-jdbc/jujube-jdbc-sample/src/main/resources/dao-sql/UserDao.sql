<@pageForUserList>
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
<#if notBlank(name)>
  and u.`name` like '%${name}%'
</#if>
<#if age gt 0>
  and u.age > ${age}
</#if>
<#if notNull(ids)>
  and u.id in (${join(ids,',')})
</#if>
<#if notBlank(nameDesc)>
  order by u.id asc
</#if>
</@pageForUserList>


<@pageForUserListOfOrder>
select u.* from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc;
</@pageForUserListOfOrder>


<@queryAgeCount>
select COUNT(*) from `user` u where u.age = ${age} and department_id = ${departmentId}
</@queryAgeCount>


<@queryAgeCount2>
select COUNT(*) from `user` u where u.age = ${age}
</@queryAgeCount2>


<@queryUserName>
select u.`name` from `user` u where u.id = ${id}
</@queryUserName>


<@queryUserAge>
select id,name,age,department_id,sex from `user` u where u.age = ${age} order by id
</@queryUserAge>


<@queryUserByDepartmentId>
select * from `user` u where department_id = ${departmentId}
</@queryUserByDepartmentId>


<@queryIdByDepartmentId>
select u.id from `user` u where department_id = ${departmentId}
</@queryIdByDepartmentId>


<@queryUserByIds>
select * from `user` u where u.id in (${join(ids,',')})
</@queryUserByIds>


<@pageForUserUnionQuery2>
select * from `user` u where u.age > ${age}
#jujube-union
select * from `user` u where u.department_id = ${departmentId}
</@pageForUserUnionQuery2>


<@pageForUserUnionQuery3>
select * from `user` u where u.age > 20
#jujube-union
select * from `user` u where u.department_id = 1
#jujube-union
select * from `user` u where u.age = 10
</@pageForUserUnionQuery3>


<@getUserDepartById>
select u.id user_id,d.id depart_id,u.name user_name,d.name depart_name from `department` d
left join `user` u on u.department_id=d.id
where u.id = ${uid}
</@getUserDepartById>


<@getUsersByDepartId>
select u.id user_id,d.id depart_id,u.name user_name,d.name depart_name from  `user` u
left join`department` d on u.department_id=d.id
where d.id = ${departId}
</@getUsersByDepartId>


