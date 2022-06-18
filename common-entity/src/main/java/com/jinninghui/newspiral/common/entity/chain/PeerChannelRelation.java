package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "节点在特定通道中的信息")
@Data
public class PeerChannelRelation implements Comparable<PeerChannelRelation> {

    @ApiModelProperty(value = "通道ID")
    private String channelId;
    @ApiModelProperty(value = "加入通道时间戳")
    private Date joinTimeStamp;
    @ApiModelProperty(value = "扩展数据")
    private String extendedData;
    @ApiModelProperty(value = "用户私钥")
    private String userPrivateKey;
    @ApiModelProperty(value = "节点加入通道时区块高度")
    private Long inBlockHeight;
    @ApiModelProperty(value = "节点离开通道时区块高度")
    private Long outBlockHeight;
    @ApiModelProperty(value = "操作类型")
    private String actionType;
    @ApiModelProperty(value = "节点更新操作的时间戳")
    private Date updateTimeStamp;

    public PeerChannelRelation() {
        this.channelId = "";
        this.joinTimeStamp = new Date(System.currentTimeMillis());
        this.extendedData = "0";
        this.userPrivateKey = "";
        this.inBlockHeight = 0L;
        this.outBlockHeight = 0L;
    }

    public PeerChannelRelation clone() {
        PeerChannelRelation peerChannelRelation = new PeerChannelRelation();
        peerChannelRelation.setChannelId(channelId);
        peerChannelRelation.setExtendedData(extendedData);
        peerChannelRelation.setUserPrivateKey(userPrivateKey);
        peerChannelRelation.setOutBlockHeight(outBlockHeight);
        peerChannelRelation.setInBlockHeight(inBlockHeight);
        peerChannelRelation.setJoinTimeStamp(joinTimeStamp);
        peerChannelRelation.setUpdateTimeStamp(updateTimeStamp);
        peerChannelRelation.setActionType(actionType);
        return peerChannelRelation;
    }

    //从大到小排序
    @Override
    public int compareTo(PeerChannelRelation o) {
        return Long.compare(o.getInBlockHeight(),this.getInBlockHeight());
    }
}
