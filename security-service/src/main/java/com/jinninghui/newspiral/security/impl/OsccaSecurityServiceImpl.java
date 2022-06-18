package com.jinninghui.newspiral.security.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificateCipher;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.security.PeerCertificateCipherReq;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.common.entity.util.OsccaCinpher;
import com.jinninghui.newspiral.security.utils.CertificateUtil;
import com.jinninghui.newspiral.security.utils.GMCertificateUtil;
import com.jinninghui.newspiral.common.entity.util.MerkleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lida
 * @date 2019/7/13 19:40
 */
@Slf4j
public class OsccaSecurityServiceImpl implements SecurityService {

    //@SofaReference
    //private ObjectSerializer serializer;

    LedgerMgr ledgerMgr;

    /**
     * key1为对端节点的身份标识，key2为channelId,不给get/set方法
     */
    Map<String, Map<String, String>> peerPrivateKeyMap = new HashMap<>();

    /**
     * key为channelId，不给get/set方法
     */
    Map<String, String> channelPublicKeyMap = new HashMap<>();
    /**
     * key1为对端节点的身份标识,不给get/set方法,存储证书相关信息
     */
    //Map<String, PeerCertificateCipher> peerCertificateCipherMap = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 密钥缓存 key1为channelId,key2为节点的身份标识
     */
    Map<String, Map<String, PeerCertificateCipher>> peerChannelCertificateCipherMap = new ConcurrentHashMap<>();


    /**
     * 本节点的身份标识，使用公私钥对时，需包含私钥
     */
    Identity localIdentity;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public SignerIdentityKey getMyCallerIdentity() {
        SignerIdentityKey callerIdentity = new SignerIdentityKey();
        callerIdentity.setIdentityKey(localIdentity.getKey().clone());
        callerIdentity.setSignature(null);
        return callerIdentity;
    }

    @Override
    public byte[] calcHashBytes(byte[] contentBytes) {
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.calcHashBytes:");
        byte[] bytes = "".getBytes();
        //synchronized (this) {
        bytes = OsccaCinpher.calHashBySM3(contentBytes);
        //log.info("After OsccaCinpher:"+Arrays.toString(bytes));
        //}
        return bytes;

    }

    @Override
    public void hash(Hashable param) {
        log.debug(ModuleClassification.SM_OSSI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",OsccaSecurityServiceImpl.hash");

        // 序列化对象
        param.setHash(null);
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.hash.rpcParam0:{}", param);
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.hash.rpcParam:{}", JSONObject.toJSON(param));
        //byte[] bytes = KryoUtil.writeToByteArray(param);
        //byte[] bytes = KryoUtil.writeToByteArray(JSONObject.toJSON(param));
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.bytes:{}" ,bytes);
        // 计算hash值
        param.setHash(Block.bytesToHexString(calcHashBytes(bytes)));
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.hash=" + param.getHash());
    }

    @Override
    public String hashBlock(Block block) {
        String hash = block.getHash();
        SignerIdentityKey callerIdentityKey = block.getSignerIdentityKey();
        String root = block.getBlockHeader().getMerkleRoot();
        block.setSignerIdentityKey(null);
        block.setHash(null);
        String merkleRoot = Hex.encodeHexString(MerkleUtil.merkleBlock(block));
        block.getBlockHeader().setMerkleRoot(merkleRoot);
        byte[] blockHashBytes = calcHashBytes(JSONObject.toJSON(block.getBlockHeader()).toString().getBytes());
        block.setSignerIdentityKey(callerIdentityKey);
        block.setHash(hash);
        block.getBlockHeader().setMerkleRoot(root);
        return Hex.encodeHexString(blockHashBytes, false);
    }

  /*  private byte[] merkleBlock(Block block) {
        ArrayList<byte[]> tree = new ArrayList<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            tree.add(tx.getSdkTransaction().getHash().getBytes());
        }
        return merkle(tree);
    }

    private byte[] combine(byte[] a, byte[] b) {
        byte[] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }


    public byte[] merkle(ArrayList<byte[]> tree) {
        try {
            if (tree.isEmpty()) {
                //log.info("Null tree in merkle");
                return "".getBytes();
            }
            if (tree.size() == 1) {
                return calcHashBytes(tree.get(0));
            }
            ArrayList<byte[]> node = new ArrayList<>();
            node.addAll(tree);
            ArrayList<byte[]> nodeLevelUp = new ArrayList<>();
            while (node.size() > 1) {
                for (int i = 0; i < node.size(); i += 2) {
                    if (i < (node.size() - 1)) {
                        nodeLevelUp.add(calcHashBytes(combine(node.get(i), node.get(i+1))));
                    } else {
                        nodeLevelUp.add(calcHashBytes(node.get(i)));
                    }
                }
                node.clear();
                node.addAll(nodeLevelUp);
                nodeLevelUp.clear();
            }
            return node.get(0);
        } catch (Exception ex) {
            log.error("Exception in merkle", ex);
            return "".getBytes();
        }

        *//*Stack<Integer> powerStack = new Stack<>();
        int size = tree.size();
        int power = 0;
        while (size > 0) {
            if ((size & 0x00000001) > 0) {
                powerStack.push(power);
            }
            power++;
            size = size >> 1;
        }
        List<ArrayList<byte[]>> trees = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        int count = 0;
        while (!powerStack.isEmpty()) {
            Integer p = powerStack.pop();
            powerList.add(p);
            int leaf = 1 << p;
            ArrayList<byte[]> bytesOfLeaf = new ArrayList<>();
            bytesOfLeaf.addAll(tree.subList(count, count+leaf));
            trees.add(bytesOfLeaf);
            count += leaf;
        }
        Map<Integer, byte[]> hashingOfLeaf = new ConcurrentHashMap<>();
        for (int i = 0; i < powerList.size(); i++) {
            hashingOfLeaf.put(i, null);
        }
        hashingOfLeaf.entrySet().parallelStream().forEach(
                integerEntry -> hashingOfLeaf.put(integerEntry.getKey(), merkleFullBinaryTree(trees.get(integerEntry.getKey())))
        );
        byte[] result = hashingOfLeaf.get(powerList.get(tree.size()-1));
        for (int i = trees.size(); i > 0; i--) {
            int m = powerList.get(i-1);
            byte[] nodeR = hashingOfLeaf.get(m);
            if (i > 1) {
                int n = powerList.get(i - 2);
                byte[] nodeL = hashingOfLeaf.get(n);
                int needHashing = n - m;
                while (needHashing > 0) {

                }
            }
        }*//*
    }

    private byte[] merkleFullBinaryTree(ArrayList<byte[]> tree) {
        int leaf = tree.size();
        if (leaf <= 0) {
            return "".getBytes();
        }
        if (leaf == 1) {
            return tree.get(0);
        }
        int cnt = 0;
        int time = 0;
        while (time <= 30) {
            int i = (leaf << time) & 0x40000000;
            if (i > 0) {
                cnt++;
            }
            if (cnt > 1) {
                log.info("Not full binary tree");
                return "".getBytes();
            }
            time++;
        }
        ArrayList<byte[]> node = new ArrayList<>();
        for (int i = 0; i < leaf; i++) {
            byte[] bytes = new byte[tree.get(i).length];
            System.arraycopy(tree.get(i), 0, bytes, 0, tree.get(i).length);
            node.add(bytes);
        }
        while (leaf > 1) {
            ArrayList<byte[]> nodeLevelUp = new ArrayList<>();
            for (int i = 0; i < leaf; i += 2) {
                nodeLevelUp.add(calcHashBytes(combine(node.get(i), node.get(i+1))));
            }
            node.clear();
            node = nodeLevelUp;
            leaf = leaf >> 1;

        }
        return node.get(0);
    }*/

    /**
     * 验证hash值，需要注意的是
     * 1.入参传入时，SignerIdentityKey需要置为空
     * 2.入参转化为string类型时，用的是JSONObject.toJson().toString，和Json.toString（）有区别
     * @param param
     * @return
     */
    @Override
    public boolean verifyHash(Hashable param) {
        //log.info(ModuleClassification.SM_OSSI_+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",OsccaSecurityServiceImpl.verifyHash:");
        String bytesHash = param.getHash();
        param.setHash(null);
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifyHash.param0:{}", param);
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifyHash.param:{}", JSONObject.toJSON(param));
        //byte[] bytes = KryoUtil.writeToByteArray(param);
        //byte[] bytes = KryoUtil.writeToByteArray(JSONObject.toJSON(param));
        //TODO 注意这里是用了JSONObject.toJson().toString噢
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifyHash.bytes:{}", bytes);
        String verifyBytesHash = Block.bytesToHexString(calcHashBytes(bytes));
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifyHash.bytesHash:{},verifyBytesHash:{}", bytesHash,verifyBytesHash);
        if (bytesHash.equals(verifyBytesHash)) {
            //log.info(ModuleClassification.SM_OSSI_+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",OsccaSecurityServiceImpl.verifyHash:true," + verifyBytesHash);
            return true;
        }
        log.info(ModuleClassification.SM_OSSI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",OsccaSecurityServiceImpl.verifyHash is false,verifyHash={}", verifyBytesHash);
        return false;
    }

    /**
     * 初始化实例，按照配置的策略读取目录下的相关私钥等文件完成初始化
     * 保证此方法调用后安全服务的各个对外接口可正常对外提供服务
     *
     * @param localIdentity
     */
    public void init(Identity localIdentity, Map<String, Map<String, PeerCertificateCipher>> peerChannelCertificateCipherMap, LedgerMgr ledgerMgr) {
        //本地身份的初始化
        this.localIdentity = localIdentity;
        //this.peerPrivateKeyMap = peerPrivateKeyMap;
        //this.channelPublicKeyMap = channelPublicKeyMap;
        //transferPeerCertificateCipherReqMap(peerCertificateCipherMap);
        setPeerCertificateCipherMap(peerChannelCertificateCipherMap);
        this.peerChannelCertificateCipherMap = peerChannelCertificateCipherMap;
        this.ledgerMgr = ledgerMgr;
        //
    }

    /**
     * 国密证书签名
     * @param verifiableData
     * @param channelId
     */
    @Override
    public void signByGMCertificate(VerifiableData verifiableData, String channelId) {
        // log.info(ModuleClassification.SM_OSSI_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",OsccaSecurityServiceImpl.signByGMCertificate.start:inpara {}", verifiableData);
        //获取本地节点的身份
        SignerIdentityKey sender = this.getMyCallerIdentity();
        sender.getIdentityKey().setChannelId(channelId);
        //根据peerId以及通道Id获取加解密实体
        PeerCertificateCipher peerCertificateCipher = getPeerCertificateCipher(sender.getIdentityKey().getValue(), channelId);
        if (null == peerCertificateCipher ||
                null == peerCertificateCipher.getPrivateKey()) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.signByGMCertificate：Certificate parameter(PeerCertificateCipher) is error,para:{}", peerCertificateCipher);
            return;
        }
        try {
            //生成签名，使用私钥对hash进行签名
            byte[] signature = GMCertificateUtil.signByGM(peerCertificateCipher.getPrivateKey(), Block.hexStringToByte(verifiableData.getHash()));
            sender.setSignature(Hex.encodeHexString(signature, false));
            //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.signByGMCertificate.textHash:{},signature:{}", verifiableData.getHash().toString(), sender.getSignature());
            verifiableData.setSignerIdentityKey(sender);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "TError", "OsccaSecurityServiceImpl.signByGMCertificate：Certificate produce sign is error:", e);
        }
        // log.info(ModuleClassification.SM_OSSI_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",OsccaSecurityServiceImpl.signByGMCertificate.end:outpara {}", JSONObject.toJSON(verifiableData));
    }

    /**
     * 使用国密证书进行验签
     * @param verifiableData 验证消息体
     * @param channelId 通道id，一个通道id和peerid对应了一个解密实体
     * @return
     */
    @Override
    public boolean verifySignatureByGMCertificate(VerifiableData verifiableData, String channelId) {
        //log.info(ModuleClassification.SM_OSSI_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",OsccaSecurityServiceImpl.verifySignatureByGMCertificate.start:{}", verifiableData);
        //节点身份判空
        if (null == verifiableData.getSignerIdentityKey() ||
                verifiableData.getSignerIdentityKey().getIdentityKey().valid() == false) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：SignerIdentityKey is empty! ");
            return false;
        }
        // TODO 取出身份以及hash本身进行hash比对，哈希完成后记得赋值回去
        SignerIdentityKey signerIdentityKey = verifiableData.getSignerIdentityKey();
        String hash = verifiableData.getHash();
        verifiableData.setSignerIdentityKey(null);
        //验证hash字段是否和计算得到的hash值一样，计算之前需要将签名字段置为null,hash字段也需要置为null
        //原因是加密的过程是，先计算hash，放到hash字段上，然后再签名，放到签名字段中，所以重新计算时，这两个字段要置空
        if (!verifyHash(verifiableData)) {
            verifiableData.setSignerIdentityKey(signerIdentityKey);
            verifiableData.setHash(hash);
            //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifySignatureByGMCertificate.verifyHash1.verifiableData:{}", JSON.toJSONString(verifiableData));
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate,message data structure modified,hash is different,verifiableData={}", JSON.toJSONString(verifiableData));
            return false;
        }
        verifiableData.setSignerIdentityKey(signerIdentityKey);
        verifiableData.setHash(hash);
        // log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifySignatureByGMCertificate.verifyHash2.verifiableData:{}", JSONObject.toJSON(verifiableData));
        //对入参的hash值进行验签
        return verifySignatureWithoutHashCheck(verifiableData, channelId);
    }

    /**
     * 从缓存或者（数据库）中拿到解密实体，然后对签名的hash进行验签
     * 如果验签成功，说明hash值没有被篡改，签名也没有被篡改
     * 如果签签失败，说明hash值被篡改，或者签名被篡改了
     * @param verifiableData
     * @param channelId
     * @return
     */
    public boolean verifySignatureWithoutHashCheck(VerifiableData verifiableData, String channelId) {
        /**
         * 拿到通道-节点-对应的密码实体,这里的实体对应的是peer_certificate表
         * 节点的密码实体和通道绑定的好处是：同一节点的n个通道中使用相同的证书，可以针对通道做证书的一些冻结解冻的操作
         * peer节点中的证书是当前节点正在使用的证书
         *
         * 场景：a节点使用自己的私钥加密之后，b节点收到消息之后，根据channelId和a的peerid（a的）去数据库中找到了该节点的证书
         * 然后使用公钥进行解密
         */
        PeerCertificateCipher peerCertificateCipher = getPeerCertificateCipher(verifiableData.getSignerIdentityKey().getIdentityKey().getValue(), channelId);
        //证书判空
        if (null == peerCertificateCipher ||
                null == peerCertificateCipher.getX509Certificate() ||
                null == peerCertificateCipher.getPublicKey()) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：Certificate parameter(PeerCertificateCipher) is error,{}", peerCertificateCipher);
            return false;
        }
        try {
            //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(peerCertificateCipher.getX509Certificate())) {
                log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：Certificate is invalid");
                return false;
            }
            //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifySignatureByGMCertificate.textHash:{},signature{}", verifiableData.getHash(), verifiableData.getSignerIdentityKey().getSignature());
            /**
             * 验签，使用私钥加密，使用公钥解密
             * 场景：使用公钥对消息的hash进行延签
             */
            boolean flag= GMCertificateUtil.verifyByGM(peerCertificateCipher.getPublicKey(), Block.hexStringToByte(verifiableData.getHash()), Hex.decodeHex(verifiableData.getSignerIdentityKey().getSignature()));
            if (flag) {
                return true;
            } else {
                return historyGMCertificateVerify(verifiableData.getSignerIdentityKey().getIdentityKey().getValue(), channelId, Block.hexStringToByte(verifiableData.getHash()), Hex.decodeHex(verifiableData.getSignerIdentityKey().getSignature()));
            }
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "TError" + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：CertificateVerifySignature is error:", e);
            return false;
        }
    }

    @Override
    public boolean syncHistoryBlockVerifySignatureByGMCertificate(VerifiableData verifiableData, List<PeerCert> peerCert) {
        if (CollectionUtils.isEmpty(peerCert)) {
            return false;
        }
        //先暂时这么写吧
        //peerCert= ledgerMgr.getPeerCertList(peerCert.get(0).getPeerId());
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.syncHistoryBlockVerifySignatureByGMCertificate.start,verifiableData:{},peerCertificateList:{}", verifiableData, peerCert);
        //节点身份判空
        if (null == verifiableData.getSignerIdentityKey() ||
                verifiableData.getSignerIdentityKey().getIdentityKey().valid() == false) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.syncHistoryBlockVerifySignatureByGMCertificate：SignerIdentityKey is empty! ");
            return false;
        }
        try {
            for (int i = 0; i < peerCert.size(); i++) {
/*                byte[] encryptByte = "".getBytes();
                try {
                    //解密私钥
                    encryptByte = Crypto.decrypt(new String(peerCert.get(i).getCertificateCerFile(), "UTF-8")).getBytes();
                } catch (Exception e) {

                }*/
                //生成证书
                X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(peerCert.get(i).getCertificateCerFile());
                //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.syncHistoryBlockVerifySignatureByGMCertificate.textHash:{},signature{}", verifiableData.getHash().toString(), verifiableData.getSignerIdentityKey().getSign());
                boolean flag = GMCertificateUtil.verifyByGM(receivedCertificate.getPublicKey(), Block.hexStringToByte(verifiableData.getHash()), Hex.decodeHex(verifiableData.getSignerIdentityKey().getSignature()));
                if (flag) return true;
            }
            return false;
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "TError" + "OsccaSecurityServiceImpl.syncHistoryBlockVerifySignatureByGMCertificate：CertificateVerifySignature is error:", e);
            return false;
        }
    }

    @Override
    public void clearPeerCertificateCipherMap(String peerId, String channelId) {
        resetPeerCertificateCipherMap(peerId, channelId);
    }

    @Override
    public boolean verifySignature(String hash, SignerIdentityKey signerIdentityKey) {
        PeerCertificateCipher peerCertificateCipher = getPeerCertificateCipher(signerIdentityKey.getIdentityKey().getValue(), signerIdentityKey.getIdentityKey().getChannelId());
        //证书判空
        if (null == peerCertificateCipher ||
                null == peerCertificateCipher.getX509Certificate() ||
                null == peerCertificateCipher.getPublicKey()) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：Certificate parameter(PeerCertificateCipher) is error,{}", peerCertificateCipher);
            return false;
        }
        try {
             //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(peerCertificateCipher.getX509Certificate())) {
                log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：Certificate is invalid");
                return false;
            }
            //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.verifySignatureByGMCertificate.textHash:{},signature{}", verifiableData.getHash(), verifiableData.getSignerIdentityKey().getSignature());
            boolean flag = GMCertificateUtil.verifyByGM(peerCertificateCipher.getPublicKey(), Block.hexStringToByte(hash), Hex.decodeHex(signerIdentityKey.getSignature()));
            if (flag) {
                return true;
            } else {
                return historyGMCertificateVerify(signerIdentityKey.getIdentityKey().getValue(), signerIdentityKey.getIdentityKey().getChannelId(), Block.hexStringToByte(hash), Hex.decodeHex(signerIdentityKey.getSignature()));
            }
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "TError" + "OsccaSecurityServiceImpl.verifySignatureByGMCertificate：CertificateVerifySignature is error:", e);
            return false;
        }
    }

    @Override
    public boolean verifySignatureByPublicKey(String hash, SignerIdentityKey signerIdentityKey) {
        if (signerIdentityKey == null || signerIdentityKey.getIdentityKey() == null) {
            log.error(ModuleClassification.SM_DSMI_ + "OsccaSecurityServiceImpl.verifySignatureByPublicKey：SignerIdentityKey is empty! ");
            return false;
        }
        //重新hash一遍用于验签使用
        //hash(verifiableData);
        //公钥判空
        if (StringUtils.isEmpty(signerIdentityKey.getIdentityKey().getValue())) {
            log.error(ModuleClassification.SM_DSMI_ + "OsccaSecurityServiceImpl.verifySignatureByPublicKey：publicKeyStr is empty! ");
            return false;
        }
        try {
            //生成公钥
            PublicKey publicKey = GMCertificateUtil.getPublicKey(signerIdentityKey.getIdentityKey().getValue());
            //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.verifySignatureByGMCertificateKey.textHash:{},signature{}", verifiableData.getHash().toString(), verifiableData.getSignerIdentityKey().getSign());
            return GMCertificateUtil.verifyByGM(publicKey, Block.hexStringToByte(hash), Hex.decodeHex(signerIdentityKey.getSignature()));
        } catch (Exception e) {
            log.error(ModuleClassification.SM_DSMI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",OsccaSecurityServiceImpl.verifySignatureByPublicKey：verifySignatureByPublicKey is error:", e);
            return false;
        }
    }


    /**
     * 赋值证书的相关参数
     *
     * @param peerId
     */
    private PeerCertificateCipher getPeerCertificateCipher(String peerId, String channelId) {
        //peerId校验
        if (StringUtils.isEmpty(peerId)) {
            log.info(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.getPeerCertificateCipher,peerId参数错误为空");
            return null;
        }
        //channelId校验
        if (StringUtils.isEmpty(channelId)) {
            log.info(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.getPeerCertificateCipher,channelId参数错误为空");
            return null;
        }

        if (null == peerChannelCertificateCipherMap.get(channelId) ||
                null == peerChannelCertificateCipherMap.get(channelId).get(peerId)||
                null==peerChannelCertificateCipherMap.get(channelId).get(peerId).getPublicKey()) {
            //TODO 重新从数据库里取哈
            Map<String, Map<String, PeerCertificateCipher>> temporaryMap = ledgerMgr.querypeerChannelCertificateCipherMap();
            setPeerCertificateCipherMap(temporaryMap);
            peerChannelCertificateCipherMap=temporaryMap;
        }

        //如果peercertificate表中没有，则从peer表中获取解密实体
        /**
         * peer里的证书是当前使用的证书，else的逻辑理论上是不会进入的，写上是为了避免一些未知错误
         */
        PeerCertificateCipher peerCertificateCipher = peerChannelCertificateCipherMap.get(channelId).get(peerId);
        if (null != peerCertificateCipher && peerCertificateCipher.getPublicKey() != null) {
            return peerCertificateCipher;
        } else {
            peerCertificateCipher = ledgerMgr.queryPeerCertificateByPeerId(peerId);
            setPeerCertificateCipher(peerCertificateCipher);
            return peerCertificateCipher;
        }
    }

    /**
     * 重新加载证书哦
     *
     * @param peerId
     */
    private void resetPeerCertificateCipherMap(String peerId, String channelId) {
        if (null != peerChannelCertificateCipherMap.get(channelId) &&
                null != peerChannelCertificateCipherMap.get(channelId).get(peerId)) {
            //peerChannelCertificateCipherMap.get(channelId).remove(peerId);
            //TODO 重新从数据库里取哈
            Map<String, Map<String, PeerCertificateCipher>> temporaryMap=ledgerMgr.querypeerChannelCertificateCipherMap();
            setPeerCertificateCipherMap(temporaryMap);
            peerChannelCertificateCipherMap=temporaryMap;
        }
    }

    /**
     *
     */
    private void setPeerCertificateCipherMap(Map<String, Map<String, PeerCertificateCipher>> peerCertificateCipherMap) {
        for (Map.Entry<String, Map<String, PeerCertificateCipher>> entry : peerCertificateCipherMap.entrySet()) {
            for (Map.Entry<String, PeerCertificateCipher> peerCertificateCipherEntry : entry.getValue().entrySet()) {
                setPeerCertificateCipher(peerCertificateCipherEntry.getValue());
            }
        }
    }

    /**
     * @param peerCertificateCipher
     */
    private void setPeerCertificateCipher(PeerCertificateCipher peerCertificateCipher) {
        try {
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(peerCertificateCipher.getCertificateCerFile());
            peerCertificateCipher.setX509Certificate(receivedCertificate);
            //解密私钥
            if (peerCertificateCipher.getCertificateKeyStoreFile() != null && peerCertificateCipher.getCertificateKeyStoreFile().length > 0) {
                byte[] encryptByte = GMCrypto.decrypt(new String(peerCertificateCipher.getCertificateKeyStoreFile(), "UTF-8")).getBytes();
                java.security.PrivateKey privateKey = GMCertificateUtil.getPrivateKey(encryptByte);
                peerCertificateCipher.setPrivateKey(privateKey);
            }
            peerCertificateCipher.setPublicKey(receivedCertificate.getPublicKey());
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "OsccaSecurityServiceImpl.setPeerCertificateCipher,解析证书错误，e=", e);
        }
    }

    /**
     * 节点密钥证书实体转化
     *
     * @param peerCertificateCipherMap
     */
    private void transferPeerCertificateCipherReqMap(Map<String, PeerCertificateCipher> peerCertificateCipherMap) {
        Map<String, PeerCertificateCipherReq> peerCertificateCipherReqHashMap = new HashMap<>();
        for (Map.Entry<String, PeerCertificateCipher> entry : peerCertificateCipherMap.entrySet()) {
            PeerCertificateCipherReq peerCertificateCipherReq = new PeerCertificateCipherReq();
            peerCertificateCipherReq.setPeerId(entry.getValue().getPeerId());

            if (null != entry.getValue().getCertificateKeyStoreFile()) {
                //私钥生成
                byte[] encryptByte = "".getBytes();
                try {
                    //解密私钥
                    encryptByte = GMCrypto.decrypt(new String(entry.getValue().getCertificateKeyStoreFile(), "UTF-8")).getBytes();
                } catch (Exception e) {

                }
                java.security.PrivateKey privateKey = GMCertificateUtil.getPrivateKey(encryptByte);
                peerCertificateCipherReq.setPrivateKey(privateKey);
            }
            try {
                //生成证书
                X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                        entry.getValue().getCertificateCerFile());
                peerCertificateCipherReq.setX509Certificate(receivedCertificate);
            } catch (Exception e) {
                log.error("OsccaSecurityServiceImpl.transferPeerCertificateCipherReqMap：X509Certificate is error:", e);
            }
            peerCertificateCipherReqHashMap.put(entry.getKey(), peerCertificateCipherReq);
            //System.out.println(entry.getKey() + ":" + entry.getValue());
        }

        //peerCertificateCipherReqMap=peerCertificateCipherReqHashMap;

    }


    /**
     * 历史证书校验
     *
     * @param peerId
     * @param channelId
     * @param decodedText
     * @param receivedignature
     * @return
     */
    public boolean historyGMCertificateVerify(String peerId, String channelId, byte[] decodedText, final byte[] receivedignature) {
        List<PeerCert> peerCert = ledgerMgr.getPeerCertList(peerId, channelId);
        //查询历史证书
        if (CollectionUtils.isEmpty(peerCert)) {
            return false;
        }
        try {
            for (int i = 0; i < peerCert.size(); i++) {
                if (peerCert.get(i).getFlag().equals("0")) continue;
                //生成证书
                X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(peerCert.get(i).getCertificateCerFile());
                boolean flag = GMCertificateUtil.verifyByGM(receivedCertificate.getPublicKey(), decodedText, receivedignature);
                if (flag) return true;
            }
            return false;
        } catch (Exception e) {
            log.error(ModuleClassification.SM_OSSI_ + "TError" + "OsccaSecurityServiceImpl.historyGMCertificateVerify：CertificateVerifySignature is error:", e);
            return false;
        }
    }


}
