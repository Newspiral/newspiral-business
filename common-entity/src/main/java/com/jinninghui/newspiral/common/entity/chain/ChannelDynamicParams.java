package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: ChannelDynamicParams
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/8/6 11:26
 */
@Data
public class ChannelDynamicParams {

    //通道当前区块高度
    @ApiModelProperty(value = "通道当前区块高度")
    private Long height;

    //通道的已达成共识的交易总数
    @ApiModelProperty(value = "通道的已达成共识的交易总数")
    private Integer transanctionCount;

    //最新共识区块的Hash值
    @ApiModelProperty(value = "最新共识区块的Hash值")
    private String blockHash;

    //通道最新区块的共识时间（北京时间）
    @ApiModelProperty(value = "通道最新区块的共识时间（北京时间）")
    private Long latestTime;

    //通道中正常在线的节点数（取被调用节点的本地数据）
    @ApiModelProperty(value = "通道中正常在线的节点数")
    private Integer normalPeerCount;

    //通道共识状态：如果当前节点的最新共识区块的创建时间与当前时间的间距大于3倍出块时间，返回共识异常，否则返回共识正常
    @ApiModelProperty(value = "通道共识状态")
    private  Boolean consensusFlag=true;

    //视图号
    @ApiModelProperty(value = "视图号")
    private Long viewNum;

    //待打包交易数
    @ApiModelProperty(value = "待打包交易数")
    private Long toPackageTransNum;

    //交易池待打包交易字节数
    @ApiModelProperty(value = "交易池待打包交易字节数")
    private Long toPackageTransSize;

    //交易池待共识交易数
    @ApiModelProperty(value = "交易池待共识交易数")
    private Long toConsensusTransNum;

    //交易池待共识交易字节数
    @ApiModelProperty(value = "交易池待共识交易字节数")
    private Long toConsensusTransSize;

}
