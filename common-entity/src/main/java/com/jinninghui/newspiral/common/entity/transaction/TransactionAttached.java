package com.jinninghui.newspiral.common.entity.transaction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: TransactionAttached
 * @Package com.jinninghui.newspiral.common.entity.transaction
 * @Description:
 * @author: xuxm
 * @date: 2020/9/28 17:38
 */
@ApiModel(description = "交易附加信息")
@Data
public class TransactionAttached {

    /**
     * 区块高度
     */
    @ApiModelProperty(value = "区块高度")
    private Long blockHeight;


    //暂时只用这一个属性
   /* *//**
     * 区块大小
     *//*
    @ApiModelProperty(value = "区块大小")
    private Long blockSize;

    *//**
     * 出块节点
     *//*
    @ApiModelProperty(value = "出块节点")
    private String builderPeerId;

    *//**
     * 通道Name
     *//*
    @ApiModelProperty(value = "通道Name")
    private String channleName;*/
}
