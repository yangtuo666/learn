package com.zhang.jiu.learn.Enum;



import lombok.Getter;

/**
 * @className: AnalyzerType
 * @description:
 * @author: sh.Liu
 * @create: 2020-05-27 11:37
 */
@Getter
public enum AnalyzerTypeEnum {

    NO("不使用分词"),
    /**
     * 标准分词，默认分词器
     */
    STANDARD("standard"),

    /**
     * ik_smart：会做最粗粒度的拆分；已被分出的词语将不会再次被其它词语占有
     */
    IK_SMART("ik_smart"),

    /**
     * ik_max_word ：会将文本做最细粒度的拆分；尽可能多的拆分出词语
     */
    IK_MAX_WORD("ik_max_word")

    ;

    private String type;

    AnalyzerTypeEnum(String type){
        this.type = type;
    }
}

