package com.campus.mapper;

import com.github.pagehelper.Page;
import com.campus.annotation.AutoFill;
import com.campus.dto.EmployeePageQueryDTO;
import com.campus.entity.Employee;
import com.campus.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status,create_time, update_time, create_user, update_user) " +
            "values(#{name}, #{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);

    /*
    * 员工分页查询: 获取总记录数（员工数）
    * */
   /* @Select("select COUNT(*) from employee")
    Integer selectCount();*/

    /*
     * 员工分页查询 : 获取员工数据集
     * */
    //@Select("select * from employee group by create_time desc limit #{beginIndex},#{pageSize}")
    /*List<Employee> selectPage(Integer beginIndex, Integer pageSize);*/
    Page<Employee> selectPage(EmployeePageQueryDTO employeePageQueryDTO);

    /*
    * 根据主键动态修改属性
    * */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /*
     * 根据id查询员工
     * */
    @Select("select * from employee where id = #{id}")
    Employee selectById(Long id);

}
