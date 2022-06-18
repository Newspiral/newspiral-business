package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.chain.*;
import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import com.jinninghui.newspiral.common.entity.common.base.BizVO;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.identity.IdentityTypeEnum;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.member.*;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractListReq;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractReq;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractDeployToken;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractInfo;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractOperationTypeEnum;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractStateEnum;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractUpdateStateToken;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.TransactionCompile;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SmartContractClassLoader;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SmartContractCompile;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;

import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.ledger.mgr.contract.SystemContractBase;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lida
 * @date 2019/9/12 18:53
 * 系统的智能合约，实现管理类功能
 */
//@Api(tags = "系统智能合约")  //TODO 获取文档后注释
//@RestController     //TODO 获取文档后注释
@Configuration
@Slf4j
//@RequestMapping(value = "/systemSmartContract", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)  //TODO 获取文档后注释
public class SystemSmartContract extends SystemContractBase {

    @Value("${newsprial.sdk.peer.agreeUnfreeze:false}")
    private String unfreeze;
    @Getter
    @Setter
    private LedgerMgr ledgerMgr;

    @Getter
    @Setter
    private MemberLedgerMgr memberLedgerMgr;

    @SofaReference
    private DataSecurityMgr dataSecurityMgr;

    //@NewSpiralPolicy(role = "organizationChannelAdministrator", rule = "2/3")
    @ApiOperation("通道添加节点")
    //@PostMapping(value = "/addOnePeer")
    public void addOnePeer(@RequestBody List<AddPeer2ChannelToken> tokenList) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.addOnePeer.start");
        Channel channel = checkAddPeerParam(tokenList);
        Peer peer = Peer.createInstance(tokenList.get(0).getNewPeer());
        channel.addOnePeer(peer);
        //log.info(ModuleClassification.TxM_SSC_+"addOnePeer,peerListSize={}", channel.getMemberPeerList().size());
        ChannelChange<Peer> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.peerAdd);
        channelChange.setActionData(peer);
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }

    //@NewSpiralPolicy(role = "organizationChannelAdministrator", rule = "2/3")
    @ApiOperation("部署智能合约")
    //@PostMapping(value = "/deploySmartContract")
    public void deploySmartContract(@RequestBody List<SmartContractDeployToken> tokenList) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.deploySmartContract.start");

        // 先从缓存池中查找当前智能合约是否已经完成编译，如果已预编译，则直接提取即可；否则重新编译
        SmartContract smartContractCopy = new SmartContract();
        SmartContractInfo smartContractInfoInput = tokenList.get(0).getSmartContractInfo();
        TransactionCompile transactionCompile = this.ledgerMgr.getTransactionCompile(smartContractInfoInput.toString());
        if (transactionCompile != null) {
            smartContractCopy = transactionCompile.getSmartContract();
        } else {
            smartContractCopy = SmartContract.createInstance(tokenList.get(0).getSmartContractInfo());
            long startTime = System.currentTimeMillis();
            try {
                tokenList.get(0).getSmartContractInfo().toString();
                new SmartContractCompile().compileSmartContract(smartContractCopy);
            } catch (Exception e) {
                log.error("deploy smart contract error, still connection: {}", e.toString());
            }
            log.info("执行交易时编译，耗时：{}", System.currentTimeMillis() - startTime);
        }
        HashMap<String, byte[]> map = smartContractCopy.getInnerClassFileList();
        byte[] byteCode = smartContractCopy.getClassFileBytes();
        if (null != map) {
            Collection<byte[]> values = map.values();
            for (int i = 0; i < values.size(); i++) {
                byteCode = ArrayUtils.addAll(byteCode, map.get(i));
            }
        }
        smartContractCopy.setClassFileHash(this.dataSecurityMgr.getHash(Base64.getEncoder().encodeToString(byteCode)));
        //smartContractCopy.setClassFileHash(this.dataSecurityMgr.getHash(Base64.getEncoder().encodeToString(smartContractCopy.getClassFileBytes())));
        String name = smartContractCopy.getName();
        if (name.length() > 256) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级，别名过长");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        Channel channel = checkDeploySmartContract(tokenList, name);
        smartContractCopy.setFlag(SmartContractStateEnum.SMARTCONTRACT_VALID.getCode());
        smartContractCopy.setId(smartContractCopy.getChannelId() + "_" + smartContractCopy.getVersion() + "_" + smartContractCopy.getAlisa());
        ChannelChange<SmartContract> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.contractAdd);
        channelChange.setActionData(smartContractCopy);
        if (CollectionUtils.isEmpty(channel.getSmartContractList())) {
            List<SmartContract> smartContractList = new ArrayList<>();
            smartContractList.add(smartContractCopy);
            channel.setSmartContractList(smartContractList);
        } else {
            channel.getSmartContractList().add(smartContractCopy);
        }
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }

    @ApiOperation("冻结解冻节点")
    public void frozenUnFrozenPeer(@RequestBody List<PeerFrozenUnFrozenApproval> peerFrozenUnFrozenApprovals ){
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.peerFrozenUnFrozenApprovals.start");
        Channel channel = checkPeerFrozenUnFrozen(peerFrozenUnFrozenApprovals);
        //将冻结解冻节点的操作同步到快照中去，快照是写入到record中的，也用于节点之间进行结果比对
        PeerFrozenUnFrozen peerFrozenUnFrozen = peerFrozenUnFrozenApprovals.get(0).getPeerFrozenUnFrozen().clone();
        ChannelChange<PeerFrozenUnFrozen> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.peerFrozen);
        channelChange.setActionData(peerFrozenUnFrozen);
        channel.setChannelChange(channelChange);
        //写入到快照中，需要进行持久化的，最后结果写入record中
        ledgerMgr.updateChannel(channel);
    }

    /**
     * 校验冻结解冻节点的参数
     * @param peerFrozenUnFrozenApprovals
     * @return
     */
    private Channel checkPeerFrozenUnFrozen(List<PeerFrozenUnFrozenApproval> peerFrozenUnFrozenApprovals) {
        Channel channel = ledgerMgr.queryChannel(peerFrozenUnFrozenApprovals.get(0).getPeerFrozenUnFrozen().getChannelId());
        //1.通道不存在，抛出异常
        if (channel == null) {
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //2.参数满足要求
        PeerFrozenUnFrozen peerFrozenUnFrozen = peerFrozenUnFrozenApprovals.get(0).getPeerFrozenUnFrozen();
        //参数错误
        if (StringUtils.isEmpty(peerFrozenUnFrozen.getFlag()) ||
                StringUtils.isEmpty(peerFrozenUnFrozen.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        //操作类型只能为0或1（0 解冻 1 冻结）
        if (!peerFrozenUnFrozen.getFlag().equals("0") &&
                !peerFrozenUnFrozen.getFlag().equals("1")) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        //3.收集的投票（token）需要满足规定的要求
        Set<String> alreadyChecked = new HashSet<>();
        for (PeerFrozenUnFrozenApproval token : peerFrozenUnFrozenApprovals) {
            //3.1所有投票来自于同一个通道内
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", "节点冻结解冻时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点冻结解冻时，所有证明需使用相同的ChannelID");
            }
            //3.2所有投票应该使用相同的智能合约
            if (peerFrozenUnFrozen.equals(token.getPeerFrozenUnFrozen()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", "节点冻结解冻时，所有证明需使用相同的智能合约");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点冻结解冻时，所有证明需使用相同的智能合约");
            }
            //3.3所有投票不存在重复的组织
            if (alreadyChecked.contains(token.getSignerIdentityKey().getIdentityKey().getValue())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", "节点冻结解冻时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点冻结解冻时，存在重复的证明组织");
            } else {
                alreadyChecked.add(token.getSignerIdentityKey().getIdentityKey().getValue());
            }
        }
        //4节点以及节点的身份是否存在
        //4.1节点不存在
        Peer optPeer = getOperatePeer(channel.getMemberPeerList(), peerFrozenUnFrozen.getPeerId());
        if(optPeer == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.PEER_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_NOT_EXIST);
        }
        PeerCert peerCert = ledgerMgr.getPeerCertificate(peerFrozenUnFrozen.getPeerId().getValue(), channel.getChannelId());
        //4.2节点身份不存在,即该节点在该通道中没有有效的证书
        if (peerCert == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_IDENTITY_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_IDENTITY_ERROR);
        }
        //5如果是冻结操作,则节点必须是正常的状态
        if ("1".equals(peerFrozenUnFrozen.getFlag())
            && !optPeer.isState()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "peer is already frozen or quit channel");
        }
        //6如果是解冻操作，则该节点必须处于冻结的状态（状态为1）
        if ("0".equals(peerFrozenUnFrozen.getFlag())) {
            if (!(optPeer.getPeerChannelRelation().getActionType().equals(PeerActionTypeEnum.FROZEN.getCode())
                && optPeer.getPeerChannelRelation().getOutBlockHeight() == 0L)) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.INVALID_PARAM);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "peer must in frozen state");
            }
        }
        //7如果是冻结操作，必须保证冻结之后还有正常的节点存在
        List<Peer> memberPeerList = channel.getMemberPeerList();
        if(!CollectionUtils.isEmpty(memberPeerList)){
            long normalPeerCount = channel.getMemberPeerList().stream().filter(peer -> peer.isState()).count();
            if (normalPeerCount <= 1 && peerFrozenUnFrozen.getFlag().equals("1")) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkPeerFrozenUnFrozen.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "There must be at least one normalPeer in the channel");
            }
        }

        return channel;
    }

    /**
     * 拿到传入的节点
     * @param memberPeerList
     * @param peerId
     * @return
     */
    public Peer getOperatePeer(List<Peer> memberPeerList, IdentityKey peerId) {
        if (!CollectionUtils.isEmpty(memberPeerList)) {
            for (Peer peer : memberPeerList) {
                if ( peer.getPeerId().getValue().equals(peerId.getValue()) ) {
                    return peer;
                }
            }
        }
        return null;
    }

    //@NewSpiralPolicy(role = "organizationChannelAdministrator", rule = "2/3")
    @ApiOperation("通道移除节点")
    //@PostMapping(value = "/removeOnePeer")
    public void removeOnePeer(@RequestBody List<RemovePeerFromChannelToken> tokenList) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.removeOnePeer.start");
        Channel channel = checkRemovePeerParam(tokenList);
        Peer peer = new Peer();
        peer.setPeerId(tokenList.get(0).getPeerId());
        channel.removeOnePeer(peer);
        ChannelChange<Peer> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.peerRemove);
        channelChange.setActionData(peer);
        channel.setChannelChange(channelChange);
        //写入到快照中，需要进行持久化的
        ledgerMgr.updateChannel(channel);
    }

    @ApiOperation("从通道中移除节点自身")
    //@PostMapping(value = "/exitMyPeerFromChannel")
    public void exitMyPeerFromChannel(@RequestBody List<RemovePeerFromChannelToken> tokenList) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.exitMySelfFromChannel.start");
        Channel channel = checkExitOnePeerFromChannel(tokenList);
        Peer peer = new Peer();
        peer.setPeerId(tokenList.get(0).getPeerId());
        channel.removeOnePeer(peer);
        ChannelChange<Peer> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.peerRemove);
        channelChange.setActionData(peer);
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }

    //@NewSpiralPolicy(role = "organizationChannelAdministrator", rule = "2/3")

    /**
     * 修改合约的状态
     *
     * @param tokenList
     */
    @ApiOperation("更新、升级智能合约")
    //@PostMapping(value = "/updateSmartContractState")
    public void updateSmartContractState(@RequestBody List<SmartContractUpdateStateToken> tokenList) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.updateSmartContractFlag.start");
        //try {
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        SmartContract smartContract = checkUpdateSmartContractFlag(tokenList, channel);
        channel.getSmartContractList().add(smartContract);
        ChannelChange<SmartContract> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.contractAdd);
        channelChange.setActionData(smartContract);
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);

/*        } catch (Exception e) {
            log.warn(ModuleClassification.TxM_SSC_+"MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.deploySmartContract,e={}", e);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "合约修改状态时，处理错误");
        }*/
    }

    @ApiOperation("更新、升级节点证书状态")
    //@PostMapping(value = "/updatePeerCertificateState")
    public void updatePeerCertificateState(@RequestBody List<PeerCertificateStateApproval> peerCertificateStateApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.updatePeerCertificateState.start");
        Channel channel = ledgerMgr.queryChannel(peerCertificateStateApprovals.get(0).getChannelId());
        PeerCertificate peerCertificate = checkUpdatePeerCertificateState(peerCertificateStateApprovals, channel);
        //进行证书状态修改动作
        //ledgerMgr.updatePeerCertificateState(peerCertificate, peerCertificateStateApprovals.get(0).getPeerCertificateStateRequest().getFlag(), peerCertificateStateApprovals.get(0).getPeerCertificateStateRequest().getChannelId());
        //ledgerMgr.updateChannel(ledgerMgr.queryChannel(peerCertificateStateApprovals.get(0).getChannelId()));
        ledgerMgr.updateChannel(getChannelByPeerCertificate(peerCertificate, channel));
    }

    //获取关于证书状态的新的通道
    private Channel getChannelByPeerCertificate(PeerCertificate peerCertificate, Channel channel) {
        for (Peer peer : channel.getMemberPeerList()) {
            if (peer.getPeerId().equals(peerCertificate.getPeer().getPeerId())) {
                peer.setPeerCert(peerCertificate.getPeerCert());
                ChannelChange<Peer> channelChange = new ChannelChange<>();
                channelChange.setActionTag(PersistConstant.PersistTarget.peerModify);
                channelChange.setActionData(peer);
                channel.setChannelChange(channelChange);
                break;
            }
        }
        return channel;
    }

    @ApiOperation("通道块大小修改")
    //@PostMapping(value = "/updateChannelBlockMaxSize")
    public void updateChannelBlockMaxSize(@RequestBody List<ChannelBlockMaxSizeApproval> channelBlockMaxSizeApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.updateChannelBlockMaxSize.start");
        Channel channel = checkUpdateChannelBlockMaxSize(channelBlockMaxSizeApprovals);
        ChannelChange<Long> channelChange = new ChannelChange<>();
        channelChange.setActionTag(PersistConstant.PersistTarget.channelBlockMaxSizeModify);
        channelChange.setActionData(channelBlockMaxSizeApprovals.get(0).getChannelBlockMaxSizeRequest().getBlockMaxSize());
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }

    /**
     * 校验修改通道块大小的参数
     *
     * @param tokenList
     * @return
     */
    private Channel checkUpdateChannelBlockMaxSize(List<ChannelBlockMaxSizeApproval> tokenList) {
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateChannelBlockMaxSize,{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        ChannelBlockMaxSizeRequest channelBlockMaxSizeRequest = tokenList.get(0).getChannelBlockMaxSizeRequest();
        //参数错误
        if (null == channelBlockMaxSizeRequest.getBlockMaxSize() ||
                channelBlockMaxSizeRequest.getBlockMaxSize() <= 0 ||
                StringUtils.isEmpty(channelBlockMaxSizeRequest.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateChannelBlockMaxSize,{}", NewSpiralErrorEnum.SMART_CONTRACT_FLAG_PARAMETER_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_FLAG_PARAMETER_ERROR);
        }

        Set<IdentityKey> alreadyChecked = new HashSet<>();
        for (ChannelBlockMaxSizeApproval token : tokenList) {
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateChannelBlockMaxSize,{}", "系统智能合约修改状块状态时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "系统智能合约修改状块状态时，所有证明需使用相同的ChannelID");
            }
            if (alreadyChecked.contains(token.getSignerIdentityKey().getIdentityKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateChannelBlockMaxSize,{}", "系统智能合约修改状块状态时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "系统智能合约修改状块状态时，存在重复的证明组织");
            } else {
                alreadyChecked.add(token.getSignerIdentityKey().getIdentityKey());
            }
        }
        return channel;
    }

    /**
     * 校验更新、升级节点证书状态的入参
     *
     * @param peerCertificateStateApprovals
     * @param channel
     * @return
     */
    private PeerCertificate checkUpdatePeerCertificateState(List<PeerCertificateStateApproval> peerCertificateStateApprovals, Channel channel) {
        //1.通道不存在，抛出异常
        if (channel == null) {
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        PeerCertificateStateRequest peerCertificateStateRequest = peerCertificateStateApprovals.get(0).getPeerCertificateStateRequest();
        //参数错误
        if (StringUtils.isEmpty(peerCertificateStateRequest.getFlag()) ||
                StringUtils.isEmpty(peerCertificateStateRequest.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        //状态错误
        if (!peerCertificateStateRequest.getFlag().equals("0") &&
                !peerCertificateStateRequest.getFlag().equals("1") &&
                !peerCertificateStateRequest.getFlag().equals("2")) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
        }

        //TODO 先不让解冻
        if (peerCertificateStateRequest.getFlag().equals("0") && !"true".equals(unfreeze)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState 暂时不支持此动作-{}", NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
        }

        //
        long normalPeerCount = channel.getMemberPeerList().parallelStream().filter(peer -> peer.isState()).count();
        if (normalPeerCount <= 1 && peerCertificateStateRequest.getFlag().equals("1")) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "There must be at least one normalPeer in the channel");
        }

        Set<String> alreadyChecked = new HashSet<>();
        for (PeerCertificateStateApproval token : peerCertificateStateApprovals) {
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", "证书状态修改时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "证书状态修改时，所有证明需使用相同的ChannelID");
            }
            if (peerCertificateStateRequest.equals(token.getPeerCertificateStateRequest()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", "证书状态修改时，所有证明需使用相同的智能合约");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "证书状态修改时，所有证明需使用相同的智能合约");
            }
            if (alreadyChecked.contains(token.getSignerIdentityKey().getIdentityKey().getValue())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", "证书状态修改时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "证书状态修改时，存在重复的证明组织");
            } else {
                alreadyChecked.add(token.getSignerIdentityKey().getIdentityKey().getValue());
            }
        }
        //这里查询一下数据库进行校验吧
        PeerCertificate peerCertificate = ledgerMgr.getPeerCertificate(peerCertificateStateRequest.getPeerId(), channel.getChannelId());
        //节点身份不存在
        if (peerCertificate == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_IDENTITY_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_IDENTITY_ERROR);
        }
        //需要判断此时的状态
        //1.如果
        if (!StringUtils.isEmpty(peerCertificate.getPeerCert())) {
            for (PeerCert peerCert : peerCertificate.getPeerCert()) {
                if (peerCertificate.getPeer().getCertificateHash().equals(peerCert.getCertificateHash())
                        && peerCert.getFlag().equals("2")) {
                    if (peerCertificateStateRequest.getFlag().equals("0")) {
                        //吊销后证书不可以恢复
                        log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_REVOKE_NORECOVERY);
                        throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_REVOKE_NORECOVERY);
                    }
                    if (peerCertificateStateRequest.getFlag().equals("1")) {
                        //吊销后证书不可以冻结
                        log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_REVOKE_NOFROZEN);
                        throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_REVOKE_NOFROZEN);
                    }
                }
                if (peerCertificate.getPeer().getCertificateHash().equals(peerCert.getCertificateHash())
                        && peerCert.getFlag().equals(peerCertificateStateRequest.getFlag())) {
                    //状态未变提示报错
                    log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificateState.{}", NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_NOCHANGE);
                    throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_FLAG_NOCHANGE);
                }

                //最后无错误后直接赋值状态
                if (peerCertificate.getPeer().getCertificateHash().equals(peerCert.getCertificateHash())) {
                    peerCert.setFlag(peerCertificateStateRequest.getFlag());
                    peerCert.setBlockHeight(null);
                }
            }
        }
        return peerCertificate;
    }

    /**
     * 校验智能合约更新升级的入参
     *
     * @param tokenList
     * @param channel
     * @return
     */
    private SmartContract checkUpdateSmartContractFlag(List<SmartContractUpdateStateToken> tokenList, Channel channel) {

        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlag.{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        SmartContractUpdateStateToken firstToken = tokenList.get(0);
        //参数错误
        if (StringUtils.isEmpty(firstToken.getState()) ||
                StringUtils.isEmpty(firstToken.getAlisa()) ||
                StringUtils.isEmpty(firstToken.getVersion()) ||
                StringUtils.isEmpty(firstToken.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlag.{}", NewSpiralErrorEnum.SMART_CONTRACT_FLAG_PARAMETER_ERROR);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_FLAG_PARAMETER_ERROR);
        }


        checkUpdateSmartContractFlagList(tokenList, channel);
        //firstToken.setId(firstToken.getChannelId() + "_" + firstToken.getName() + "_" + firstToken.getVersion());
        //这里进行校验吧
        SmartContract matchSmartContract = null;
        for (SmartContract sc : channel.getSmartContractList()) {
            if (sc.getChannelId().equals(firstToken.getChannelId()) &&
                    sc.getAlisa().equals(firstToken.getAlisa()) &&
                    sc.getVersion().equals(firstToken.getVersion())) {
                matchSmartContract = sc.Clone();
                break;
            }
        }
        //该智能合约不存在
        if (matchSmartContract == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlag.{}", NewSpiralErrorEnum.SMART_CONTRACT_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_NOT_EXIST);
        }
        //需要判断此时的状态
        if (matchSmartContract.getFlag().equals(firstToken.getState().getCode())) {
            //状态无变提示报错
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlag.{}", NewSpiralErrorEnum.SMART_CONTRACT_FLAG_NOCHANGE);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_FLAG_NOCHANGE);
        }
        if (matchSmartContract.getFlag().equals(SmartContractStateEnum.SMARTCONTRACT_DESTORIED.getCode())) {
            //已经销毁不能操作
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlag.{}", NewSpiralErrorEnum.SMART_CONTRACT_FLAG_DESTORY);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_FLAG_DESTORY);
        }
        matchSmartContract.setFlag(firstToken.getState().getCode());
        return matchSmartContract;
    }

    /**
     * 校验证书
     *
     * @param tokenList
     * @param channel
     */
    private void checkUpdateSmartContractFlagList(List<SmartContractUpdateStateToken> tokenList, Channel channel) {
        SmartContractUpdateStateToken firstToken = tokenList.get(0);
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        Set<String> alreadyChecked = new HashSet<>();
        for (SmartContractUpdateStateToken token : tokenList) {
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlagList,合约状态变更时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "合约状态变更时，所有证明需使用相同的ChannelID");
            }
            if (alreadyChecked.contains(token.getSignerIdentityKey().getIdentityKey().getValue())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlagList,合约状态变更时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "合约状态变更时，存在重复的证明组织");
            } else {
                alreadyChecked.add(token.getSignerIdentityKey().getIdentityKey().getValue());
            }

            Member orgMember = orgMemberMap.get(token.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlagList,参数错误,token={}", token);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }

            //todo 验签
            if (!ledgerMgr.verifySign(token, orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateSmartContractFlagList,参数错误,token={}", JSONObject.toJSON(token));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }

        //检查合约修改状态策略
        int approvalCnt = tokenList.size();
        long size = orgMemberMap.size();
        switch (channel.getSmartContractDeplyStrategy()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改合约状态的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改合约状态的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改合约状态的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改合约状态的需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持修改合约状态");
        }

    }

    /**
     * 添加节点校验
     */
    private Channel checkAddPeerParam(List<AddPeer2ChannelToken> tokenList) {
        PeerInfo newPeer = tokenList.get(0).getNewPeer();
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam.start");
        //0.参数校验
        PeerChannelRelation peerRecord = ledgerMgr.selectLatestRecordByPeerIdAndChanneId(tokenList.get(0).getNewPeer().getPeerId().getValue(),tokenList.get(0).getChannelId(),PeerActionTypeEnum.IN_OUT.getCode());
        if (peerRecord == null) {
            if (newPeer.getCertificateCerFile().length==0) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,第一次添加新节点时，节点ca证书不应为空");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点ca证书不应为空");
            }
            //第一次加入，需要判断证书相关
            if (StringUtils.isEmpty(newPeer.getOrgId().getValue())
                || !newPeer.getOrgId().getType().equals(IdentityTypeEnum.CHINA_PKI)) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,第一次添加新节点时，节点所属组织身份不应为空");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点所属组织身份不应为空");
            }
            //节点组织
            if (StringUtils.isEmpty(newPeer.getPeerOrganization().getCertificateCerFile())
                || StringUtils.isEmpty(newPeer.getPeerOrganization().getOrganizationId())
                || StringUtils.isEmpty(newPeer.getPeerOrganization().getOrganizationName())
                || StringUtils.isEmpty(newPeer.getPeerOrganization().getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,第一次添加新节点时，节点组织相关参数不应为空");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点组织相关参数不应为空");
            }
        }
        //1.投票不为空
        if (tokenList.isEmpty()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,Token list is empty");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Token list is empty");
        }
        //2.通道存在
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //3.添加节点时，正常的节点未达到通道的上限
        long size = channel.getMemberPeerList().stream().filter(peer->peer.isState()).count();
        if (size >= channel.getMaxPeerCount()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，组织节点数已达到最大值");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，组织节点数已达到最大值");
        }
        //4.检查token
        IdentityKey newPeerId = newPeer.getPeerId();
        Set<IdentityKey> alreadyCheckedSet = new HashSet<>();
        for (AddPeer2ChannelToken token : tokenList) {
            //4.1所有投票必须来自于同一通道
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，所有证明需使用相同的ChannelID");
            }
            //4.2所有投票添加的是同一个节点
            if (token.getNewPeer().getPeerId().equals(newPeerId) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，所有证明需使用相同的新节点ID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，所有证明需使用相同的新节点ID");
            }
            //4.3所有投票不存在相同的组织
            if (alreadyCheckedSet.contains(token.getSignerIdentityKey().getIdentityKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，存在重复的证明组织");
            } else {
                alreadyCheckedSet.add(token.getSignerIdentityKey().getIdentityKey());
            }
        }

        //5.添加节点时，如果该节点存在，而且没有退出通道，则抛出异常
        Peer optPeer = getOperatePeer(channel.getMemberPeerList(), newPeerId);
        if (optPeer != null
            && !optPeer.isExitChannel()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加的节点已经在Channel中,addPeerId={},existPeer={}", newPeerId.getValue(), JSONObject.toJSON(optPeer));
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加的节点已经在Channel中,无法重复添加");
        }


        //TODO 安全测评使用
        //组织证书map
/*        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (AddPeer2ChannelToken token : tokenList) {
            String publicKey = token.getSignerIdentityKey().getIdentityKey().getValue();
            if (orgChannelAdminPublicKey.equals(publicKey)) {
                isChannelAdmin = true;
            }
            //todo 验签
*//*            if (!ledgerMgr.verifySign(token.clone(), publicKey)) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkAddPeerParam,参数错误,token={}", JSONObject.toJSON(token));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加节点的组织根证书签名错误");
            }*//*
        }
        if (!isChannelAdmin) {
            log.error(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkAddPeerParam,参数错误,添加节点时，用的不是创建通道的组织管理者签名");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加节点时，用的不是创建通道的组织管理者签名");
        }*/
        return channel;
    }

    private Channel checkDeploySmartContract(List<SmartContractDeployToken> tokenList, String name) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,start");
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        SmartContractInfo smartContract = tokenList.get(0).getSmartContractInfo();
        //校验一下参数
        if (StringUtils.isEmpty(smartContract.getAlisa()) ||
                StringUtils.isEmpty(smartContract.getVersion()) ||
                StringUtils.isEmpty(smartContract.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级，参数为空");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        //操作状态错误
        if (!smartContract.getOperationType().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_INSTALL) &&
                !smartContract.getOperationType().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_UPGRADE)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        if (smartContract.getAlisa().length() > 64 || smartContract.getVersion().length() > 22) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级，参数长度过长");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        // xxm 这里需要调整一波哈
        //smartContract.setMemberId(smartContract.getCode() + "_" +smartContract.getVersion());

        //这里查询一下数据库进行校验吧
/*        SmartContract form = ledgerMgr.getSmartContractByKey(smartContract.getName(), smartContract.getVersion(), smartContract.getChannelId());
        //智能合约已经存在
        if (form != null) {
            log.warn(ModuleClassification.TxM_SSC_+"MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.SMART_CONTRACT_ALREADY_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_ALREADY_EXIST);
        }*/
        //安装升级策略检查
        checkDeploySmartContractList(tokenList, channel);
        List<SmartContract> changeSmartContracts = new ArrayList<>();
        List<SmartContract> alreadyExistSmartContracts = channel.getSmartContractList();
        //是否存在要升级的合约
        boolean upgradeExist = false;
        for (SmartContract sc : alreadyExistSmartContracts) {

            if (smartContract.getOperationType().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_INSTALL)) {
                //安装时，类名重复报错
                if (sc.getName().equals(name) || sc.getAlisa().equals(smartContract.getAlisa())) {
                    log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.SMART_CONTRACT_ALREADY_EXIST);
                    throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_ALREADY_EXIST);
                }
            } else if (smartContract.getOperationType().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_UPGRADE)) {
                if (sc.getName().equals(name)) {
                    if (smartContract.getVersion().equals(sc.getVersion())) {
                        //name相同版本也相同报异常
                        log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_VERSION_EXIST);
                        throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_VERSION_EXIST);
                    }
                    //升级时相同的name直接吊销掉
                    if (!sc.getFlag().equals("3")) {
                        changeSmartContracts.add(sc.Clone());
                    }
                    upgradeExist = true;

                    //升级时别名必须一致
                    if (!sc.getAlisa().equals(smartContract.getAlisa())) {
                        log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_ALISA_NOTEXIST);
                        throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_ALISA_NOTEXIST);
                    }
                }
            } else {
                //啥都不做
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
            }
        }

        if (!upgradeExist && smartContract.getOperationType().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_UPGRADE)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,{}", NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_NOEXIST);
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_UPGRADE_NOEXIST);
        }
        //吊销的合约
        for (SmartContract changeForm : changeSmartContracts) {
            changeForm.setFlag("3");
            channel.getSmartContractList().add(changeForm);
        }
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,end");
        return channel;
    }

    private void checkDeploySmartContractList(List<SmartContractDeployToken> tokenList, Channel channel) {
        SmartContractInfo smartContract = tokenList.get(0).getSmartContractInfo();
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        Set<String> alreadyChecked = new HashSet<>();
        for (SmartContractDeployToken token : tokenList) {
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，所有证明需使用相同的ChannelID");
            }
            if (smartContract.equals(token.getSmartContractInfo()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级时，所有证明需使用相同的智能合约");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，所有证明需使用相同的智能合约");
            }
            if (alreadyChecked.contains(token.getSignerIdentityKey().getIdentityKey().getValue())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContract,合约安装或升级时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，存在重复的证明组织");
            } else {
                alreadyChecked.add(token.getSignerIdentityKey().getIdentityKey().getValue());
            }

            Member orgMember = orgMemberMap.get(token.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeploySmartContract,参数错误,token={}", token);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //todo 验签
            if (!ledgerMgr.verifySign(token.clone(), orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkDeploySmartContractList,参数错误,token={}", JSONObject.toJSON(token));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        //检查合约安装升级策略
        int approvalCnt = tokenList.size();
        long size = orgMemberMap.size();
        switch (channel.getSmartContractDeplyStrategy()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持安装升级合约的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持安装升级合约的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持安装升级合约的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持安装升级合约的需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持安装升级合约");
        }
//        //校验授权应用,2.0.8版本将业务合约权限使用注解来实现了
//        if (!CollectionUtils.isEmpty(smartContract.getAuthorizedMember())) {
//            for (String publicKey : smartContract.getAuthorizedMember()) {
//                if (!channel.getMembers().parallelStream().anyMatch(p -> p.getPublicKey().equals(publicKey))) {
//                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该合约的授权应用还未加入到通道中");
//                }
//            }
//        }
//        //校验授权角色
//        if (!CollectionUtils.isEmpty(smartContract.getAuthorizedRole())) {
//            for (String roleId : smartContract.getAuthorizedRole()) {
//                if (!channel.getRoles().parallelStream().anyMatch(p -> p.getRoleId().equals(roleId))) {
//                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该合约的授权角色还未加入到通道中");
//                }
//            }
//        }
    }

    /**
     * @param smartContract
     * @return
     */
    @ApiOperation("获取智能合约命名空间")
    //@PostMapping(value = "/getSmartContractNameSpace")
    private String getSmartContractNameSpace(@RequestBody SmartContract smartContract) {
        String name = "";
/*        if (smartContract.getState().equals(SmartContractOperationTypeEnum.SMARTCONTRACT_UPGRADE)) {
            //判断内存里是否有，有就它有
        }*/
        try {
            Class<?> clazz = new SmartContractClassLoader().defineClass(smartContract);
            if (clazz != null) {
                name = clazz.getName();
            }
            //DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            //beanFactory.removeBeanDefinition(clazz.getName());
        } catch (Exception e) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "getSmartContractNameSpace error={}", e);
        }
        return name;
    }

    /**
     * 校验移除节点参数
     * @param tokenList
     * @return
     */
    private Channel checkRemovePeerParam(List<RemovePeerFromChannelToken> tokenList) {
        //0.投票不为空
        if (tokenList.isEmpty()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,Token list is null");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Token list is null");
        }
        //1.校验参数，一部分参数通过注解进行校验
        if (StringUtils.isEmpty(tokenList.get(0).getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,channel id is null");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "通道id不能为空");
        }
        //2.通道存在
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //3.移除节点时，节点必须已经存在于通道中，而且解冻没有退出（冻结也可以移除）
        IdentityKey removePeer = tokenList.get(0).getPeerId();
        Peer operatePeer = getOperatePeer(channel.getMemberPeerList(), removePeer);
        if (operatePeer == null
                || operatePeer.isExitChannel()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,移除节点时，节点必须已经加入而且尚未退出通道");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，节点必须已经加入而且尚未退出通道");
        }
        //4.节点从通道移除后，必须保证通道中至少有一个正常节点
        long normalPeerCount = channel.getMemberPeerList().stream().filter(peer -> (peer.isState() && !peer.getPeerId().equals(removePeer))).count();
        if (normalPeerCount < 1) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "There must be at least one peer in the channel");
        }
        //5.将通道中所有节点所关联的组织进行解析并放在map中
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }

        //6进行token基本的验证
        Set<IdentityKey> alreadyCheckedSet = new HashSet<>();
        for (RemovePeerFromChannelToken token : tokenList) {
            //6.1 所有的投票必须有相同的channelId
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,Not all token has the same channelId");
                throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST, "Not all token has the same channelId");
            }
            //6.2所有的投票必须是对同一个节点
            if (removePeer.equals(token.getPeerId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,Not all token points to the same peer");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Not all token points to the same peer");
            }
            //6.3所有投票来自于不同的节点
            if (alreadyCheckedSet.contains(token.getSignerIdentityKey().getIdentityKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,移除节点时，每个节点只能投一票");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，每个节点只能投一票");
            } else {
                alreadyCheckedSet.add(token.getSignerIdentityKey().getIdentityKey());
            }
            //7基本权限检查。
            Member orgMember = orgMemberMap.get(token.getSignerIdentityKey().getIdentityKey().getValue());
            //7.1判断调用者是不是通道内的成员
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkRemovePeerParam,参数错误,token={}", token);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //7.2验签
            if (!ledgerMgr.verifySign(token.clone(), orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkRemovePeerParam,参数错误,token={}", JSONObject.toJSON(token));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }

        //8.检查移除节点策略
        int approvalCnt = tokenList.size();
        long size = orgMemberMap.size();
        switch (channel.getPeerAddStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点的需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持移除节点");
        }

        return channel;
    }

    private Channel checkExitOnePeerFromChannel(List<RemovePeerFromChannelToken> tokenList) {
        //0.参数校验，用了注解进行实现
        //1.投票不为空
        if (tokenList.isEmpty()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkExitMySelfFromChannel,Token list is null");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Token list is null");
        }
        //2.通道存在
        Channel channel = ledgerMgr.queryChannel(tokenList.get(0).getChannelId());
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkExitMySelfFromChannel,{}", NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //3.检查token
        IdentityKey removePeer = tokenList.get(0).getPeerId();
        Set<IdentityKey> alreadyCheckedSet = new HashSet<>();
        for (RemovePeerFromChannelToken token : tokenList) {
            //3.1所有投票必须来自于同一通道
            if (token.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，所有证明需使用相同的ChannelID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，所有证明需使用相同的ChannelID");
            }
            //3.2所有投票添加的是同一个节点
            if (token.getPeerId().equals(removePeer) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，所有证明需使用相同的新节点ID");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，所有证明需使用相同的节点ID");
            }
            //3.3所有投票不存在相同的组织
            if (alreadyCheckedSet.contains(token.getSignerIdentityKey().getIdentityKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,添加新节点时，存在重复的证明组织");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，存在重复的证明组织");
            } else {
                alreadyCheckedSet.add(token.getSignerIdentityKey().getIdentityKey());
            }
        }
        //4.移除节点时，节点必须已经存在于通道中，而且解冻没有退出（冻结也可以移除）
        Peer operatePeer = getOperatePeer(channel.getMemberPeerList(), removePeer);
        if (operatePeer == null
                || operatePeer.isExitChannel()) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,移除节点时，节点必须已经加入而且尚未退出通道");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点时，节点必须已经加入而且尚未退出通道");
        }
        //5.移除节点之后，通道中必须存在至少一个正常的节点
        long normalPeerCount = channel.getMemberPeerList().stream().filter(peer -> (peer.isState() && !peer.getPeerId().equals(removePeer))).count();
        if (normalPeerCount < 1) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddPeerParam,移除节点后，通道中至少应该存在一个节点");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "移除节点后，通道中至少应该存在一个节点");
        }
        return channel;
    }
    /*    *//**
     * 部署业务的智能合约
     *//*
    @NewSpiralPolicy(role = "channelAdmin", rule = "2|3")
    public void depolySmartContract(List<SmartContractDeplyApproval> smartContractDeplyApprovalList)
    {
        Channel channel = checkDepolySmartContract(smartContractDeplyApprovalList);
        channel.getSmartContractList().add(smartContractDeplyApprovalList.get(0).getNewSmartContract());
        ledgerMgr.updateChannel(channel);
    }*/

// *************************************************start********************************************************************************


    /**
     * 查询通道角色列表信息
     *
     * @param queryRoleListParams
     * @return
     * @author wuhuaijiang
     */
    //@AuthRoleToken(roleShortName = {"organization", "application"})
    @ApiOperation("查询通道角色列表信息")
    //@PostMapping(value = "/getRoleList")
    public List<Role> getRoleList(@RequestBody QueryRoleListParams queryRoleListParams) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getRoleList.start");
        //TODO 检查参数等  主要是对channelId进行校验
        checkGetRoleList(queryRoleListParams);
        //return memberLedgerMgr.getRoleList(queryRoleApprovals.get(0).getFormRole().getChannelId());
        return memberLedgerMgr.getRoleList(queryRoleListParams);
    }

    /**
     * 获取角色检查,未通过直接抛异常
     *
     * @param queryRoleListParams
     * @return
     */
    private void checkGetRoleList(QueryRoleListParams queryRoleListParams) {
        //1判断channelId不为空
        if (StringUtils.isEmpty(queryRoleListParams.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkGetRoleList,通道Id{}为空", queryRoleListParams.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "角色查询时，通道Id为空");
        }
        //2判断通道是否存在
        if (null == ledgerMgr.getChannel(queryRoleListParams.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkGetRoleList,通道不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "角色查询时，该通道不存在");
        }
    }

    /**
     * 查询通道角色信息
     *
     * @param queryRoleParams
     * @return
     * @author wuhuaijiang
     */
    //@AuthRoleToken(roleShortName = {"organization", "application","maintenance"})
    @ApiOperation("查询通道角色信息")
    //@PostMapping(value = "/getRole")
    public Role getRole(@RequestBody QueryRoleParams queryRoleParams) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getRoleList.start");
        //TODO 检查参数等
        checkGetRole(queryRoleParams);
        //return memberLedgerMgr.getRoleList(queryRoleApprovals.get(0).getFormRole().getChannelId());
        return memberLedgerMgr.getRole(queryRoleParams);
    }

    /**
     * 获取角色检查,未通过直接抛异常
     *
     * @param queryRoleParams
     * @return
     */
    private void checkGetRole(QueryRoleParams queryRoleParams) {
        //1判断channelId是否存在
        if (StringUtils.isEmpty(queryRoleParams.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkGetRole,通道Id{}为空", queryRoleParams.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "角色查询时，通道Id为空");
        }
        //2判断通道是否存在
        if (null == ledgerMgr.queryChannel(queryRoleParams.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkGetRole,通道不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "角色查询时，该通道不存在");
        }
        //3判断其他参数是否正常
        //如果其他三个参数均为空，抛出参数异常
        if (StringUtils.isEmpty(queryRoleParams.getRoleId())
                && StringUtils.isEmpty(queryRoleParams.getName())
                && StringUtils.isEmpty(queryRoleParams.getShortName())) {
            log.error(ModuleClassification.LedM_MLI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "角色全局ID、名称、英文名称必须至少有一个不为空");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
    }


    /**
     * 通道自定义角色创建
     *
     * @param roleAddApprovals
     * @author wuhuaijiang
     */
    @ApiOperation("通道自定义角色创建")
    //@PostMapping(value = "/addCustomRole")
    public void addCustomRole(@RequestBody List<RoleAddApproval> roleAddApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.addCustomRole.start");
        try {
            //校验参数
            Role addRole = checkAddCustomRole(roleAddApprovals);
            log.info(ModuleClassification.TxM_SSC_ + "getChannel in addCustomRole");
            //拿到通道
            Channel channel = ledgerMgr.queryChannel(addRole.getChannelId());
            //TODO 角色新增业务逻辑
            List<Role> roles = new ArrayList<>();
            boolean existFlag = false;
            for (Role role : channel.getRoles()) {
                //roles.add(role.clone());
                if (role.getRoleId().equals(addRole.getRoleId())) {
                    existFlag = true;
                }
            }
            if (!existFlag) {
                roles.add(addRole);
            }
            channel.setRoles(roles);
            ChannelChange<Role> channelChange = new ChannelChange<>();
            channelChange.setActionTag(PersistConstant.PersistTarget.roleAdd);
            channelChange.setActionData(addRole);
            channel.setChannelChange(channelChange);
            ledgerMgr.updateChannel(channel);
        } catch (Exception e) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.addCustomRole,e={}", e);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "通道新增自定义角色时创建错误");
        }
    }

    /**
     * 新增通道自定义角色校验，未通过直接抛异常
     *
     * @param roleAddApprovals
     * @return
     * @author wuhuaijiang
     */
    private Role checkAddCustomRole(List<RoleAddApproval> roleAddApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddCustomRole.start");
        //校验传入参数列表是否为空
        if (CollectionUtils.isEmpty(roleAddApprovals)) {
            log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + "新增自定义角色入参请求列表为空");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色新增时，入参请求列表为空");
        }
        //拿到一个addrole角色对象
        RoleAdd form = roleAddApprovals.get(0).getRoleAdd().clone();
        //判断角色参数是否为空.channelID,name,shortname必须都传
        if (null == form
                || StringUtils.isEmpty(form.getChannelId())
                || StringUtils.isEmpty(form.getName())
                || StringUtils.isEmpty(form.getShortName())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddCustomRole,role参数错误,form={}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色新增时，参数错误");
        }
        //校验name和shortname的长度
        this.checkLength(form.getName(), form.getShortName(), form.getRemark());
        //校验权限组是否存在
        List<Integer> authIds = roleAddApprovals.get(0).getRoleAdd().getAuthIds();
        List<Integer> authIdList = memberLedgerMgr.getAuthIds();
        //校验权限入参
        this.checkAuth(authIds, authIdList);
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkAddCustomRole");
        //拿到通道
        Channel channel = ledgerMgr.queryChannel(form.getChannelId());
        //判断通道是否存在,在数据库中查询
        if (null == channel) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkAddCustomRole,role的通道参数错误{}", form.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //根据通道id和name以及shortname进行查询，确认该通道中不存在重复的name和shortName角色
        List<Role> existRole = memberLedgerMgr.getRoleByChannelIdandParams(form.getChannelId(), form.getName(), form.getShortName());
        //如果该角色存在而且state不是2，则报错
        if (!CollectionUtils.isEmpty(existRole)) {
            for (Role erole : existRole) {
                //如果erole的state不是删除，则错误
                if (erole.getState() != 2) {
                    log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddCustomRole,已该名称命名的角色已在通道中存在");
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该名称命名的角色在通道中已经存在");
                }
            }
        }
        //TODO 基础的证明检查，包括检查证明是不是在一个通道中，身份有没有重复
        checkApprovalList(roleAddApprovals, channel);
        //TODO 是否满足添加策略
        checkAddCustomRoleStrategyApprovalList(roleAddApprovals, channel);
        //将入参放入到role中,并将roleflag设置为2,业务状态默认设置为1
        Role role = form.toRole();
        return role;
    }

    private void checkLength(String name, String shortName, String remark) {
        if (!StringUtils.isEmpty(name)) {
            if (name.length() > 256) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "名称长度超限");
            }
        }
        if (!StringUtils.isEmpty(shortName)) {
            if (shortName.length() > 128) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "英文名称超限");
            }
        }
        if (!StringUtils.isEmpty(remark)) {
            if (remark.length() > 1024) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "说明超限");
            }
        }
    }

    /**
     * 校验权限是否在权限表中
     *
     * @param auths
     * @param authIdList
     */
    private void checkAuth(List<Integer> auths, List<Integer> authIdList) {
        if (CollectionUtils.isEmpty(auths)) {
            return;
        }
        if (CollectionUtils.isEmpty(authIdList)) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkAddCustomRole,权限入参错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        Boolean flag = false;
        try {
            for (int i = 0; i < auths.size(); i++) {
                //对于每一入参都和数据库中的权限表进行比较，如果匹配到一次，则更改flag
                for (int j = 0; j < authIdList.size(); j++) {
                    if (auths.get(i).equals(authIdList.get(j))) {
                        flag = true;
                        break;
                    }
                }
                //如果一个入参，遍历完毕都没有匹配到，则校验错误
                if (!flag) {
                    log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkAddCustomRole,权限入参错误");
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
                }
                flag = false;
            }
        } catch (NewspiralException e) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkAddCustomRole,权限入参错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
    }


    /**
     * 通道自定义角色修改
     *
     * @param roleUpdateApprovals
     * @author wuhuaijiang
     */
    @ApiOperation("通道自定义角色修改")
    //@PostMapping(value = "/updateCustomRole")
    public void updateCustomRole(@RequestBody List<RoleUpdateApproval> roleUpdateApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.updateCustomRole.start");
        try {
            //TODO 检查参数等
            Role updateRole = checkUpdateCustomRole(roleUpdateApprovals);
            Channel channel = ledgerMgr.queryChannel(updateRole.getChannelId());
            List<Role> roles = new ArrayList<>();
            roles.add(updateRole);
            /*for (int i = 0; i < channel.getRoles().size(); i++) {
                Role role = channel.getRoles().get(i);
                if (!updateRole.getRoleId().equals(role.getRoleId())) {
                    roles.add(role.clone());
                }
            }*/
            channel.setRoles(roles);
            ChannelChange<Role> channelChange = new ChannelChange<>();
            channelChange.setActionData(updateRole);
            channelChange.setActionTag(PersistConstant.PersistTarget.roleModify);
            channel.setChannelChange(channelChange);
            ledgerMgr.updateChannel(channel);
        } catch (Exception e) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.updateCustomRole,e={}", e);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "通道修改自定义角色时修改错误");
        }
    }

    /**
     * 修改自定义角色检查,未通过直接抛异常
     *
     * @param roleUpdateApprovals
     * @return
     */
    private Role checkUpdateCustomRole(List<RoleUpdateApproval> roleUpdateApprovals) {
        //1校验入参列表是否为空
        if (CollectionUtils.isEmpty(roleUpdateApprovals)) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkupdateCustomRole,修改自定义角色入参列表为空");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，入参列表为空");
        }
        //2拿到updateRole，并判断参数是否符合校验规则
        RoleUpdate form = roleUpdateApprovals.get(0).getUpdateRole().clone();
        checkLength(form.getName(), form.getShortName(), form.getRemark());
        //3检查需要修改的角色是否为空
        if (null == form) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkupdateCustomRole,role为null");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，role为null");
        }
        //4校验修改参数是否正确,必须输入channelid和roleid
        if (StringUtils.isEmpty(form.getChannelId()) || StringUtils.isEmpty(form.getRoleId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkupdateCustomRole,role参数错误{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，参数错误");
        }
        //5校验通道是否存在
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkAddCustomRole");
        Channel channel = ledgerMgr.queryChannel(roleUpdateApprovals.get(0).getChannelId());
        if (null == channel) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkUpdateCustomRole,role的通道参数错误{}", form.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //校验权限组是否存在
        List<Integer> authIds = roleUpdateApprovals.get(0).getUpdateRole().getAuthIds();
        List<Integer> authIdList = memberLedgerMgr.getAuthIds();
        //遍历入参权限列表
        this.checkAuth(authIds, authIdList);
        //6根据channelId和roleId查询是否存在该角色
        Role existRole = memberLedgerMgr.getRole(form.getRoleId());
        //需要修改的角色不存在
        if (null == existRole) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateCustomRole,role不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，参数错误");
        }
        //7对于要修改的角色，其roleflag必须为2
        if (existRole.getRoleFlag() != 2) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkupdateCustomRole,role的类型参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "不是自定义角色，无法修改");
        }
        //8校验需要删除的自定义角色所在的通道应该和存在该角色的通道是同一个通道
        if (!existRole.getChannelId().equals(form.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkupdateCustomRole,role的通道参数错误,{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，通道参数错误");
        }
        //9校验修改过后的short，name和其他不会重复
        List<Role> existRoles = memberLedgerMgr.getRoleByChannelIdandParams(form.getChannelId(), form.getName(), form.getShortName());
        //9.1应该注意，如果是本条记录则应该放行
        if (!CollectionUtils.isEmpty(existRoles)) {
            for (int i = 0; i < existRoles.size(); i++) {
                Role erole = existRoles.get(i);
                //如果存在的角色不是删除状态，则要检查是否是唯一的
                if (erole.getState() != 2) {
                    if (!((Role) existRoles.get(i)).getRoleId().equals(form.getRoleId())) {
                        log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateCustomRole,自定义角色修改时，名称和英文名称和已有名称重复");
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色修改时，名称和英文名称和已有名称重复");
                    }
                }
            }
        }
        //TODO 主要是检查是否来同一个通道，以及是否存在相同的组织
        checkApprovalList(roleUpdateApprovals, channel);
        //TODO 检查修改通道自定义角色策略是否满足要求
        checkUpdateCustomRoleStrategyApprovalList(roleUpdateApprovals, channel);
        //将入参转化到role中
        Role role = form.toRole();
        return role;
    }

    /**
     * 通道自定义角色删除
     */
    @ApiOperation("通道自定义角色删除")
    //@PostMapping(value = "/deleteCustomRole")
    public void deleteCustomRole(@RequestBody List<RoleDeleteApproval> roleDeleteApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.deleteChannelCustomRole.start");
        //TODO 检查参数等
        Role deleteRole = checkDeleteCustomRole(roleDeleteApprovals);
        // TODO 业务逻辑处理
        Channel channel = ledgerMgr.queryChannel(roleDeleteApprovals.get(0).getChannelId());
        List<Role> roles = new ArrayList<>();
        roles.add(deleteRole);
        /*for (int i = 0; i < channel.getRoles().size(); i++) {
            Role role = channel.getRoles().get(i);
            if (!deleteRole.getRoleId().equals(role.getRoleId())) {
                roles.add(role.clone());
            }
        }*/
        channel.setRoles(roles);
        ChannelChange<Role> channelChange = new ChannelChange<>();
        channelChange.setActionData(deleteRole);
        channelChange.setActionTag(PersistConstant.PersistTarget.roleModify);
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }

    private Role checkDeleteCustomRole(List<RoleDeleteApproval> roleDeleteApprovals) {
        //自定义角色修改时角色的参数基本检查
        //1.校验角色列表修改参数不为空
        if (CollectionUtils.isEmpty(roleDeleteApprovals)) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkChannelDeleteCustomRole,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色删除时，删除角色列表参数为空");
        }
        //2校验角色
        RoleDelete form = roleDeleteApprovals.get(0).getDeleteRole();
        if (null == form) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole,role为null");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色删除时，role为null");
        }
        //3校验输入参数
        if (StringUtils.isEmpty(form.getRoleId()) || StringUtils.isEmpty(form.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole,role参数错误{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色删除时，参数错误");
        }
        //4校验通道是否存在
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkAddCustomRole");
        Channel channel = ledgerMgr.queryChannel(roleDeleteApprovals.get(0).getChannelId());
        if (null == channel) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkDeleteCustomRole,role的通道参数错误{}", form.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        //5.校验需要删除的自定义角色必须存在
        Role existRole = memberLedgerMgr.getRole(form.getRoleId());
        if (null == existRole) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole,role不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色删除时，角色不存在");
        }
        //6如果state已经是2，则不需要删除
        if (existRole.getState() == 2) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色已经删除，无法重复删除");
        }
        //7验证角色的类型必须为自定义类型
        if (existRole.getRoleFlag() != 2) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole,role的类型参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "不是自定义角色，无法删除");
        }
        //8.校验需要删除的自定义角色所在的通道应该和存在该角色的通道是同一个通道
        if (!existRole.getChannelId().equals(form.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteCustomRole,role的通道参数错误,{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "自定义角色删除时，通道参数错误");
        }
        //9将入参转化到role中
        Role role = new Role();
        BeanUtils.copyProperties(form, role);
        //10逻辑删除定义
        role.setState(2);
        //11.基础的证明检查，包括检查证明是不是在一个通道中，身份有没有重复
        checkApprovalList(roleDeleteApprovals, channel);
        //12是否满足修改策略检查
        checkDeleteCustomRoleStrategyApprovalList(roleDeleteApprovals, channel);
        return role;
    }

    /**
     * 通道角色策略检查
     * 校验角色加入策略是否满足
     *
     * @param roleAddApprovals
     * @param channel
     */
    private void checkAddCustomRoleStrategyApprovalList(List<RoleAddApproval> roleAddApprovals, Channel channel) {
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (RoleAddApproval approval : roleAddApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //todo 验签
            if (!ledgerMgr.verifySign(approval, orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,参数错误,approval={}", JSONObject.toJSON(approval));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        //总的请求数
        int approvalCnt = roleAddApprovals.size();
        //所有组织证书,对于多个节点，如果是同一个组织创建的，则算一个
        long size = orgMemberMap.size();
        switch (channel.getRoleAddStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (2.0 / 3.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (1.0 / 2.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持角色加入");
        }
    }

    /**
     * 通道角色策略检查
     * 校验角色修改策略是否满足
     *
     * @param roleUpdateApprovals
     * @param channel
     */
    private void checkUpdateCustomRoleStrategyApprovalList(List<RoleUpdateApproval> roleUpdateApprovals, Channel channel) {
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (RoleUpdateApproval approval : roleUpdateApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //todo 验签
            if (!ledgerMgr.verifySign(approval, orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,参数错误,approval={}", JSONObject.toJSON(approval));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        //检查角色加入通道策略
        int approvalCnt = roleUpdateApprovals.size();  //总的请求数
        long size = orgMemberMap.size();  //组织证书数量
        switch (channel.getRoleAddStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (2.0 / 3.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改角色的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改角色的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (1.0 / 2.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改角色的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改角色需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持角色修改");
        }
    }


    /**
     * 通道角色策略检查
     * 校验角色删除策略是否满足
     *
     * @param roleDeleteApprovals
     * @param channel
     */
    private void checkDeleteCustomRoleStrategyApprovalList(List<RoleDeleteApproval> roleDeleteApprovals, Channel channel) {
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (RoleDeleteApproval approval : roleDeleteApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //todo 验签
            if (!ledgerMgr.verifySign(approval, orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,参数错误,approval={}", JSONObject.toJSON(approval));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        //检查角色加入通道策略
        int approvalCnt = roleDeleteApprovals.size();  //总的请求数
        long size = orgMemberMap.size();  //所有拥有在线证书的节点数、
        switch (channel.getRoleDelStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (2.0 / 3.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if ((approvalCnt + 0.0) / size < (1.0 / 2.0)) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色需要通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持角色删除");
        }
    }


    //********************************************end********************************************************************


    /**
     * 新增一个成员
     *
     * @param memberAddApprovals
     */
    @ApiOperation("通道自定义成员添加")
    //@PostMapping(value = "/addMember")
    public void addMember(@RequestBody List<MemberAddApproval> memberAddApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.addMember.start");
        //TODO 检查参数等
        Channel channel = ledgerMgr.queryChannel(memberAddApprovals.get(0).getChannelId());
        Member newMember = checkAddMember(memberAddApprovals, channel);
        List<Member> members = new ArrayList<>();
        boolean existFlag=false;
        for (Member member : channel.getMembers()) {
            if ((member.getChannelId() + member.getPublicKey()).equals(newMember.getChannelId() + newMember.getPublicKey())) {
                if (member.getStatus() == 3) {
                    log.info(ModuleClassification.TxM_SSC_ + ",SystemSmartContract.addMember,member is exist,do update member action");
                    existFlag=true;
                    break;
                } else {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "成员新增时，成员已经存在");
                }
            }
        }
        members.add(newMember);
        channel.setMembers(members);
        ChannelChange<Member> channelChange = new ChannelChange<>();
        channelChange.setActionTag(existFlag?PersistConstant.PersistTarget.memberModify:PersistConstant.PersistTarget.memberAdd);
        channelChange.setActionData(newMember);
        channel.setChannelChange(channelChange);
        ledgerMgr.updateChannel(channel);
    }


    /**
     * 修改一个成员
     *
     * @param memberUpdateApprovals
     */
    @ApiOperation("通道自定义成员修改")
    //@PostMapping(value = "/updateMember")
    public void updateMember(@RequestBody List<MemberUpdateApproval> memberUpdateApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.memberUpdateInfo.start");
        //TODO 检查参数等
        try {
            Channel channel = ledgerMgr.getChannel(memberUpdateApprovals.get(0).getChannelId());
            Member updateMember = checkUpdateMember(memberUpdateApprovals, channel);
            List<Member> members = new ArrayList<>();
            members.add(updateMember);
            /*for (int i = 0; i < channel.getMembers().size(); i++) {
                Member member = channel.getMembers().get(i);
                if (!(updateMember.getChannelId() + updateMember.getPublicKey())
                        .equals(member.getChannelId() + member.getPublicKey())) {
                    members.add(member.clone());
                }
            }*/
            channel.setMembers(members);
            ChannelChange<Member> channelChange = new ChannelChange<>();
            channelChange.setActionTag(PersistConstant.PersistTarget.memberModify);
            channelChange.setActionData(updateMember);
            channel.setChannelChange(channelChange);
            ledgerMgr.updateChannel(channel);
        } catch (Exception e) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.memberUpdateInfo,e={}", e);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "修改成员时，处理错误");
        }

    }


    @ApiOperation("通道自定义成员删除")
    //@PostMapping(value = "/deleteMember")
    public void deleteMember(@RequestBody List<MemberDeleteApproval> memberDeleteApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.deleteMember.start");
        try {
            Channel channel = ledgerMgr.getChannel(memberDeleteApprovals.get(0).getChannelId());
            Member deletMember = checkDeleteMember(memberDeleteApprovals, channel);
            List<Member> members = new ArrayList<>();
            members.add(deletMember);
            /*for (int i = 0; i < channel.getMembers().size(); i++) {
                Member member = channel.getMembers().get(i);
                if (!(deletMember.getChannelId() + deletMember.getPublicKey())
                        .equals(member.getChannelId() + member.getPublicKey())) {
                    members.add(member.clone());
                }
            }*/
            channel.setMembers(members);
            ChannelChange<Member> channelChange = new ChannelChange<>();
            channelChange.setActionTag(PersistConstant.PersistTarget.memberModify);
            channelChange.setActionData(deletMember);
            channel.setChannelChange(channelChange);
            ledgerMgr.updateChannel(channel);
        } catch (Exception e) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.deleteMember,e={}", e);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "删除成员时，处理错误");
        }
    }

    /**
     * 查询单个成员信息
     *
     * @param queryMemberReq
     * @return
     */
    @ApiOperation("查询通道成员信息")
    //@PostMapping(value = "/getMember")
    public Member getMember(@RequestBody QueryMemberReq queryMemberReq) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getMember.start");
        //TODO 检查参数等
        checkGetMember(queryMemberReq);

        Member member = new Member();
        member.setPublicKey(queryMemberReq.getPublicKey());
        member.setChannelId(queryMemberReq.getChannelId());
        return memberLedgerMgr.getMember(member);
    }

    /**
     * 查询成员列表信息
     *
     * @param queryMemberListReq
     * @return
     */
    @ApiOperation("查询通道成员列表信息")
    //@PostMapping(value = "/getMemberList")
    public List<Member> getMemberList(@RequestBody QueryMemberListReq queryMemberListReq) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getMemberList.start");
        //TODO 检查参数等
        checkGetMemberList(queryMemberListReq);
        return memberLedgerMgr.getMemberList(queryMemberListReq);
    }

    /**
     * 修改节点的证书，测评功能，当前业务场景需要重新考虑
     *
     * @param peerCertificateUpdateApprovals
     */
    @ApiOperation("修改节点证书")
    //@PostMapping(value = "/updatePeerCertificate")
    public void updatePeerCertificate(@RequestBody List<PeerCertificateUpdateApproval> peerCertificateUpdateApprovals) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.updatePeerCertificate.start");
        //TODO 检查参数等
        checkUpdatePeerCertificate(peerCertificateUpdateApprovals);
        //ledgerMgr.updatePeerCertificateByPrimaryKey(peerCertificateUpdateApprovals.get(0).getUpdatePeerCertificate(), peerCertificateUpdateApprovals.get(0).getChannelId());
        //ledgerMgr.updateChannel(ledgerMgr.queryChannel(peerCertificateUpdateApprovals.get(0).getChannelId()));
        PeerCertificate peerCertificate = getPeerCertificate(peerCertificateUpdateApprovals.get(0).getUpdatePeerCertificate());
        Channel channel = ledgerMgr.queryChannel(peerCertificateUpdateApprovals.get(0).getUpdatePeerCertificate().getChannelId());
        for (Peer peer : channel.getMemberPeerList()) {
            if (peer.getPeerId().equals(peerCertificate.getPeer().getPeerId())) {
                //peer=peerCertificate.getPeer();
                peer.setCertificateKeyStoreFile(peerCertificate.getPeer().getCertificateKeyStoreFile());
                peer.setCertificateCerFile(peerCertificate.getPeer().getCertificateCerFile());
                peer.setPeerCert(peerCertificate.getPeerCert());
                //新增修改节点标签哈
                ChannelChange<Peer> channelChange = new ChannelChange<>();
                channelChange.setActionTag(PersistConstant.PersistTarget.peerModify);
                channelChange.setActionData(peer);
                channel.setChannelChange(channelChange);
                break;
            }
        }
        ledgerMgr.updateChannel(channel);
        //throw new NewspiralException(NewSpiralErrorEnum.UN_IMPLEMENTED);
    }


    private PeerCertificate getPeerCertificate(PeerCertificateUpdateParams updatePeerCertificate) {
        PeerCertificate peerCertificate = ledgerMgr.getPeerCertificate(updatePeerCertificate.getPeerId(), updatePeerCertificate.getChannelId());
        peerCertificate.getPeer().setCertificateCerFile(updatePeerCertificate.getCertificateCerFile());
        peerCertificate.getPeer().setCertificateKeyStoreFile(updatePeerCertificate.getCertificateKeyStoreFile());
        //peerCertificate.getPeer().setCertificateAlias("test1");
        for (PeerCert peerCert : peerCertificate.getPeerCert()) {
            //全部吊销
            if (!"2".equals(peerCert.getFlag())) {
                peerCert.setFlag("2");
                peerCert.setBlockHeight(null);
            }
        }
        //新增一条有效证书状态
        PeerCert peerCert = new PeerCert();
        peerCert.setPeerId(updatePeerCertificate.getPeerId().getValue());
        peerCert.setFlag("0");//正常状态
        peerCert.setCertificateCerFile(updatePeerCertificate.getCertificateCerFile());
        peerCert.setCertificateHash(ledgerMgr.getHash(updatePeerCertificate.getCertificateCerFile()));
        peerCert.setCreateTime(System.currentTimeMillis());
        peerCert.setBlockHeight(null);
        peerCert.setChannelId(updatePeerCertificate.getChannelId());
        if (peerCertificate.getPeer().getIsLocalPeer()) {
            peerCert.setIsLocalPeer(1);
        } else {
            peerCert.setIsLocalPeer(0);
        }
        peerCert.setCertificateKeyStoreFile(updatePeerCertificate.getCertificateKeyStoreFile());
        peerCertificate.getPeerCert().add(peerCert);
        return peerCertificate;
    }


    /**
     * 修改成员的证书检查，未通过直接抛异常
     *
     * @param peerCertificateUpdateApprovals
     * @return
     */
    private void checkUpdatePeerCertificate(List<PeerCertificateUpdateApproval> peerCertificateUpdateApprovals) {
        //新增的角色的参数基本检查
        if (CollectionUtils.isEmpty(peerCertificateUpdateApprovals)) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificate,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点证书修改时，参数为空");
        }
        PeerCertificateUpdateParams form = peerCertificateUpdateApprovals.get(0).getUpdatePeerCertificate();
        if (null == form
                || StringUtils.isEmpty(form.getPeerId())
                || StringUtils.isEmpty(form.getCertificateCerFile())
                || StringUtils.isEmpty(form.getCertificateKeyStoreFile())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificate,peer参数错误{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "节点证书修改时，参数错误");
        }
        //TODO 校验证书是否重复 通过证书hash
        if (ledgerMgr.verifyPeerCertificateRepeat(form.getCertificateCerFile(), form.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdatePeerCertificate,peer参数错误{}", form);
            throw new NewspiralException(NewSpiralErrorEnum.PEER_CERTIFICATE_REPEAT, "节点证书修改时，节点证书重复");
        }
        Channel channel = ledgerMgr.queryChannel(form.getChannelId());
        //checkApprovalList(peerCertificateUpdateApprovals, channel);
    }


    /**
     * 新增成员检查，未通过直接抛异常
     *
     * @param memberAddApprovals
     * @return
     */
    private Member checkAddMember(List<MemberAddApproval> memberAddApprovals, Channel channel) {
        //新增的成员的参数基本检查
        if (CollectionUtils.isEmpty(memberAddApprovals)) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "成员新增时，参数为空");
        }
        Member member = Member.createInstance(memberAddApprovals.get(0).getNewMemberInfo());
        member.setChannelId(memberAddApprovals.get(0).getChannelId());
        if (StringUtils.isEmpty(member.getCertificateCerFile())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,member参数错误{}", member);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "成员新增时，参数错误");
        }
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkAddMember");
        if (null == member.getChannelId() || !member.getChannelId().equals(channel.getChannelId())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "SystemSmartContract.checkAddMember,member的通道参数错误{}", member.getChannelId());
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        List<Role> roleList = new ArrayList<>();
        for (Role role : member.getRoles()) {
            Role roleInDB = memberLedgerMgr.getRole(role.getRoleId());
            if (null == roleInDB) {
                log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,member的角色参数错误{}", member.getRoles());
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
            }
            roleList.add(roleInDB);
        }
        member.setRoles(roleList);
        member.setStatus(0);
        //解析一下成员的其他信息，名称，公钥，issuid等
        if (!ledgerMgr.processMemberCertificate(member)) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "成员新增时，解析成员证书错误");
        }
        //Channel channel = ledgerMgr.queryChannel(memberAddApprovals.get(0).getChannelId());
        checkOrgApprovalList(memberAddApprovals, channel);
        checkAddMemberApprovalList(memberAddApprovals, channel, member);
        return member;
    }

    /**
     * @param memberAddApprovals
     * @param channel
     * @param newMember
     */
    private void checkAddMemberApprovalList(List<MemberAddApproval> memberAddApprovals, Channel channel, Member newMember) {

        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (MemberAddApproval approval : memberAddApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //TODO 已经调整,证书间的包含关系
            if (newMember.getIssuerId().contains(orgMember.getName())) {
                isFather = true;
            }
            //todo 验签
            if (!ledgerMgr.verifySign(approval, orgMember.getPublicKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMember,参数错误,approval={}", JSONObject.toJSON(approval));
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }

        //检查成员加入策略
        //投票数
        int approvalCnt = memberAddApprovals.size();
        //组织数
        long size = orgMemberMap.size();
        switch (channel.getMemberAddChannelStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增成员的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增成员的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增成员的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增成员的需要通道管理员的证明");
                }
                break;
            case PARENT_AGREE:
                if (!isChannelAdmin && !isFather) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增成员的需要父成员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持成员加入");
        }
        //检查成员角色加入策略
        for (Role role : newMember.getRoles()) {
            switch (role.getMemberAddStrategy()) {
                case ABSOLUTE_MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                    }
                    break;
                case ALL_AGREE:
                    if (approvalCnt != size) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                    }
                    break;
                case MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                    }
                    break;
                case MANAGER_AGREE:
                    if (!isChannelAdmin) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的需要通道管理员的证明");
                    }
                    break;
                case PARENT_AGREE:
                    if (!isChannelAdmin && !isFather) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的需要父成员的证明");
                    }
                    break;
                default:
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持成员角色加入");
            }
        }
    }

    /**
     * 基础证明检查
     *
     * @param elem
     * @param channel
     */
    private void checkApprovalList(List<?> elem, Channel channel) {
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkApprovalList");
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList,channel参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        Set<IdentityKey> identitySet = new HashSet<>();
        BaseApproval approval = new BaseApproval();
        for (Object object : elem) {
            BeanUtils.copyProperties(object, approval);
            if (approval.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "所有证明需使用相同的ChannelID");
            }
            if (identitySet.contains(approval.getSignerIdentityKey().getIdentityKey())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "存在重复的证明组织");
            } else {
                identitySet.add(approval.getSignerIdentityKey().getIdentityKey());
            }
        }
    }

    /**
     * 基础证明检查
     *
     * @param elem
     * @param channel
     */
    private void checkOrgApprovalList(List<?> elem, Channel channel) {
        log.info(ModuleClassification.TxM_SSC_ + "getChannel in checkApprovalList");
        if (channel == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList,channel参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
        Set<String> identityPublickey = new HashSet<>();
        BaseApproval approval = new BaseApproval();
        for (Object object : elem) {
            BeanUtils.copyProperties(object, approval);
            if (approval.getChannelId().equals(channel.getChannelId()) == false) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "所有证明需使用相同的ChannelID");
            }
            if (identityPublickey.contains(approval.getSignerIdentityKey().getIdentityKey().getValue())) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "存在重复的证明组织");
            } else {
                identityPublickey.add(approval.getSignerIdentityKey().getIdentityKey().getValue());
            }
        }
    }

    /**
     * 修改成员检查，未通过直接抛异常
     *
     * @param memberUpdateApprovals
     * @return
     */
    private Member checkUpdateMember(List<MemberUpdateApproval> memberUpdateApprovals, Channel channel) {
        //修改的成员的参数基本检查
        if (CollectionUtils.isEmpty(memberUpdateApprovals)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "修改成员时，参数为空");
        }
        Member firstMember = Member.createInstance(memberUpdateApprovals.get(0).getMemberUpdateInfo());
        firstMember.setChannelId(memberUpdateApprovals.get(0).getChannelId());
        if (null == firstMember
                || StringUtils.isEmpty(firstMember.getChannelId())
                || StringUtils.isEmpty(firstMember.getCertificateCerFile())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member参数错误,firstMember={}", firstMember);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "修改成员时，参数错误");
        }
        //操作状态
        if (null != firstMember.getStatus() && firstMember.getStatus() != MemberStateEnum.VALID.code &&
                firstMember.getStatus() != MemberStateEnum.FROZEN.code) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SystemSmartContract.checkUpdateMember,{}", NewSpiralErrorEnum.INVALID_PARAM);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "修改成员时，状态值错误");
        }

        //TODO 应该重新解析证书，查询公钥是否存在
        //解析一下成员的其他信息，名称，公钥，issuid等
        if (!ledgerMgr.processMemberCertificate(firstMember)) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "成员修改时，解析成员证书错误");
        }
        Member existMember = memberLedgerMgr.getMember(firstMember);
        if (null == existMember) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "修改成员时，参数错误，成员不存在");
        }
        for (MemberUpdateApproval approval : memberUpdateApprovals) {
            String orgPublicKey = approval.getSignerIdentityKey().getIdentityKey().getValue();
            // 验签
            if (!ledgerMgr.verifySign(approval, orgPublicKey)) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        // 验证角色存不存在
        List<Role> roles = new ArrayList<>();
        for (Role role : firstMember.getRoles()) {
            Role roleInDB = memberLedgerMgr.getRole(role.getRoleId());
            if (null == roleInDB) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member的角色参数错误,roles={}", firstMember.getRoles());
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
            }
            roles.add(roleInDB);
        }
        firstMember.setRoles(roles);
        if (firstMember.getStatus() != null) {
            if (existMember.getStatus() == 3) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member的角色参数错误,roles={}", firstMember.getRoles());
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "改成员已经是业务删除，不可以进行修改操作");
            }
        }
        modifyMember(existMember, firstMember);
        //成员无变化，不让操作
        if (existMember.equals(firstMember)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member无变化时非法操作");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "修改成员时，参数错误，成员无变化非法操作");
        }
        checkApprovalList(memberUpdateApprovals, channel);
        checkUpdateMemberApprovalList(memberUpdateApprovals, channel, firstMember, existMember);
        return firstMember;
    }

    private void checkUpdateMemberApprovalList(List<MemberUpdateApproval> memberUpdateApprovals, Channel channel, Member updateMember, Member existMember) {
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (MemberUpdateApproval approval : memberUpdateApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //TODO 已经调整,证书间的包含关系
            if (updateMember.getIssuerId().contains(orgMember.getName())) {
                isFather = true;
            }
        }
        //检查成员修改策略
        //投票数量
        int approvalCnt = memberUpdateApprovals.size();
        long size = orgMemberMap.size();

        //根据状态删除还是修改来判断策略
        if (updateMember.getStatus() == 3) {
            switch (channel.getMemberRemoveChannelStrategyEnum()) {
                case ABSOLUTE_MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修删除成员的证明数量过少");
                    }
                    break;
                case ALL_AGREE:
                    if (approvalCnt != size) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的证明数量过少");
                    }
                    break;
                case MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的证明数量过少");
                    }
                    break;
                case MANAGER_AGREE:
                    if (!isChannelAdmin) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的需要通道管理员的证明");
                    }
                    break;
                case PARENT_AGREE:
                    if (!isChannelAdmin && !isFather) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的需要父成员或通道管理员的证明");
                    }
                    break;
                default:
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持删除角色");
            }

        } else {

            switch (channel.getMemberModifyChannelStrategyEnum()) {
                case ABSOLUTE_MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改成员的证明数量过少");
                    }
                    break;
                case ALL_AGREE:
                    if (approvalCnt != size) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改成员的证明数量过少");
                    }
                    break;
                case MAJORITY_AGREE:
                    if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改成员的证明数量过少");
                    }
                    break;
                case MANAGER_AGREE:
                    if (!isChannelAdmin) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改成员的需要通道管理员的证明");
                    }
                    break;
                case PARENT_AGREE:
                    if (!isChannelAdmin && !isFather) {
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修改成员的需要父成员或通道管理员的证明");
                    }
                    break;
                default:
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持修改成员");
            }

            List<Role> addRoles = new ArrayList<>();
            List<Role> delRoles = new ArrayList<>();
            compareRoleList(updateMember.getRoles(), existMember.getRoles(), addRoles, delRoles);

            for (Role role : delRoles) {
                switch (role.getMemberDelStrategy()) {
                    case ABSOLUTE_MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case ALL_AGREE:
                        if (approvalCnt != size) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case MANAGER_AGREE:
                        if (!isChannelAdmin) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的需要通道管理员的证明");
                        }
                        break;
                    case PARENT_AGREE:
                        if (!isChannelAdmin && !isFather) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的需要父成员的证明");
                        }
                        break;
                    default:
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持删除角色加入");
                }
            }

            for (Role role : addRoles) {
                switch (role.getMemberAddStrategy()) {
                    case ABSOLUTE_MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                        }
                        break;
                    case ALL_AGREE:
                        if (approvalCnt != size) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                        }
                        break;
                    case MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的证明数量过少");
                        }
                        break;
                    case MANAGER_AGREE:
                        if (!isChannelAdmin) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的需要通道管理员的证明");
                        }
                        break;
                    case PARENT_AGREE:
                        if (!isChannelAdmin && !isFather) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新增角色的需要父成员的证明");
                        }
                        break;
                    default:
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持成员角色加入");
                }
            }

        }


/*        //检查成员角色加入策略
        for (Role role : memberUpdateApprovals.get(0).getMemberUpdateInfo().getRoles()) {
                switch (role.getMemberDelStrategy()) {
                    case ABSOLUTE_MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case ALL_AGREE:
                        if (approvalCnt != size) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case MAJORITY_AGREE:
                        if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的证明数量过少");
                        }
                        break;
                    case MANAGER_AGREE:
                        if (!isChannelAdmin) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的需要通道管理员的证明");
                        }
                        break;
                    case PARENT_AGREE:
                        if (!isFather) {
                            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除角色的需要父成员的证明");
                        }
                        break;
                    default:
                        throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持成员角色加入");
            }
        }*/
    }

    private void checkDeleteMemberApprovalList(List<MemberDeleteApproval> memberDeleteApprovals, Channel channel, Member existMember) {
        //组织证书map
        Map<String, Member> orgMemberMap = new HashMap<>();
        boolean isFather = false;
        boolean isChannelAdmin = false;
        String orgChannelAdminPublicKey = "";
        for (Peer peer : channel.getMemberPeerList()) {
            //节点的所有组织证书
            Member orgMember = new Member();
            orgMember.setCertificateCerFile(peer.getPeerOrganization().getCertificateCerFile());
            //解析组织证书的其他属性
            ledgerMgr.processMemberCertificate(orgMember);
            orgMemberMap.put(peer.getPeerOrganization().getPublicKey(), orgMember);
            //是否是组织管理员
            if (peer.getPeerOrganization().getOrganizationId().equals(channel.getOrganizationId())) {
                orgChannelAdminPublicKey = peer.getPeerOrganization().getPublicKey();
            }
        }
        for (MemberDeleteApproval approval : memberDeleteApprovals) {
            Member orgMember = orgMemberMap.get(approval.getSignerIdentityKey().getIdentityKey().getValue());
            //判断调用者是不是通道内的组织
            if (orgMember == null) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkAddMemberApprovalList,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书错误");
            }
            if (orgChannelAdminPublicKey.equals(orgMember.getPublicKey())) {
                isChannelAdmin = true;
            }
            //TODO 已经调整,证书间的包含关系
            if (existMember.getIssuerId().contains(orgMember.getName())) {
                isFather = true;
            }
        }
        //检查成员修改策略
        //投票数量
        int approvalCnt = memberDeleteApprovals.size();
        long size = orgMemberMap.size();


        switch (channel.getMemberRemoveChannelStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持修删除成员的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的证明数量过少");
                }
                break;
            case MANAGER_AGREE:
                if (!isChannelAdmin) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的需要通道管理员的证明");
                }
                break;
            case PARENT_AGREE:
                if (!isChannelAdmin && !isFather) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持删除成员的需要父成员或通道管理员的证明");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持删除角色");
        }
    }


    /**
     * 删除成员检查，未通过直接抛异常
     *
     * @param memberDeleteApprovals
     * @return
     */
    private Member checkDeleteMember(List<MemberDeleteApproval> memberDeleteApprovals, Channel channel) {
        //修改的成员的参数基本检查
        if (CollectionUtils.isEmpty(memberDeleteApprovals)) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteMember,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "删除成员时，参数为空");
        }
        MemberDeleteInfo firstDeleteInfo = memberDeleteApprovals.get(0).getMemberDeleteInfo();
        if (null == firstDeleteInfo
                || StringUtils.isEmpty(firstDeleteInfo.getChannelId())
                || StringUtils.isEmpty(firstDeleteInfo.getPublicKey())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkDeleteMember,member参数错误,form={}", firstDeleteInfo);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "删除成员时，参数错误");
        }

        Member existMember = memberLedgerMgr.getMember(Member.createInstance(firstDeleteInfo));
        if (null == existMember) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member不存在");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "删除成员时，参数错误，成员不存在");
        }
        //成员已经被删除，执行抛异常
        if(existMember.getStatus()==MemberStateEnum.DELETED.code)
        {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,member已经被删除");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "删除成员时，参数错误，成员已经被删除");
        }
        for (MemberDeleteApproval approval : memberDeleteApprovals) {
            String orgPublicKey = approval.getSignerIdentityKey().getIdentityKey().getValue();
            // 验签
            if (!ledgerMgr.verifySign(approval, orgPublicKey)) {
                log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkUpdateMember,参数错误,approval={}", approval);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "该节点的组织根证书签名错误");
            }
        }
        // 角色赋值
        Member newMember = existMember.clone();
        newMember.setRoles(new ArrayList<>());
        newMember.setStatus(MemberStateEnum.DELETED.getCode());
        checkApprovalList(memberDeleteApprovals, channel);
        checkDeleteMemberApprovalList(memberDeleteApprovals, channel, existMember);
        return newMember;
    }


    /**
     * 角色列表前后的比对
     *
     * @param newRoles
     * @param oldRoles
     * @param addRoles
     * @param delRoles
     */
    private void compareRoleList(List<Role> newRoles, List<Role> oldRoles, List<Role> addRoles, List<Role> delRoles) {
        Map<String, Role> oldRoleMap = new HashMap<>();
        for (Role role : oldRoles) {
            oldRoleMap.put(role.getRoleId(), role);
        }

        Map<String, Role> newRoleMap = new HashMap<>();
        for (Role role : newRoles) {
            newRoleMap.put(role.getRoleId(), role);
            //删除的
            if (role.getState() == 2) {
                delRoles.add(role);
            } else {
                //新增的
                if (oldRoleMap.get(role.getRoleId()) == null) {
                    addRoles.add(role);
                }
            }
        }
        for (Role role : oldRoles) {
            //新的里面没有旧的是删除
            if (newRoleMap.get(role.getRoleId()) == null) {
                delRoles.add(role);
            }
        }

    }

    /**
     * 变化后的和之前赋值
     *
     * @param existMember
     * @param updateMember
     */
    private void modifyMember(Member existMember, Member updateMember) {
        if (updateMember.getId() == null) {
            updateMember.setId(existMember.getId());
        }
        if (updateMember.getStatus() == null) {
            updateMember.setStatus(existMember.getStatus());
        }
        if (StringUtils.isEmpty(updateMember.getName())) {
            updateMember.setName(existMember.getName());
        }
        if (StringUtils.isEmpty(updateMember.getIssuerId())) {
            updateMember.setIssuerId(existMember.getIssuerId());
        }
        if (StringUtils.isEmpty(updateMember.getExtendedData())) {
            updateMember.setExtendedData(existMember.getExtendedData());
        }
        if (CollectionUtils.isEmpty(updateMember.getRoles())) {
            updateMember.setRoles(existMember.getRoles());
        }
        if (StringUtils.isEmpty(updateMember.getSignAlgorithm())) {
            updateMember.setSignAlgorithm(existMember.getSignAlgorithm());
        }
        if (StringUtils.isEmpty(updateMember.getCertificateCerFile())) {
            updateMember.setCertificateCerFile(existMember.getCertificateCerFile());
        }
    }


    /**
     * 查询成员检查，未通过直接抛异常
     *
     * @param queryMemberReq
     * @return
     */
    private void checkGetMember(QueryMemberReq queryMemberReq) {
        //成员的参数基本检查
        if (null == queryMemberReq
                || StringUtils.isEmpty(queryMemberReq.getChannelId())
                || StringUtils.isEmpty(queryMemberReq.getPublicKey())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkGetMember,member参数错误,QueryMemberReq={}", queryMemberReq);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "查询成员时，参数错误");
        }

    }

    /**
     * 查询成员列表检查，未通过直接抛异常
     *
     * @param queryMemberListReq
     * @return
     */
    private void checkGetMemberList(QueryMemberListReq queryMemberListReq) {
        //成员的参数基本检查
        if (queryMemberListReq == null) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkGetMemberList,参数错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "查询成员列表时，参数为空");
        }
        if (StringUtils.isEmpty(queryMemberListReq.getChannelId())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkGetMemberList,member参数错误,queryMemberListReq={}", queryMemberListReq);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "查询成员列表时，参数错误");
        }
    }

    /**
     * 校验是调用者是通道中的成员
     *
     * @param channelId
     * @param signerIdentityKey
     */
    private void checkVaidateCallerIdentity(String channelId, SignerIdentityKey signerIdentityKey) {
        Channel channel = ledgerMgr.queryChannel(channelId);
        Set<String> memmberSet = new HashSet<>();
        for (Member member : channel.getMembers()) {
            memmberSet.add(member.getChannelId() + member.getPublicKey());
        }

        if (!memmberSet.contains(signerIdentityKey.getIdentityKey().getChannelId() + signerIdentityKey.getIdentityKey().getValue())) {
            log.warn(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.checkVaidateCallerIdentity,身份参数错误,signerIdentityKey={}", signerIdentityKey);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "查询成员时，身份参数错误");
        }

    }

    /**
     * 如果未通过检查会抛出异常
     *
     * @return
     */
/*    private Channel checkDepolySmartContract(List<SmartContractDeplyApproval> smartContractDeplyApprovalList) {
        Channel channel = ledgerMgr.getChannel(smartContractDeplyApprovalList.get(0).getChannelId());
        if (channel == null) {
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }

        Set<IdentityKey> identitySet = new HashSet<>();
        SmartContract smartContract = smartContractDeplyApprovalList.get(0).getNewSmartContract();
        smartContract.setMemberId(smartContract.getCode() + "_" + smartContract.getVersion());
        for (SmartContractDeplyApproval approval : smartContractDeplyApprovalList) {
            if (approval.getChannelId().equals(channel.getChannelId()) == false) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，所有证明需使用相同的ChannelID");
            }

            //TODO 这里还需要检查智能合约的内容与Hash值匹配，签名与内容匹配
            if (smartContract.equals(approval.getNewSmartContract()) == false) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，所有证明需使用相同的智能合约");
            }
            if (identitySet.contains(approval.getSignerIdentityKey().getIdentityKey())) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "部署智能合约时，存在重复的证明组织");
            } else {
                identitySet.add(approval.getSignerIdentityKey().getIdentityKey());
            }
        }

        //检查节点加入策略
        int approvalCnt = smartContractDeplyApprovalList.size();
        int size = channel.getMemberPeerList().size();
        switch (channel.getSmartContractDeplyStrategy()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持部署智能合约的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持部署智能合约的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持部署智能合约的证明数量过少");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持节点加入");
        }

        //TODO 这里最好再检查一下Java字节码的兼容性，即是否可正常的在本节点运行
        //是否是重复部署

        SmartContract newSmartContract = smartContractDeplyApprovalList.get(0).getNewSmartContract();
        newSmartContract.setMemberId(newSmartContract.getCode() + "_" + newSmartContract.getVersion());
        List<SmartContract> alreadyExistSmartContractList = channel.getSmartContractList();
        for(SmartContract smartContractTemp : alreadyExistSmartContractList)
        {
            if(smartContractTemp.equals(newSmartContract))
            {
                throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_ALREADY_EXIST);
            }
        }
        return channel;
    }*/

    /**
     * 如果不合法，抛出异常
     * 不抛异常执行完毕，表示节点可以加入本通道，返回可以加入的通道对象
     *
     * @param peerAddChannelApprovalList
     */
/*    private Channel checkAddOnePeerParam(List<PeerAddChannelApproval> peerAddChannelApprovalList) {

        String channelId = peerAddChannelApprovalList.get(0).getChannelId();
        Channel channel = ledgerMgr.getChannel(channelId);
        int size = channel.getMemberPeerList().size();
        if (size >= channel.getMaxPeerCount()) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，组织节点数已达到最大值");
        }


        //入参合法性检查
        IdentityKey newMemberPeer = peerAddChannelApprovalList.get(0).getNewMemberPeer().getPeerId();
        if (channel.containPeerId(newMemberPeer)) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加的节点已经在Channel中");
        }
        Set<IdentityKey> identitySet = new HashSet<>();
        for (PeerAddChannelApproval approval : peerAddChannelApprovalList) {
            if (approval.getChannelId().equals(channelId) == false) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，所有证明需使用相同的ChannelID");
            }
            if (approval.getNewMemberPeer().getPeerId().equals(newMemberPeer) == false) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，所有证明需使用相同的新节点ID");
            }
            if (identitySet.contains(approval.getSignerIdentityKey().getIdentityKey())) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "添加新节点时，存在重复的证明组织");
            } else {
                identitySet.add(approval.getSignerIdentityKey().getIdentityKey());
            }
        }

        //检查节点加入策略
        int approvalCnt = peerAddChannelApprovalList.size();
        switch (channel.getPeerAddStrategyEnum()) {
            case ABSOLUTE_MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 2.0 / 3.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新节点加入的证明数量过少");
                }
                break;
            case ALL_AGREE:
                if (approvalCnt != size) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新节点加入的证明数量过少");
                }
                break;
            case MAJORITY_AGREE:
                if (approvalCnt + 0.0 / size < 1.0 / 2.0) {
                    throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "支持新节点加入的证明数量过少");
                }
                break;
            default:
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "待加入的通道不支持节点加入");
        }
        return channel;
    }*/

    /**
     * @param vo
     * @return
     */
    @ApiOperation("查询系统智能合约列表")
    //@PostMapping(value = "/getSmartContractList")
    public BizVO getSmartContractList(@RequestBody QuerySmartContractListReq vo) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getSmartContractList.start");
        //参数校验
        if (vo == null || StringUtils.isEmpty(vo.getChannelId()) ||
                (null != vo.getFlag() && !"1".equals(vo.getFlag()) &&
                        !"2".equals(vo.getFlag()) && !"3".equals(vo.getFlag())) ||
                vo.getPageNo() <= 0 || vo.getPageSize() <= 0) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getSmartContractList,类型错误vo={}", vo);
            return null;
        }
        return ledgerMgr.getSmartContractAllList(vo);
    }

    /**
     * @param vo
     * @return
     */
    @ApiOperation("查询系统智能合约")
    //@PostMapping(value = "/getSmartContract")
    public SmartContract getSmartContract(@RequestBody QuerySmartContractReq vo) {
        log.info(ModuleClassification.TxM_SSC_ + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getSmartContract.start");
        //参数校验
        if (vo == null || StringUtils.isEmpty(vo.getChannelId()) ||
                StringUtils.isEmpty(vo.getAlisa()) || StringUtils.isEmpty(vo.getVersion())) {
            log.error(ModuleClassification.TxM_SSC_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_USER_ACTION + ",SystemSmartContract.getSmartContract,类型错误vo={}", vo);
            return null;
        }
        return ledgerMgr.getSmartContractInfo(vo);
    }
}
