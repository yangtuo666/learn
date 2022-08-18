package com.zhang.jiu.learn.annotation;


import java.lang.annotation.*;

/**
 * 用于标识使用 该字段作为ES数据中的id
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface EsId {
}

