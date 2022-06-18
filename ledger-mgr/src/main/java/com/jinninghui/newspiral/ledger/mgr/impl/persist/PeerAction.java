package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.ledger.mgr.impl.LedgerMgrImpl;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.IdentityModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerCertificateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.ChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.IdentityModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MemberMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerCertificateModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerModelMapper;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.jinninghui.newspiral.ledger.mgr.impl.domain.IdentityModel.transforIdentityKeyBrief;

@Slf4j
@Component
public class PeerAction implements PersistActionInterface<Channel> {

    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;

    @SofaReference
    private SecurityServiceMgr securityMgr;

    @Autowired
    private LedgerMgrImpl ledgerMgr;

    @Autowired
    private PeerModelMapper peerMapper;

    @Autowired
    private IdentityModelMapper identityMapper;

    @Autowired
    private PeerChannelModelMapper peerChannelMapper;

    @Autowired
    private PeerCertificateModelMapper peerCertificateMapper;

    @Autowired
    private ChannelModelMapper channelMapper;

    @Autowired
    private MemberMapper memberMapper;



    @Override
    public void doAdd(Channel newChannel) {
        Peer localPeer = ledgerMgr.queryLocalPeer();
        Peer peer = (Peer) newChannel.getChannelChange().getActionData();
        //判断节点是否存在，节点如果不存在，则在peer表中插入一条记录
        if (peerMapper.selectByPrimaryKey(peer.getPeerId().getValue()) == null) {
            PeerModel peerModel = PeerModel.createInstance(peer);
            peerModel.setIsLocalPeer(0);
            peerModel.setCertificateKeyStoreFile("");
            peerMapper.insert(peerModel);
        }
        //判断节点身份是否存在，没有就在identity表中插入一条
        String identityKeyStr = JSON.toJSONString(transforIdentityKeyBrief(peer.getPeerId()));
        if (identityMapper.selectByPrimaryKey(identityKeyStr) == null) {
            Identity identity = new Identity();
            identity.setKey(peer.getPeerId());
            identity.setParentKey(peer.getOrgId());
            IdentityModel identityModel = IdentityModel.createInstance(identity);
            identityModel.setSetupTimestamp(new Date());
            identityMapper.insert(identityModel);
            ledgerMgr.putIntoIdentityMap(identityKeyStr, identity);
        }
        //判断节点证书是否存在，如果没有的话就在peerCertificate表中插入
        if ( peerCertificateMapper.selectByPeerIdAndChannelId(peer.getPeerId().getValue(),newChannel.getChannelId()) == null) {
            //TODO 插入节点证书
            ledgerMgr.insertPeerChannelCertificate(peer, newChannel, localPeer, newChannel.getLatestChannelChangeHeight());
        }
        //处理组织证书，在member表中插入记录,判断在该方法中
        ledgerMgr.insertOrganizationMember(peer.getPeerOrganization(), newChannel.getChannelId());

        //在peer_channel表中增加一条记录（以下两种情况均为增加一条新记录）
        //1.该节点第一次加入 2.该节点不是第一次加入
        PeerChannelModelWithBLOBs peerChannelModel = new PeerChannelModelWithBLOBs();
        peerChannelModel.setChannelId(newChannel.getChannelId());
        peerChannelModel.setPeerId(peer.getPeerId().getValue());
        peerChannelModel.setInBlockHeight(newChannel.getLatestChannelChangeHeight());
        peerChannelModel.setOutBlockHeight(0L);
        peerChannelModel.setJoinTimestamp(new Date());
        peerChannelModel.setUpdateTimestamp(new Date());
        peerChannelModel.setActionType(PeerActionTypeEnum.IN_OUT.getCode());
        //TODO 这里需要用groupBy来分组，extendedData是通道中节点加入的数量
        //这个数量在启动的时候会用到，每个节点的这个字段是相同的
        int count = peerChannelMapper.selectCountByChannelId(newChannel.getChannelId(),peerChannelModel.getPeerId());
        peerChannelModel.setExtendedData(String.valueOf(count));

        PeerChannelModelWithBLOBs latestInOutRecord = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(), newChannel.getChannelId(), PeerActionTypeEnum.IN_OUT.getCode());
        if ( latestInOutRecord == null || latestInOutRecord.getOutBlockHeight() !=  0L) {
            //说明没有记录，或者已经退出了，可以插入
            if (latestInOutRecord!=null) {
                peerChannelModel.setExtendedData(latestInOutRecord.getExtendedData());
            }
            log.info(ModuleClassification.LedM_LMI_ + " insert peerChannel {}, in {}, out {}", peerChannelModel.getPeerId(), peerChannelModel.getInBlockHeight(), peerChannelModel.getOutBlockHeight());
            int insertRow = peerChannelMapper.insert(peerChannelModel);
            if(insertRow<=0){
                log.error(ModuleClassification.LedM_LMI_ + " insert peerChannel fail");
            }
        }
    }

    @Override
    public void doRemove(Channel newChannel) {
        Peer peer = (Peer) newChannel.getChannelChange().getActionData();
        log.info(ModuleClassification.LedM_LMI_ + "delete peer {}", peer.getPeerId().getValue());
        //TODO peerChannel表修改一条记录
        //查询peer表中加入退出类型最大的一条操作记录
        PeerChannelModelWithBLOBs latestInOutRecord = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.IN_OUT.getCode());
        PeerChannelModelWithBLOBs latestFrozenRecord = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.FROZEN.getCode());
        //如果该节点处于冻结状态，则需要同时将冻结的退出高度更新，并且，peer的最新记录应该是退出的这一条
        if (latestFrozenRecord != null
            && latestFrozenRecord.getInBlockHeight() >= latestInOutRecord.getInBlockHeight()
            && latestFrozenRecord.getOutBlockHeight() == 0) {
            latestFrozenRecord.setOutBlockHeight(newChannel.getLatestChannelChangeHeight());
            latestFrozenRecord.setUpdateTimestamp(new Date());
            //更新冻结解冻记录到peer_channel表
            peerChannelMapper.updateByPeerIdAndChannel(latestFrozenRecord);
            log.info(ModuleClassification.LedM_LMI_ + "update peer {}, in {}, out {}", latestFrozenRecord.getPeerId(), latestFrozenRecord.getInBlockHeight(), latestFrozenRecord.getOutBlockHeight());
        }
        latestInOutRecord.setOutBlockHeight(newChannel.getLatestChannelChangeHeight());
        //退出的时间应该人为延长一些，保证冻结和退出的时间逻辑顺序关系，这样读的时候按照时间就读到了退出操作
        latestInOutRecord.setUpdateTimestamp(new Date(System.currentTimeMillis()+1000L));
        //更新添加退出记录到peer_channel表
        peerChannelMapper.updateByPeerIdAndChannel(latestInOutRecord);
        log.info(ModuleClassification.LedM_LMI_ + "update peer {}, in {}, out {}", latestInOutRecord.getPeerId(), latestInOutRecord.getInBlockHeight(), latestInOutRecord.getOutBlockHeight());
        //节点移除
        ledgerMgr.removeDeletedPeerOfChannel(newChannel, peer);
    }

    @Override
    public void doModify(Channel newChannel) {
        boolean isLocal=false;
        Peer localPeer = ledgerMgr.queryLocalPeer();
        Peer peer = (Peer) newChannel.getChannelChange().getActionData();
        PeerModel peerModel = PeerModel.createInstance(peer);
        if (!peer.equals(localPeer)) {
            peerModel.setCertificateKeyStoreFile("");
        }
        else {
            isLocal=true;
        }
        //在peer表中替换了节点的证书
        int nRow = peerMapper.updatePeerCertificateByPrimaryKey(peerModel);
        int nPeercert = 0;
        //删除节点证书
        //peerCertificateModelMapper.deleteByPeerId(updatePeer.getPeerId().getValue(), newChannel.getChannelId());
        for (PeerCert peerCert : peer.getPeerCert()) {
            PeerCertificateModel peerCertificateModel = PeerCertificateModel.createInstance(peerCert);
            if (null == peerCertificateModel.getBlockHeight()) {
                peerCertificateModel.setBlockHeight(newChannel.getLatestChannelChangeHeight());
            }
            if (!peer.equals(localPeer)) {
                peerCertificateModel.setCertificateKeyStoreFile("");
                peerCertificateModel.setIsLocalPeer(0);
            }
            else
            {
                peerCertificateModel.setIsLocalPeer(1);
            }
            //TODO 修改状态、高度以及时间
            peerCertificateModel.setUpdateTime(new Date());
            nPeercert = peerCertificateMapper.updatePeerCertificateFlagByPrimaryKey(peerCertificateModel);
            if (nPeercert <= 0) {
                peerCertificateModel.setUpdateTime(null);
                nPeercert = peerCertificateMapper.insert(peerCertificateModel);
            }
        }
        if (nRow > 0 || nPeercert > 0) {
            ChannelModel channelModel = channelMapper.selectByPrimaryKey(newChannel.getChannelId());
            SecurityService securityService = securityMgr.getMatchSecurityService(channelModel.getSecurityServiceKey());
            securityService.clearPeerCertificateCipherMap(peerModel.getPeerIdValue(), channelModel.getChannelId());
            ledgerMgr.removePeersFromMap(peerModel.getPeerIdValue());
            ledgerMgr.removePeersFromMap(newChannel.getChannelId());
            //直接改内存
            consensusMsgProcessor.processPeerCertificateByPeerId(ledgerMgr.queryPeerCertificateList(peerModel.getPeerIdValue(), newChannel.getChannelId()), newChannel.getChannelId());
        }

        //修改交易模块的缓存
        if(isLocal) {
            consensusMsgProcessor.reloadLocalPeer();
        }

    }

    @Override
    public void doRest(Channel channel) {

    }

}
