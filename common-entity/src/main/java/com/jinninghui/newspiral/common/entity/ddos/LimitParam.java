package com.jinninghui.newspiral.common.entity.ddos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 限流请求实体
 */
@Data
public class LimitParam {

    /**
     * 方法名
     */
    @ApiModelProperty("方法名")
    @NotBlank
    private String methodName;

    /**
     * 方法所在类名
     */
    @ApiModelProperty("方法所在类名")
    @NotBlank
    private String className;

    /**
     *  是否进行限流，（限流1，不限流0），如不传则默认为1
     */
    @ApiModelProperty("是否进行限流，（限流1，不限流0），如不传则默认限流")
    private String limitAccess = "1";

    /**
     * 令牌创建速率,每秒钟允许的最大请求数
     */
    @ApiModelProperty("令牌创建速率,每秒钟允许的最大请求数，不为null，大于1")
    @Min(1)
    @NotNull(message = "传入的createTokenRate为null，请传值")
    private Integer createTokenRate;

    /**
     *
     */
    @ApiModelProperty("每个请求最大等待时间，如超过这个时间则请求被拒绝，不为null，[0,3]")
    @Min(0)
    @Max(3)
    @NotNull(message = "传入的waitTokenTime为null，请传值")
    private Integer waitTokenTime;

    public Interface toInterface(){
        Interface anInterface = new Interface();
        anInterface.setClassName(this.getClassName());
        anInterface.setMethodName(this.getMethodName());
        anInterface.setLimitAccess(this.getLimitAccess());
        anInterface.setCreateTokenRate(this.getCreateTokenRate());
        anInterface.setWaitTokenTime(this.getWaitTokenTime());
        return anInterface;
    }

}
