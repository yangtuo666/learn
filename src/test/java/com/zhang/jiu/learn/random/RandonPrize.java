package com.zhang.jiu.learn.random;

import com.zhang.jiu.learn.po.PrizeProbabilityForTemplateBo;
import com.zhang.jiu.learn.po.Teacher;
import com.zhang.jiu.learn.po.User;
import com.zhang.jiu.learn.po.UserDto;
import com.zhang.jiu.learn.utils.BeanCopyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


public class RandonPrize {

    private final static Integer HUNDRED_THOUSAND = 100000;
    @Test
    public void random(){
        List<PrizeProbabilityForTemplateBo> prizeProbabilityBos = new ArrayList<>();
        PrizeProbabilityForTemplateBo prize1 = new PrizeProbabilityForTemplateBo();
        prize1.setMemberLotteryPrize(1);
        prize1.setProbabilityPercentage(10000);
        prizeProbabilityBos.add(prize1);

        PrizeProbabilityForTemplateBo prize2 = new PrizeProbabilityForTemplateBo();
        prize2.setMemberLotteryPrize(2);
        prize2.setProbabilityPercentage(20000);
        prizeProbabilityBos.add(prize2);

        PrizeProbabilityForTemplateBo prize3 = new PrizeProbabilityForTemplateBo();
        prize3.setMemberLotteryPrize(3);
        prize3.setProbabilityPercentage(30000);
        prizeProbabilityBos.add(prize3);

        PrizeProbabilityForTemplateBo prize4 = new PrizeProbabilityForTemplateBo();
        prize4.setMemberLotteryPrize(4);
        prize4.setProbabilityPercentage(40000);
        prizeProbabilityBos.add(prize4);

        for (int i=0;i<5000;i++){
            Integer prizeByProbability = this.getPrizeByProbability(prizeProbabilityBos);
            System.out.println(prizeByProbability);
        }


    }


    public Integer getPrizeByProbability(List<PrizeProbabilityForTemplateBo> prizeProbabilityBos) {

        if (CollectionUtils.isEmpty(prizeProbabilityBos)){
            throw new RuntimeException("抽奖失败，请稍后重试！");
        }
        Integer allProbability = prizeProbabilityBos.stream().mapToInt(PrizeProbabilityForTemplateBo::getProbabilityPercentage).sum();
        //加起来肯定是100000，不是100000直接抛异常
        if (!allProbability.equals(HUNDRED_THOUSAND)) {
            throw new RuntimeException("抽奖失败，请稍后重试！");
        }
        //中奖概率从小到大排个序
        List<PrizeProbabilityForTemplateBo> afterSortResult = prizeProbabilityBos.stream().sorted(Comparator.comparing(PrizeProbabilityForTemplateBo::getProbabilityPercentage)).collect(Collectors.toList());
        //左边区间
        int left = 0;
        //右边区间
        int right = 0;
        Random random = new Random();
        //产生100000以内的随机数
        int randomNum = random.nextInt(HUNDRED_THOUSAND);
        //判断落在哪个概率区间
        for (PrizeProbabilityForTemplateBo bo : afterSortResult) {
            right += bo.getProbabilityPercentage();
            if (left < randomNum && randomNum <= right) {
                //返回中奖的序列号
                return bo.getMemberLotteryPrize();
            }else {
                //没命中，看是否命中下一个奖品的数字区间内
                left = right;
            }
        }
        //都没命中，说明有问题
        throw new RuntimeException("抽奖失败，请稍后重试！");
    }

}
