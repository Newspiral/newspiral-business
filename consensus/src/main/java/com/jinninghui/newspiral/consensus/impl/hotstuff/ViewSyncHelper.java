package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.common.entity.consensus.View;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.gateway.entity.QueryViewNoReq;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Scope("prototype")
public class ViewSyncHelper {
    /**
     * 定时恢复节点可用性字段
     */
    private long internal = 300000;

    private long lastUpdateTimeStamp = 0;

    private ConsensusContext consensusContext;

    @SofaReference
    private SecurityServiceMgr securityServiceMgr;

    @SofaReference
    private P2pClient p2pClient;

    public void init(ConsensusContext consensusContext) {
        this.consensusContext = consensusContext;
    }

    public boolean trySyncHighAndViewNo() {
        log.info(ModuleClassification.ConM_NSHP_+","+consensusContext.getChannel().getChannelId()+" ,Try to sync the view number with other peers");
        if(consensusContext.getOrderedPeerList().size()==1)
        {
            log.info(ModuleClassification.ConM_NSHP_+", only one peer");
            return true;
        }
        updatePeerAvailable();
        QueryViewNoReq queryViewNoReq = createQueryViewNoReq();
        Map<Long, Integer> viewCount = new HashMap<>();
        Map<String, QueryViewNoResp> viewNoRespMap = new ConcurrentHashMap<>();
        Integer heighterCount = 0;
        QueryViewNoResp queryViewNoRespOfMyself = new QueryViewNoResp();
        queryViewNoRespOfMyself.setHeight(consensusContext.getBlockHeight());
        queryViewNoRespOfMyself.setViewNo(consensusContext.getCurrView().getNo());
        viewNoRespMap.put(consensusContext.myself.getPeerId().getValue(), queryViewNoRespOfMyself);
        consensusContext.getOrderedPeerList().parallelStream().forEach(peer -> {
            try {
                //排除本地节点哈
                if (peer.getIsLocalPeer()) {
                    return;
                }
                if (peer.isAvailable() == false) {
                    log.info(ModuleClassification.ConM_NSHP_.toString() + " peer may be not available:" + peer.getPeerId().toString());
                    return;
                }
                QueryViewNoResp queryViewNoResp = p2pClient.queryViewNo(queryViewNoReq, peer);
                if (null != queryViewNoResp) {
                    viewNoRespMap.put(peer.getPeerId().getValue(), queryViewNoResp);
                } else {
                    peer.setAvailable(false);
                    log.info(ModuleClassification.ConM_NSHP_.toString() + " unable to get view from peer " + peer.getPeerId().toString());
                }
            } catch (Exception ex) {
                log.info(ModuleClassification.ConM_NSHP_ +","+consensusContext.getChannel().getChannelId()+ ",exception in query view from peer " + peer.getPeerId().toString(), ex);
            }
        });
        long curr = System.currentTimeMillis();
        //wait for resp, at most 2 seconds.
        while (System.currentTimeMillis() - curr < 1000 && viewNoRespMap.size() < consensusContext.getQcMinNodeCnt()) {
            continue;
        }

        Iterator<Map.Entry<String, QueryViewNoResp>> iterator = viewNoRespMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, QueryViewNoResp> entry = iterator.next();
            QueryViewNoResp queryViewNoResp = entry.getValue();
            //height
            if (queryViewNoResp.getHeight() > consensusContext.getBlockHeight() + 1) {
                heighterCount++;
            }
            //view
            if (queryViewNoResp.getHeight().equals(consensusContext.getBlockHeight())) {
                Integer cnt = viewCount.get(queryViewNoResp.getViewNo());
                if (null == cnt) {
                    viewCount.put(queryViewNoResp.getViewNo(), 1);
                } else {
                    viewCount.put(queryViewNoResp.getViewNo(), cnt + 1);
                }
            }
        }
        //syncheigh
        //高度的同步判断影响了节点加入时的流程，先注释此处，应该只会影响超时恢复的快慢
        if (trySyncHeight(consensusContext.getOrderedPeerList().size() - 1, heighterCount)) {
            return false;
        }
        log.info(ModuleClassification.ConM_Sync_.toString() + consensusContext.getChannel().getChannelId() + " size of view map is " + viewCount.size());
        //syncView
        trySyncViewNo(viewCount);
        return true;
    }

    private void updatePeerAvailable() {
        if (System.currentTimeMillis() - lastUpdateTimeStamp > internal) {
            lastUpdateTimeStamp = System.currentTimeMillis();
            for (Peer peer: consensusContext.getOrderedPeerList()) {
                peer.setAvailable(true);
            }
        }
    }

    private QueryViewNoReq createQueryViewNoReq() {
        QueryViewNoReq queryViewNoReq = new QueryViewNoReq();
        queryViewNoReq.setChannelId(consensusContext.getChannel().getChannelId());
        queryViewNoReq.setBlockHeight(consensusContext.getBlockHeight());
        queryViewNoReq.setSignerIdentityKey(null);
        getLocalSecurityService().hash(queryViewNoReq);
        getLocalSecurityService().signByGMCertificate(queryViewNoReq, consensusContext.getChannel().getChannelId());
        return queryViewNoReq;
    }

    private boolean trySyncHeight(Integer total, Integer heighterCount) {
        //一半的高度超过本节点
        if (total <= heighterCount * 2) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + " left behind, set to WAIT_SYNC_WITH_CHANNEL");
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
            return true;
        }
        return false;
    }

    private void trySyncViewNo(Map<Long, Integer> viewCount) {
        if (viewCount.size() == 0) {
            log.warn(ModuleClassification.ConM_Sync_.toString() + consensusContext.getChannel().getChannelId() +
                    " unable to fetch any view info from other peers");
        }
        Long viewChosen = consensusContext.getCurrView().getNo();
        Integer cnt = new Integer(0);
        for (Map.Entry<Long, Integer> entry : viewCount.entrySet()) {
            if (entry.getValue() > cnt) {
                viewChosen = entry.getKey();
                cnt = entry.getValue();
            } else if (entry.getValue().equals(cnt) && viewChosen < entry.getKey()) {
                viewChosen = entry.getKey();
            }
        }
        if (consensusContext.getHighestQC().getBlockViewNo() >= viewChosen) {
            log.warn(ModuleClassification.ConM_NSHP_ +","+consensusContext.getChannel().getChannelId()+ ", Unreasonable view number of most of peers");
            viewChosen = consensusContext.getCurrView().getNo();
        }
        if (false == viewChosen.equals(consensusContext.getCurrView().getNo())) {
            //当且仅当本节点的视图和大多数节点的视图值不一致，才进行视图跳转
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() +","+consensusContext.getChannel().getChannelId()+
                    " ,adjust view to " + viewChosen + consensusContext.getConsensusInfo());
            View view = new View();
            view.setNo(viewChosen - 1);
            view.setStartTimestamp(System.currentTimeMillis());
            view.setExpiredTimestamp(consensusContext.getCurrentTimeOut());
            consensusContext.setCurrView(view);
        }
    }

    private SecurityService getLocalSecurityService() {
        return securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
    }

}
