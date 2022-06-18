package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.chain.PeerFrozenUnFrozen;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.ledger.mgr.impl.LedgerMgrImpl;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerCertificateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.ChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.IdentityModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerCertificateModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerModelMapper;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class PeerStateAction implements PersistActionInterface<Channel> {


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


    @Override
    public void doAdd(Channel newChannel) {
        boolean isLocal=false;
        //查询本地节点
        Peer localPeer = ledgerMgr.queryLocalPeer();
        //从通道中拿到改变的数据,有一个字段记录了是冻结操作还是解冻操作
        PeerFrozenUnFrozen peer = (PeerFrozenUnFrozen) newChannel.getChannelChange().getActionData();
        if (peer.getPeerId().getType().equals(localPeer.getPeerId().getType())
            && peer.getPeerId().getValue().equals(localPeer.getPeerId().getValue())) {
            isLocal=true;
        }
        PeerChannelModelWithBLOBs peerChannelModel;
        //冻结操作还是解冻操作
        //冻结操作则新插入一条记录，解冻则对最新的一条记录操作。
        if (peer.getFlag().equals("1")) {
            //冻结操作
            peerChannelModel = new PeerChannelModelWithBLOBs();
            peerChannelModel.setChannelId(newChannel.getChannelId());
            peerChannelModel.setPeerId(peer.getPeerId().getValue());
            peerChannelModel.setInBlockHeight(newChannel.getLatestChannelChangeHeight());
            peerChannelModel.setOutBlockHeight(0L);
            peerChannelModel.setJoinTimestamp(new Date());
            peerChannelModel.setUpdateTimestamp(new Date());
            peerChannelModel.setActionType(PeerActionTypeEnum.FROZEN.getCode());
            //查询到Peer_Channel表中最新的一条进入退出的记录
            PeerChannelModel inOutPeerRecord = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.IN_OUT.getCode());
            peerChannelModel.setExtendedData(inOutPeerRecord.getExtendedData());
        } else {
            //解冻操作，查询peer_channel表中最新的一条冻结解冻操作的记录
            peerChannelModel = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.FROZEN.getCode());
            peerChannelModel.setOutBlockHeight(newChannel.getLatestChannelChangeHeight());
            peerChannelModel.setUpdateTimestamp(new Date());
        }
        //进行更新操作，没有就插入
        log.info(ModuleClassification.LedM_LMI_ + "updatePeerChannelModel ");
        int peerChannelRow = peerChannelMapper.updateByPeerIdAndChannel(peerChannelModel);
        if (peerChannelRow <= 0) {
            log.info(ModuleClassification.LedM_LMI_ + " insert peerChannel {}, in {}, out {}", peerChannelModel.getPeerId(), peerChannelModel.getInBlockHeight(), peerChannelModel.getOutBlockHeight());
            int insertRow = peerChannelMapper.insert(peerChannelModel);
            if(insertRow<=0){
                log.error(ModuleClassification.LedM_LMI_ + " insert peerChannel fail");
            }
        } else {
            log.info(ModuleClassification.LedM_LMI_ + "update peer {}, in {}, out {}", peerChannelModel.getPeerId(), peerChannelModel.getInBlockHeight(), peerChannelModel.getOutBlockHeight());
        }

        //修改交易模块的缓存
        if(isLocal) {
            consensusMsgProcessor.reloadLocalPeer();
        }
    }

    @Override
    public void doRemove(Channel newChannel) {

    }

    @Override
    public void doModify(Channel newChannel) {

    }

    @Override
    public void doRest(Channel newChannel) {

    }
}
