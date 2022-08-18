package com.zhang.jiu.learn.annotation;

import java.lang.annotation.*;

/**
 * Es 文档注解，用于做索引实体映射
 * 作用在类上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface IndexName {

    /**
     * index : 索引名称
     * @return
     */
    String index();

}

