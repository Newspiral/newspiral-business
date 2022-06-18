package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelRelation;

public class PeerChannelModelWithBLOBs extends PeerChannelModel {

    public static PeerChannelModelWithBLOBs createInstance(Channel channel, Peer peer) {
        PeerChannelModelWithBLOBs bean =new PeerChannelModelWithBLOBs();
        bean.setChannelId(channel.getChannelId());
        bean.setInBlockHeight(peer.getPeerChannelRelation().getInBlockHeight());
        bean.setOutBlockHeight(peer.getPeerChannelRelation().getOutBlockHeight());
        bean.setJoinTimestamp(peer.getPeerChannelRelation().getJoinTimeStamp());
        bean.setPeerId(peer.getPeerId().getValue());
        bean.setExtendedData(peer.getPeerChannelRelation().getExtendedData());
        bean.setUserPrivateKey(peer.getPeerChannelRelation().getUserPrivateKey());
        bean.setActionType(peer.getPeerChannelRelation().getActionType());
        return bean;
    }

    public PeerChannelRelation toPeerChannelRelation() {
        PeerChannelRelation peerChannelRelation = new PeerChannelRelation();
        peerChannelRelation.setChannelId(this.getChannelId());
        peerChannelRelation.setExtendedData(this.getExtendedData());
        peerChannelRelation.setInBlockHeight(this.getInBlockHeight());
        peerChannelRelation.setOutBlockHeight(this.getOutBlockHeight());
        peerChannelRelation.setJoinTimeStamp(this.getJoinTimestamp());
        peerChannelRelation.setUserPrivateKey(this.getUserPrivateKey());
        peerChannelRelation.setUpdateTimeStamp(this.getUpdateTimestamp());
        peerChannelRelation.setActionType(this.getActionType());
        return peerChannelRelation;
    }
}
