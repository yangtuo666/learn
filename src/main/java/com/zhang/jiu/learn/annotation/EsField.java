package com.zhang.jiu.learn.annotation;





import com.zhang.jiu.learn.Enum.AnalyzerTypeEnum;
import com.zhang.jiu.learn.Enum.FieldTypeEnum;

import java.lang.annotation.*;

/**
 * 作用在字段上，用于定义类型，映射关系
 * @author ls
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EsField  {

    FieldTypeEnum type() default FieldTypeEnum.TEXT;

    /**
     * 指定分词器
     * @return
     */
    AnalyzerTypeEnum analyzer() default AnalyzerTypeEnum.STANDARD;

    boolean keywordAttach() default false;
}

