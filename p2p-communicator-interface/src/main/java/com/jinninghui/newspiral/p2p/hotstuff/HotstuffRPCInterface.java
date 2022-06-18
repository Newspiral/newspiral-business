package com.jinninghui.newspiral.p2p.hotstuff;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;


import java.util.Set;

/**
 * @author lida
 * @date 2019/7/23 19:48
 * HotStuff所需要使用和定义的RPC接口
 */
public interface HotstuffRPCInterface {

    public enum BroadcastScope {
        Channel, TwentyFivePercentage
    }


    /**
     * 将某个GenericMSg消息广播至channelId指定的通道的所有节点
     * 尽力而为策略，并不需要保证全部成功
     * 所以可以采用异步方式，也即允许返回的时候，其实一个都没有发送出去，但是会在未来的一小段时间内持续尝试广播
     * @param genericMsg
     * @param channelId
     */
    void broadcastGenericMsg(GenericMsg genericMsg, String channelId, Set<Peer> excludePeers);


/*    *//**
     * 将某个Transcation消息广播至channelId指定的通道的所有节点
     * 尽力而为策略，并不需要保证全部成功
     * 所以可以采用异步方式，也即允许返回的时候，其实一个都没有发送出去，但是会在未来的一小段时间内持续尝试广播
     * @param sdkTransaction
     * @param channelId
     */
    void broadcastTranscation(SDKTransaction sdkTransaction, String channelId, Set<Peer> excludePeers, BroadcastScope scope);


    /**
     * 给targetPeer发送一条消息
     * @param newViewMsg
     * @param targetPeer
     */
    void sendNewView(NewViewMsg newViewMsg, Peer targetPeer, String channelId);

    /**
     * 给targetPeer发送一条BlockVoteMsg
     * @param voteMsg
     */
    void sendBlockVoteMsg(BlockVoteMsg voteMsg, Peer targetPeer, String channelId);

    /**todo: remove this method to other place
     *
     * @param newchannel
     */

    void updateChannelPeerClient(Channel newchannel,Long increase);
}
