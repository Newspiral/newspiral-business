package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description= "共识状态信息")
@Data
public class CurrentConsensusState {
    /**
     * 通道ID
     */
    @ApiModelProperty(value = "通道ID")
    String channelId;

    /**
     * block的高度
     */
    @ApiModelProperty(value = "block的高度")
    Long height;

    /**
     * 当前view
     */
    @ApiModelProperty(value = "当前的viewNo")
    Long currentView;

    @ApiModelProperty(value = "prePrepare",hidden = true)
    HotStuffDataNode prePrepare;

    @ApiModelProperty(value = "prepare",hidden = true)
    HotStuffDataNode prepare;

    @ApiModelProperty(value = "preCommit",hidden = true)
    HotStuffDataNode preCommit;

    @ApiModelProperty(value = "highestQC", hidden = true)
    GenericQC highestQC;

    @ApiModelProperty(value = "经过排序的节点列表")
    List<Peer> orderedPeerList;

    /**
     * 本节点所处的共识阶段
     */
    @ApiModelProperty(value = "本节点所处的共识阶段")
    String consensusStageEnum;
}
