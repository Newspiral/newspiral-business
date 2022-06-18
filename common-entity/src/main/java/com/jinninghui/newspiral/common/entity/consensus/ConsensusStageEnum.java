package com.jinninghui.newspiral.common.entity.consensus;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 节点共识阶段
 */
public enum ConsensusStageEnum {

    WAIT_SYNC_WITH_CHANNEL("WaitSyncWithChannel","等待与网络同步数据，不能确定本地状态，不参与共识逻辑，一个典型的例子是本节点宕机了一段时间"),
    LEADER_WAIT_NEWVIEW("LeaderWaitNewView", "本节点在本轮是主节点，但是还没有拿到足够多的NewView消息，因此还不能开始构造Block"),
    LEADER_WAIT_TRANS("LeaderWaitTrans", "本节点在本轮是主节点，已经拿到足够多的NewView消息，但尚未有足够多的交易，因此还没有开始构造Block"),
    LEADER_WAIT_BLOCK_VOTE("LeaderWaitBlockVote", "本节点在本轮是主节点，已经构造并发送完毕Block，正在等待BlockVote"),
    REPLICA_WAIT_BLOCK("ReplicaWaitBlock", "本节点在本轮不是主节点，在等待主节点的Block"),
    ONE_PEER("OnePeer","通道中只有一个节点"),
    LEAVE_CHANNEL("LeaveChannel", "离开通道，停止相关线程"),
    NO_AVALIABLE_DB_ROUTE("NoAvaliableDBRoute", "没有可用的数据库路由，在等待刷新数据库配置");
    public final String code;
    public final String message;
    ConsensusStageEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
