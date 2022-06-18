package com.jinninghui.newspiral.common.entity.ddos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class IpConstraintListParam {

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
    @NotNull(message = "传入的constraintType为null，请传值")
    private ConstraintTypeEnum constraintType;

    /**
     * 限制操作是否生效，1为生效，0为不生效
     */
    @ApiModelProperty("限制操作是否生效，1为生效，0为不生效")
    private String active;

    /**
     * 补充信息，用于说明该ip地址的额外信息
     */
    @ApiModelProperty("补充信息，用于说明该ip地址的额外信息")
    private String Remark;

    public IpConstraintList toIpConstraintList(){
        IpConstraintList ipConstraintList = new IpConstraintList();
        ipConstraintList.setIpAddr(this.getIpAddr());
        ipConstraintList.setConstraintType(this.getConstraintType().getConstraintType());
        ipConstraintList.setActive(this.getActive());
        ipConstraintList.setRemark(this.getRemark());
        return ipConstraintList;
    }
}
