package com.jinninghui.newspiral.transaction.mgr.impl;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface;
import org.springframework.stereotype.Component;

/**
 * @version V1.0
 * @Title: AsyncTransactionTask
 * @Package com.jinninghui.newspiral.transaction.mgr.impl
 * @Description:
 * @author: xuxm
 * @date: 2020/6/19 17:17
 */
@Component
public class AsyncTransactionTask {
    @SofaReference
    private HotstuffRPCInterface rpcInterface;

    /*@Async
    public void  broadcastTranscation(SDKTransaction sdkTransaction,Peer peer, boolean flag)
    {
        //不转发第二次
        if(!flag)return;
        HashSet<Peer> peers = new HashSet<>();
        //xxm 先注释，发给自己
        peers.add(peer);
        HotstuffRPCInterface.BroadcastScope broadcastScope;
        if (flag) {
            broadcastScope= HotstuffRPCInterface.BroadcastScope.Channel;
        } else {
            broadcastScope= HotstuffRPCInterface.BroadcastScope.TwentyFivePercentage;
        }
        rpcInterface.broadcastTranscation(
                sdkTransaction, sdkTransaction.getChannelId(), peers, broadcastScope);
    }*/


    private HotstuffRPCInterface.BroadcastScope judgeBroadcasetScopeByFromSDK(boolean fromSDK) {
        if (fromSDK) {
            return HotstuffRPCInterface.BroadcastScope.Channel;
        } else {
            return HotstuffRPCInterface.BroadcastScope.TwentyFivePercentage;
        }
    }
}
