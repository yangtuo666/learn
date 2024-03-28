package com.zhang.jiu.learn.po;

import com.zhang.jiu.learn.Enum.FieldTypeEnum;
import com.zhang.jiu.learn.annotation.EsField;
import com.zhang.jiu.learn.annotation.EsId;
import com.zhang.jiu.learn.annotation.IndexName;
import lombok.Data;

@Data
@IndexName(index = "user_index")
public class User {
    @EsId
    private String id;
    @EsField(type = FieldTypeEnum.TEXT,keywordAttach = true)
    private String name;
    @EsField(type = FieldTypeEnum.INTEGER)
    private Integer age;
    @EsField(type = FieldTypeEnum.OBJECT)
    private Teacher teacher;

    public static void main(String[] args) {
        String inte = "null";
        Integer integer = Integer.valueOf(inte);
        System.out.println(integer);

    }
}
