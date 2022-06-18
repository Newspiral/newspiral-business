package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.chain.PeerBasicParams;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @date 2020/10/26
 * @author whj
 */

@Data
public class PeerConsensusStateResp implements Serializable {
    /**
     * 节点Id
     */
    private String peerId;
    /**
     * 通道Id
     */
    private String channelId;
    /**
     * 通道区块高度
     */
    private Long blockHeight;
    /**
     * 节点在通道中的视图号
     */
    private Long viewNo;
    /**
     * 共识状态
     */
    private ConsensusStageEnum consensusStage;
    /**
     * HotStuff的prePrepare Node
     */
    private HotStuffDataNodeResp prePrepareNode;
    /**
     * HotStuff的prePare Node
     */
    private HotStuffDataNodeResp prePareNode;
    /**
     * HotStuff的preCommint Node
     */
    private HotStuffDataNodeResp preCommitNode;
    /**
     * 通道中的节点信息列表
     */
    private List<PeerBasicParams> peerBasicParams;
}
