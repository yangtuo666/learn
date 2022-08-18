package com.zhang.jiu.learn.Enum;



import lombok.Getter;

/**
 * es 类型参看
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html
 */
@Getter
public enum FieldTypeEnum {
    /**
     * text
     */
    TEXT("text"),

    KEYWORD("keyword"),

    INTEGER("integer"),

    DOUBLE("double"),

    DATE("date"),

    /**
     * 单条数据
     */
    OBJECT("object"),

    /**
     * 嵌套数组
     */
    NESTED("nested"),


    ;


    FieldTypeEnum(String type){
        this.type = type;
    }

    private final String type;


}

