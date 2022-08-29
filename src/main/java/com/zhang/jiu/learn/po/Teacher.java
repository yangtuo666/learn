package com.zhang.jiu.learn.po;

import com.zhang.jiu.learn.Enum.FieldTypeEnum;
import com.zhang.jiu.learn.annotation.EsField;
import lombok.Data;

@Data
public class Teacher {
    @EsField(keywordAttach = true)
    private String teacherName;
    @EsField(type = FieldTypeEnum.INTEGER)
    private Integer teacherAge;
}
