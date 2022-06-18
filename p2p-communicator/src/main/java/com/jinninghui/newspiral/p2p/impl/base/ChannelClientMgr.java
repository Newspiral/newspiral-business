package com.jinninghui.newspiral.p2p.impl.base;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface.BroadcastScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * @author lida
 * @date 2019/7/29 17:43
 * 某个Channel的所有Client的管理者
 */
@Slf4j
@Component
public class ChannelClientMgr {

    /**
     * key为对端节点的身份标识，不给get/set方法,只有正常的节点才有ServiceForPeerClient，用来节点之间通信
     */
    private Map<String, ServiceForPeerClient> clientsMap = new ConcurrentHashMap<>();

    private Channel channel;

    private ForkJoinPool forkJoinPool = new ForkJoinPool(16);


    /**
     * location peer
     */
    private static Peer sourcePeer;
    @SofaReference
    private LedgerMgr ledgerMgr;

    public void initDb() {
        try {
            //该peer对象从数据库中单独读出来，与通道中的peer对象不是同一个对象。
            sourcePeer = ledgerMgr.queryLocalPeer();
            log.info(ModuleClassification.P2PM_CCM_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",ChannelClientMgr.initDb,sourcePeer={}", sourcePeer.getPeerId());
        } catch (Exception ex) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr(p2p模块)初始化异常,这会导致系统无法正常工作，退出系统:", ex);
            System.exit(1);
        }
    }

    /**
     * 初始化
     *
     * @param channel
     */
    synchronized public void init(Channel channel) {
        log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.init,Channel={}", channel.toString());
        clientsMap.clear();//保证可重入
        this.channel = channel;
        for (Peer peer : channel.getMemberPeerList()) {
            /*if (peer.equals(sourcePeer)) {
                continue;
            }*/
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.init.start,init ServiceForPeerClient,peer={}", peer.getPeerId());
            ServiceForPeerClient client = new ServiceForPeerClient();
            client.init(peer);
            clientsMap.put(peer.getPeerId().toString(), client);
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.init.end,init ServiceForPeerClient,peer={}", peer.getPeerId());
        }

        log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.init，构造了" + clientsMap.size() + "个ServiceForPeerClient对象");
    }

    public ServiceForPeerClient getClient(IdentityKey identityKey) {
        return clientsMap.get(identityKey.toString());
    }
    /**
     * 广播消息至除了excludePeerList之外的节点
     *
     * @param sdkTransaction
     * @param excludePeerList
     */
    public void broadcastTransaction(SDKTransaction sdkTransaction, Set<Peer> excludePeerList, BroadcastScope scope) {
        //log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ChannelClientMgr.broadcastTransaction.start,sdkTransaction.ClientTxId={},scope={}",sdkTransaction.getClientTxId(),excludePeerList,scope);

        // 计算随机节点列表
        ArrayList<Integer> seedList = new ArrayList<Integer>();
        if (BroadcastScope.TwentyFivePercentage.equals(scope)) {
            /*Random random = new Random();
            int actuallyPeerSize = clientsMap.size() - excludePeerList.size();
            for (int seed = 0; seed < actuallyPeerSize * 0.25; seed++) {
                seedList.add(random.nextInt(actuallyPeerSize));
            }*/
            return;
            //return;
        }
        // 确认所有需要发送的节点
        int index = 0;
        for (ServiceForPeerClient client : clientsMap.values()) {
            if (!excludePeerList.contains(client.getTargetPeer())) {
                // 发送节点序列
                index++;
                //log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ChannelClientMgr.broadcastTransaction,sourcePeer.ip={},targetPeer.ip={}," +
                //        "targetPeer={}",sourcePeer.getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER),
                //        client.getTargetPeer().getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER),client.getTargetPeer());
                if (seedList.size() == 0 || seedList.contains(index)) {
                    client.addSDKTranscation(sdkTransaction);
                }
            }

        }


    }


    /**
     * 广播消息至除了excludePeerList之外的节点
     *
     * @param genericMsg
     * @param excludePeerList
     */
    public void broadcastGenericMsg(GenericMsg genericMsg, Set<Peer> excludePeerList) {
        long startSumbitTime = System.currentTimeMillis();
/*        ForkJoinTask forkJoinTask = this.forkJoinPool.submit(() -> {
            clientsMap.entrySet().parallelStream().forEach(
                    entry -> {
                        if (false == excludePeerList.contains(entry.getValue().getTargetPeer())) {
                            long curr = System.currentTimeMillis();
                            entry.getValue().addGenericMsg(genericMsg);
                            log.error("target {},add genericMsg time {}, block {}, genericMsg size {},waitThreadTime {}",
                                    entry.getValue().getTargetPeer().getServiceUrls(),System.currentTimeMillis() - curr,
                                    genericMsg.getHotStuffDataNode().getBlock().getHash(),
                                    genericMsg.getHotStuffDataNode().getBlock().getTransactionList().size(),curr-startSumbitTime);
                        }
                    }
            );
        });
        try {
            forkJoinTask.get();
        } catch (Exception ex) {
            log.info(channel.getChannelId()+"Exception in broadcast GenericMsg:", ex);
        }*/
        try {
            clientsMap.entrySet().stream().forEach(
                    entry -> {
                        if (false == excludePeerList.contains(entry.getValue().getTargetPeer())) {
                            long curr = System.currentTimeMillis();
                            entry.getValue().addGenericMsg(genericMsg);
                            log.info("target {},add genericMsg time {}, block {}, genericMsg size {},waitThreadTime {}",
                                    entry.getValue().getTargetPeer().getServiceUrls(), System.currentTimeMillis() - curr,
                                    genericMsg.getHotStuffDataNode().getBlock().getHash(),
                                    genericMsg.getHotStuffDataNode().getBlock().getTransactionList().size(), curr - startSumbitTime);
                        }
                    }
            );
        } catch (Exception ex) {
            log.error(channel.getChannelId() + "Exception in broadcast GenericMsg:", ex);
        }

    }

    public void sendNewView(NewViewMsg newViewMsg, Peer targetPeer) {
        ServiceForPeerClient client = clientsMap.get(targetPeer.getPeerId().toString());
        if (client != null) {
            client.addNewViewMsg(newViewMsg);
        } else {
            log.warn("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.sendNewView,根据输入的targetPeer无法找到对应的client,放弃NewViewMsg发送,targetPeer:" + targetPeer.toString());
        }
    }

    public void sendBlockVoteMsg(BlockVoteMsg voteMsg, Peer targetPeer) {
        //log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ChannelClientMgr.sendBlockVoteMsg.start,voteMsg.BusinessKey={},targetPeer.id={}",voteMsg.getBusinessKey(),targetPeer.getPeerId().getValue());
        ServiceForPeerClient client = clientsMap.get(targetPeer.getPeerId().toString());
        if (client != null) {
            /*log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ChannelClientMgr.broadcastGenericMsg,sourcePeer.ip={},targetPeer.ip={}" ,sourcePeer.getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER),
                    client.getTargetPeer().getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER));*/
            client.addBlockVoteMsg(voteMsg);
        } else {
            log.warn("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ChannelClientMgr.sendBlockVoteMsg,根据输入的targetPeer无法找到对应的client,放弃BlockVoteMsg发送,targetPeer:" + targetPeer.toString());
        }
    }

    public void updateChannelPeer(Channel newChannel, Long increase) {
        //查询该高度通道中所有没有退出通道的节点列表
        List<Peer> peerList = newChannel.getMemberPeerListNotExitChannel(newChannel.getLatestChannelChangeHeight() + increase);
        //则该高度所有退出通道的节点列表为差集
        List<Peer> hasExitChannelPeerList = newChannel.getMemberPeerListExitChannel(newChannel.getLatestChannelChangeHeight() + increase);
        //this.clientsMap.clear();
        //清掉无用的客户端
        if (!CollectionUtils.isEmpty(hasExitChannelPeerList)) {
            hasExitChannelPeerList.stream().forEach(peer -> clientsMap.remove(peer.getPeerId().toString()));
        }
        Set<String> validClientSet = new HashSet<>();
        for (Peer peer : peerList) {
            validClientSet.add(peer.getPeerId().toString());
            if (clientsMap.get(peer.getPeerId().toString()) != null) {
                continue;
            }
            ServiceForPeerClient client = new ServiceForPeerClient();
            client.init(peer);
            clientsMap.put(peer.getPeerId().toString(), client);
        }
        Iterator<Map.Entry<String, ServiceForPeerClient>> iterator = clientsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceForPeerClient> entry = iterator.next();
            if (validClientSet.contains(entry.getKey()) == false) {
                iterator.remove();
            }
        }
        //todo:update channel.
    }


}
