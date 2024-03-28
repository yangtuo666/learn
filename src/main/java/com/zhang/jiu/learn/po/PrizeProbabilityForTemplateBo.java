package com.zhang.jiu.learn.po;

import lombok.Data;

/**
 * app-api
 *
 * @author 松溪
 * @date 2020/5/13
 */
@Data
public class PrizeProbabilityForTemplateBo {

    /**
     * 类型 模板中奖品的number
     */
    private Integer memberLotteryPrize;

    /**
     * 中奖概率
     */
    private Integer probabilityPercentage;

}
