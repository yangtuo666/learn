package com.zhang.jiu.learn.copy;

import org.junit.jupiter.api.Test;
import com.zhang.jiu.learn.po.Teacher;
import com.zhang.jiu.learn.po.User;
import com.zhang.jiu.learn.po.UserDto;
import com.zhang.jiu.learn.utils.BeanCopyUtil;

public class CopyUtilsTest {
    @Test
    public void copyTest(){
        User user = new User();
        user.setAge(10);
        user.setName("小明");
        Teacher teacher = new Teacher();
        teacher.setTeacherAge(40);
        teacher.setTeacherName("老王");
        user.setTeacher(teacher);

        /*UserDto userDto = new UserDto();
        BeanCopyUtil.map(user,userDto);*/
        UserDto userDto = BeanCopyUtil.map(user, UserDto.class);
        System.out.println(user);
        userDto.getTeacher().setTeacherName("隔壁老李");
        userDto.getTeacher().setTeacherAge(10);
        System.out.println(userDto);
        System.out.println(user);
        System.out.println(teacher);

  /*      teacher.setTeacherAge(19);
        teacher.setTeacherName("hhhh");
        System.out.println(teacher);
        System.out.println(userDto);*/

    }
}
