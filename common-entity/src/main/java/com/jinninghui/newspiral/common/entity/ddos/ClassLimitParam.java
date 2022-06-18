package com.jinninghui.newspiral.common.entity.ddos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ClassLimitParam {
    /**
     *  接口所在类的类名
     */
    @ApiModelProperty("接口所在类的类名")
    private ClassTypeEnum classTypeEnum;

    /**
     * 接口是否进行限流，（限流1，不限流0）默认为限流
     */
    @ApiModelProperty("接口是否进行限流，（限流1，不限流0）默认为限流")
    private String limitAccess;

    /**
     * 令牌创建速率,每秒钟允许的最大请求数
     */
    @ApiModelProperty("令牌创建速率,每秒钟允许的最大请求数")
    private Integer createTokenRate;

    /**
     * 每个请求最大等待时间，如超过这个时间则请求被拒绝
     */
    @ApiModelProperty("每个请求最大等待时间，如超过这个时间则请求被拒绝")
    private Integer waitTokenTime;

    public Interface toInterface(){
        Interface anInterface = new Interface();
        anInterface.setClassName(this.getClassTypeEnum().getClassName());
        anInterface.setLimitAccess(this.getLimitAccess());
        anInterface.setCreateTokenRate(this.getCreateTokenRate());
        anInterface.setWaitTokenTime(this.getWaitTokenTime());
        return anInterface;
    }
}
