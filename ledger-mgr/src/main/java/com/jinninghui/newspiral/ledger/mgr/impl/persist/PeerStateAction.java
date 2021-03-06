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
        //??????????????????
        Peer localPeer = ledgerMgr.queryLocalPeer();
        //?????????????????????????????????,?????????????????????????????????????????????????????????
        PeerFrozenUnFrozen peer = (PeerFrozenUnFrozen) newChannel.getChannelChange().getActionData();
        if (peer.getPeerId().getType().equals(localPeer.getPeerId().getType())
            && peer.getPeerId().getValue().equals(localPeer.getPeerId().getValue())) {
            isLocal=true;
        }
        PeerChannelModelWithBLOBs peerChannelModel;
        //??????????????????????????????
        //?????????????????????????????????????????????????????????????????????????????????
        if (peer.getFlag().equals("1")) {
            //????????????
            peerChannelModel = new PeerChannelModelWithBLOBs();
            peerChannelModel.setChannelId(newChannel.getChannelId());
            peerChannelModel.setPeerId(peer.getPeerId().getValue());
            peerChannelModel.setInBlockHeight(newChannel.getLatestChannelChangeHeight());
            peerChannelModel.setOutBlockHeight(0L);
            peerChannelModel.setJoinTimestamp(new Date());
            peerChannelModel.setUpdateTimestamp(new Date());
            peerChannelModel.setActionType(PeerActionTypeEnum.FROZEN.getCode());
            //?????????Peer_Channel??????????????????????????????????????????
            PeerChannelModel inOutPeerRecord = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.IN_OUT.getCode());
            peerChannelModel.setExtendedData(inOutPeerRecord.getExtendedData());
        } else {
            //?????????????????????peer_channel????????????????????????????????????????????????
            peerChannelModel = peerChannelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(),newChannel.getChannelId(), PeerActionTypeEnum.FROZEN.getCode());
            peerChannelModel.setOutBlockHeight(newChannel.getLatestChannelChangeHeight());
            peerChannelModel.setUpdateTimestamp(new Date());
        }
        //????????????????????????????????????
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

        //???????????????????????????
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
