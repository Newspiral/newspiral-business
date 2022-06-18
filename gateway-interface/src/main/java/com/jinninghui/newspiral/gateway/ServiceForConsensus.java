package com.jinninghui.newspiral.gateway;


import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.gateway.entity.QueryViewNoReq;

public interface ServiceForConsensus {

    /************* HotSutff共识相关接口开始***********************************************/
    /**
     * 注意，此方法需要判断当前系统是否处于同步状态，是则将genericMsg消息缓存起来（只缓存当前有效且最新的）
     * @param genericMsg
     */
    void addGenericMsg(GenericMsg genericMsg);

    /**
     * 处理某个通道的NewView消息
     * @param newViewMsg
     */
    void addNewViewMsg(NewViewMsg newViewMsg);
    /**
     * 处理某个通道的BlockVote消息
     * @param voteMsg
     */
    void addBlockVoteMsg(BlockVoteMsg voteMsg);

    /**
     * get current view number of peer.
     * @param queryViewNoReq
     * @return
     */
    QueryViewNoResp queryViewNo(QueryViewNoReq queryViewNoReq);



    /************* HotSutff共识相关接口结束***********************************************/
}
