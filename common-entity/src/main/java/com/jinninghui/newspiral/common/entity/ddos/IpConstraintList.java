package com.jinninghui.newspiral.common.entity.ddos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ToString
public class IpConstraintList implements Serializable {

    /**
     * ip地址
     */
    @ApiModelProperty("ip地址")
    @NotBlank
    private String ipAddr;

    /**
     * 禁止该ip访问的类名，该类名下的所有接口禁止访问
     */
    @ApiModelProperty("限制操作类型，比如禁止访问，允许访问，限流等")
    @NotBlank
    private String constraintType;

    /**
     * 黑名单是否生效，1为生效，0为不生效，默认为1
     */
    @ApiModelProperty("黑名单是否生效，1为生效，0为不生效，默认为1")
    private String active = "1";

    /**
     * 补充信息，用于说明该ip地址的额外信息
     */
    @ApiModelProperty("补充信息，用于说明该ip地址的额外信息")
    private String Remark;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Long createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Long updateTime;
}
