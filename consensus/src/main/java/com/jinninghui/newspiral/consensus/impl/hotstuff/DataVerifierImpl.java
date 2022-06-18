package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.VersionEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.consensus.hotstuff.DataVerifier;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ganzirong
 * @date 2020/12/21
 * 数据校验基本上包含以下几项内容：
 * 1. 篡改校验；
 * 2. 签名校验；
 * 3. 生成规则校验（如果有的话）
 */
@Slf4j
@Component
public class DataVerifierImpl implements DataVerifier {

    @SofaReference
    private SecurityServiceMgr securityServiceMgr;

    @SofaReference
    private DataSecurityMgr dataSecurityMgr;

    public void init() {

    }

    public boolean verifyBlock(Block block, String securityServiceKey) {
        //先校验交易的合法性
        List<SDKTransaction> sdkTransactions = block.getSDKTransactions();
        if(!CollectionUtils.isEmpty(sdkTransactions)){
            for (SDKTransaction sdkTransaction : sdkTransactions) {
                if (verifyTransaction(sdkTransaction, securityServiceKey) == false) {
                    log.error("DataVerifierImpl.verifyBlock is false, transaction is modified,sdkTransaction:transHash={},sdkTransaction={},",
                            sdkTransaction.getHash(),JSONObject.toJSON(sdkTransaction));
                    return false;
                }
            }
        }
        SecurityService securityService = getSecurityService(securityServiceKey);
        //检查默克尔根
        String merkleRoot = SignForConsensus.merkleBlock(block);
        if (block.getBlockHeader().getMerkleRoot().equals(merkleRoot) == false) {
            log.error("DataVerifierImpl.verifyBlock is false, merkleRoot is not equal,merkleRoot1={},merkleRoot2={}",block.getBlockHeader().getMerkleRoot(),merkleRoot);
            return false;
        }
        //检查区块签名
        if (VersionEnum.FABRIC.getCode().equals(block.getBlockHeader().getVersion())) {
            //兼容Farbic1.0区块，区块hash需要直接计算，不能使用blockheader中的hash
            BlockHeader blockHeader = block.getBlockHeader().clone();
            blockHeader.setConsensusTimestamp(new Long(0));
            blockHeader.setPersistenceTimestamp(new Long(0));
            blockHeader.setPackagerAndSign(null);
            blockHeader.setWitness(null);
            //log.info("block height:"+blockHeader.getHeight());
            //log.info("block header bytes:"+Arrays.toString(JSONObject.toJSON(blockHeader).toString().getBytes()));
            byte[] bytes = securityService.calcHashBytes(JSONObject.toJSON(blockHeader).toString().getBytes());
            String blockHash = Hex.encodeHexString(bytes, false);
            //log.info("blockHash:"+ blockHash);
            if (securityService.verifySignature(blockHash, block.getSignerIdentityKey()) == false) {
                log.error("(Fabric1.0)verifyBlock.verifySignature is false, invalid signature of block, blockhash:" + blockHash +
                        ",block height:" + block.getBlockHeader().getHeight() +
                        " blockheader:" + JSONObject.toJSON(block.getBlockHeader()).toString());
                return false;
            }
        } else {
            String blockhash = SignForConsensus.hashBlockHeader(block.getBlockHeader(), securityService);
            if (securityService.verifySignature(blockhash, block.getSignerIdentityKey()) == false) {
                log.error("verifyBlock.verifySignature is false, invalid signature of block, blockhash:" + blockhash);
                return false;
            }
        }
        return true;
    }

    public boolean verifyTransaction(SDKTransaction sdkTransaction, String securityServiceKey) {
        SecurityService securityService = getSecurityService(securityServiceKey);
        if (VersionEnum.FABRIC.getCode().equals(sdkTransaction.getVersion())) {
            //检查token中的签名是否为对该交易本身做hash后的签名
            List<NewSpiralPolicyToken> tokens = sdkTransaction.getTokenList();
            sdkTransaction.setTokenList(null);
            byte[] sdkTxBytes = JSONObject.toJSON(sdkTransaction).toString().getBytes();
            String txHash = dataSecurityMgr.hash(sdkTxBytes);
            sdkTransaction.setTokenList(tokens);
            if (tokens.isEmpty()) {
                return false;
            }
            if (tokens.get(0).getHash().equals(txHash) == false) {
                return false;
            }
            if (securityService.verifySignature(tokens.get(0).getHash(), tokens.get(0).getSignerIdentityKey()) == false) {
                log.error("(Fabric1.0)verifyTransaction.verifySignature is error,tokenHash={},identityKey={},sdkTransaction={}",
                        tokens.get(0).getHash(),JSONObject.toJSON(tokens.get(0).getSignerIdentityKey()),JSONObject.toJSON(sdkTransaction));
                return false;
            }
        } else {
            transferMethodArgs(sdkTransaction.getSmartContractCallInstnace());
            //TODO 这里先这么搞吧，有些交易例如添加节点是节点证书是byte类型，入库交易后类型为String了
            dataSecurityMgr.hash(sdkTransaction);
            //TODO 安全测评先这么写哈
/*            if(sdkTransaction.getSmartContractCallInstnace().getMethodName().equals("businessWriteSCSafety")&&
                    !sdkTransaction.getSmartContractCallInstnace().getSmartContractId().equals("com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.SystemSmartContract"))
            {
                dataSecurityMgr.hash(sdkTransaction);
            }*/

            if (securityService.verifySignatureByPublicKey(sdkTransaction.getHash(), sdkTransaction.getSignerIdentityKey()) == false) {
                log.error("verifyTransaction.verifySignature is error,txHash={},sdkTransaction={}",sdkTransaction.getHash(),JSONObject.toJSON(sdkTransaction));
                return false;
            }
        }
        return true;
    }
    //todo:将共识流程中的QC的检查也统一起来，当前只用在同步历史数据中。
    public boolean verifyGenericQC(GenericQC genericQC, Channel channel) {
        //检查是否有足够的签名
        List<Peer> peers = channel.getValidMemberPeerList(genericQC.getHeight());
        //int total = peers.parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList()).size();
        int total = peers.size();
        //Iterator<Peer> it = peers.iterator();
        /*while(it.hasNext()){
            Peer peer = (Peer)it.next();
            //证书变动时的高度
            Long maxBlockHeight = peer.getPeerCert().get(0).getBlockHeight();
            String flag=peer.getPeerCert().get(0).getFlag();
            if ("0".equals(flag)&&(genericQC.getHeight() <maxBlockHeight + 3)&&maxBlockHeight!=0) {
                total=total-1;
            }
        }*/
        int need = (total * 2) / 3 + 1;
        if (genericQC.getVoteMap().size() < need) {
            if (genericQC.getVersion().equals("Fabric1.0") || genericQC.getHeight() > 2) {
                log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                        " less block votes than needed. GenericQC:" + genericQC.toString());
                return false;
            }
        }
        SecurityService securityService = getSecurityService(channel.getSecurityServiceKey());
        //检查签名的合法性：是否由指定范围内的节点签；签名是否合法
        Set<String> msgBussinessKey = new HashSet<>();
        for (BlockVoteMsg blockVoteMsg : genericQC.getVoteMap().values()) {
            //检查是否有重复的投票
            if (msgBussinessKey.contains(blockVoteMsg.getBussinessKey())) {
                log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                        " repeated BlockVoteMsg in genericQC:", genericQC.toString());
                return false;
            } else {
                msgBussinessKey.add(blockVoteMsg.getBussinessKey());
            }
            //检查是否指向同一个区块
            if (blockVoteMsg.getBlockHash().equals(genericQC.getBlockHash()) == false) {
                log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                        " invalid blockVoteMsg: " + blockVoteMsg.toString() +
                        " in genericQC:" + genericQC.toString());
                return false;
            }
            //检查是否为有效节点的签名
            boolean isFromValidPeer = false;
            Peer senderPeer = null;
            for (Peer peer: peers) {
                if (blockVoteMsg.getSignerIdentityKey().getIdentityKey().equals(peer.getPeerId())) {
                    isFromValidPeer = true;
                    senderPeer = peer;
                    break;
                }
            }
            if (isFromValidPeer == false) {
                log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                        " blockVoteMsg not signed by valid peer in channel. GenericQC:" +
                        genericQC.toString());
                return false;
            }

            //todo:证书是否有效
            String hash = dataSecurityMgr.calcHash(blockVoteMsg);
            if (false == hash.equals(blockVoteMsg.getHash())) {
                log.info(ModuleClassification.ConM_Sync_.toString() + channel.getChannelId() +
                        " blockVoteMsg has been modified, blockVoteMsg:" + blockVoteMsg.toString());
            }
            if (securityService.verifySignature(hash, blockVoteMsg.getSignerIdentityKey()) == false) {
                log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                        " invalid signature in blockVoteMsg:" + blockVoteMsg.toString());
                return false;
            }
            /*if (genericQC.getVersion().equals("Fabric1.0") == false) {
                SignerIdentityKey signerIdentityKey = blockVoteMsg.getSignerIdentityKey();
                blockVoteMsg.setSignerIdentityKey(null);
                byte[] content = JSONObject.toJSON(blockVoteMsg).toString().getBytes();
                blockVoteMsg.setSignerIdentityKey(signerIdentityKey);
                String hash = dataSecurityMgr.hash(content);
                if (securityService.verifySignature(hash, blockVoteMsg.getSignerIdentityKey()) == false) {
                    log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                            " invalid signature in blockVoteMsg:" + blockVoteMsg.toString());
                    return false;
                }
            } else {

                String hash = dataSecurityMgr.calcHash(blockVoteMsg);
                if (securityService.verifySignature(hash, blockVoteMsg.getSignerIdentityKey()) == false) {
                    log.info(ModuleClassification.ConM_Verify_.toString() + channel.getChannelId() +
                            " invalid signature in blockVoteMsg:" + blockVoteMsg.toString());
                    return false;
                }
            }*/

        }
        return true;
    }

    private SecurityService getSecurityService(String key) {
        return securityServiceMgr.getMatchSecurityService(key);
    }

    /**
     * 合约参数如果是json进行实体转化
     * @param smartContractCallInstnace
     * @return
     */
    private static SmartContractCallInstnace transferMethodArgs(SmartContractCallInstnace smartContractCallInstnace) {
        if (null == smartContractCallInstnace.getMethodArgs() ||
                null == smartContractCallInstnace.getMethodArgClassNames()) {
            return smartContractCallInstnace;
        }
        Object[] methodArgs = smartContractCallInstnace.getMethodArgs();
        //校验参数个数是否一致
        int j = smartContractCallInstnace.getMethodArgs().length;
        if (j != smartContractCallInstnace.getMethodArgClassNames().length) {
            return smartContractCallInstnace;
        }
        try {
            for (int i = 0; i < j; i++) {
                Object methodArg = smartContractCallInstnace.getMethodArgs()[i];
                if (methodArg instanceof JSONArray) {
                    methodArgs[i]=JSONObject.parseArray(JSON.toJSONString(methodArg), Class.forName(smartContractCallInstnace.getMethodArgClassNames()[i]));
                }
                if (methodArg instanceof JSONObject) {
                    methodArgs[i]=JSON.parseObject(JSON.toJSONString(methodArg), Class.forName(smartContractCallInstnace.getMethodArgClassNames()[i]));
                }
            }
            smartContractCallInstnace.setMethodArgs(methodArgs);
        } catch (Exception e) {
            log.error(ModuleClassification.ConM_Verify_+"TError"+"DataVerifierImpl.transferMethodArgs,error={}", e.getMessage(),e);
        } finally {
            return smartContractCallInstnace;
        }
    }
}
