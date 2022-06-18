package com.jinninghui.newspiral.p2p.impl.hotstuff;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface;
import com.jinninghui.newspiral.p2p.impl.base.ServiceForPeerClientMgr;
import com.jinninghui.newspiral.p2p.impl.base.ChannelClientMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @author lida
 * @date 2019/7/29 15:39
 */
@Slf4j
public class HotStuffRPC implements HotstuffRPCInterface {

    @Autowired
    private ServiceForPeerClientMgr serviceForPeerClientMgr;


    @Override
    public void broadcastGenericMsg(GenericMsg genericMsg, String channelId, Set<Peer> excludePeers) {
        ChannelClientMgr mgr = serviceForPeerClientMgr.getChannelClientMgr(channelId);
        mgr.broadcastGenericMsg(genericMsg,excludePeers);
    }
    @Override
    public void broadcastTranscation(SDKTransaction sdkTransaction, String channelId, Set<Peer> excludePeers, BroadcastScope scope) {
        ChannelClientMgr mgr = serviceForPeerClientMgr.getChannelClientMgr(channelId);
        mgr.broadcastTransaction(sdkTransaction, excludePeers, scope);
    }

    @Override
    public void sendNewView(NewViewMsg newViewMsg, Peer targetPeer, String channelId) {
        ChannelClientMgr mgr = serviceForPeerClientMgr.getChannelClientMgr(channelId);
        mgr.sendNewView(newViewMsg,targetPeer);
    }

    @Override
    public void sendBlockVoteMsg(BlockVoteMsg voteMsg, Peer targetPeer, String channelId) {
        ChannelClientMgr mgr = serviceForPeerClientMgr.getChannelClientMgr(channelId);
        mgr.sendBlockVoteMsg(voteMsg,targetPeer);
    }
    @Override
    public void updateChannelPeerClient(Channel newchannel,Long increase) {
        ChannelClientMgr mgr = serviceForPeerClientMgr.getChannelClientMgr(newchannel.getChannelId());
        mgr.updateChannelPeer(newchannel,increase);
    }
}
