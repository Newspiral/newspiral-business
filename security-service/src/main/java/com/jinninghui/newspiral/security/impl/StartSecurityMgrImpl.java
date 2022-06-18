package com.jinninghui.newspiral.security.impl;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificateCipher;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.security.StartSecurityMgr;
import com.jinninghui.newspiral.security.utils.CertificateUtil;
import com.jinninghui.newspiral.security.utils.DateUtil;
import com.jinninghui.newspiral.security.utils.GMCertificateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: StartSecurityMgrlmpl
 * @Package com.jinninghui.newspiral.security.impl
 * @Description:
 * @author: xuxm
 * @date: 2019/11/8 14:21
 */
//@Component
@Slf4j
//@Configuration
//@PropertySource("classpath:security.properties")
public class StartSecurityMgrImpl implements StartSecurityMgr {

    //@Value("${start.key.action}")
    private String startKeyAction;

    /*
     */
/**
 * key1为对端节点的身份标识,不给get/set方法,存储证书相关信息
 *//*

    Map<String, PeerCertificateCipher> peerCertificateCipherMap = new HashMap<>();
*/

    /**
     * 密钥缓存 key1为channelId,key2为节点的身份标识
     */
    Map<String, Map<String, PeerCertificateCipher>> peerChannelCertificateCipherMap = new HashMap<>();

    /**
     * 本节点的身份标识
     */
    Identity localIdentity;

    @SofaReference
    private LedgerMgr ledgerMgr;

    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;

    @Override
    public void init() {
        try {
            this.localIdentity = ledgerMgr.queryLocalIdentity();
            this.peerChannelCertificateCipherMap = ledgerMgr.querypeerChannelCertificateCipherMap();
        }
        catch(Exception ex)
        {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",StartSecurityMgrImpl初始化异常,这会导致系统无法正常工作，退出系统:", ex);
            System.exit(1);
        }
    }

    @Override
    public boolean getStarSecurityFlag() {
        if (StringUtils.isEmpty(startKeyAction)) return false;
        //明文格式例如：2013-01-01^2222-01-01^127.0.0.1^peerId
        String[] decryptStr = Crypto.decrypt(startKeyAction).split("\\^");
        log.info("许可时间段：" + decryptStr[0] + "~" + decryptStr[1]);
        String currDate = DateUtil.convert2Str(new Date(), DateUtil.FORMAT_SIMPLE);
        //判断时间是否在当前时间内
        if (currDate.compareTo(decryptStr[0]) < 0
                || currDate.compareTo(decryptStr[1]) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean initVerifyCertificateValidity(String channnelId) {
        log.info("StartSecurityMgrlmpl.initVerifyCertificateValidity:start");
        PeerCertificateCipher peerCertificateCipher = peerChannelCertificateCipherMap.get(channnelId).get(localIdentity.getKey().getValue());
        if (null == peerCertificateCipher ||
                StringUtils.isEmpty(peerCertificateCipher.getCertificateAlias()) ||
                StringUtils.isEmpty(peerCertificateCipher.getCertificateStorePass()) ||
                null == peerCertificateCipher.getCertificateKeyStoreFile() ||
                null == peerCertificateCipher.getCertificateCerFile()) {
            log.error(ModuleClassification.SM_STSMI_ +"StartSecurityMgrlmpl.initVerifyCertificateValidity：Certificate parameter(PeerCertificateCipher) is error,para:{}", peerCertificateCipher);
            return false;
        }
        try {
            //密钥库生成
            KeyStore keyStore = CertificateUtil.getKeyStore(peerCertificateCipher.getCertificateStorePass(), peerCertificateCipher.getCertificateKeyStoreFile());
            //密钥库证书生成
            X509Certificate certificate = CertificateUtil.getCertificateByKeystore(keyStore, peerCertificateCipher.getCertificateAlias());
            //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(certificate)) {
                log.error(ModuleClassification.SM_STSMI_ +"StartSecurityMgrlmpl.initVerifyCertificateValidity：KeyStoreCertificate is invalid");
                return false;
            }
            //CA生成证书
            X509Certificate receivedCertificate = CertificateUtil.getCertificateByCertByte(
                    peerCertificateCipher.getCertificateCerFile(), CertificateUtil.CERT_TYPE);
            //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                log.error(ModuleClassification.SM_STSMI_ +"StartSecurityMgrlmpl.initVerifyCertificateValidity：CACertificate is invalid");
                return false;
            }
        } catch (Exception e) {
            log.error(ModuleClassification.SM_STSMI_ +"TError"+"StartSecurityMgrlmpl.initVerifyCertificateValidity：initVerifyCertificateValidity is error:", e);
            return false;
        }
        log.info("StartSecurityMgrlmpl.initVerifyCertificateValidity:end");
        return true;
    }

    @Override
    public boolean initVerifyGMCertificateValidity(String channelId) {
        log.info("StartSecurityMgrlmpl.initVerifyGMCertificateValidity,start");
        PeerCertificateCipher peerCertificateCipher = new PeerCertificateCipher();
        if (!StringUtils.isEmpty(channelId) && null != peerChannelCertificateCipherMap.get(channelId)) {
            peerCertificateCipher = peerChannelCertificateCipherMap.get(channelId).get(localIdentity.getKey().getValue());
        } else {
            Peer peer = ledgerMgr.queryLocalPeer();
            peerCertificateCipher.setCertificateKeyStoreFile(peer.getCertificateKeyStoreFile());
            peerCertificateCipher.setCertificateCerFile(peer.getCertificateCerFile());
            peerCertificateCipher.setPeerId(peer.getPeerId().getValue());
        }
        if (null == peerCertificateCipher ||
                null == peerCertificateCipher.getCertificateKeyStoreFile() ||
                null == peerCertificateCipher.getCertificateCerFile()) {
            log.error(ModuleClassification.SM_STSMI_ +"StartSecurityMgrlmpl.initVerifyGMCertificateValidity：GMCertificate parameter(PeerCertificateCipher) is error,para:{}", peerCertificateCipher);
            return false;
        }

        try {
            //CA生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    peerCertificateCipher.getCertificateCerFile());
            //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                log.error(ModuleClassification.SM_STSMI_ +"StartSecurityMgrlmpl.initVerifyGMCertificateValidity：CACertificate is invalid");
                return false;
            }
        } catch (Exception e) {
            log.error(ModuleClassification.SM_STSMI_ +"TError"+"StartSecurityMgrlmpl.initVerifyGMCertificateValidity：initVerifyCertificateValidity is error:", e);
            return false;
        }
        log.info("StartSecurityMgrlmpl.initVerifyGMCertificateValidity,end");
        return true;
    }

    @Override
    public void checkAllCertificateValidity() {
        List<Peer> peerList = ledgerMgr.queryAllPeers();
        List<Peer> invalidPeers = new ArrayList<>();
        for (Peer peer : peerList) {
            try {
                //CA生成证书
                X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                        peer.getCertificateCerFile());
                if (!CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                    invalidPeers.add(peer);
                }
            } catch (Exception e) {
                invalidPeers.add(peer);
                log.error("TError"+"invalidPeers",e);

            }
        }
        List<Member> members = memberLedgerMgr.getAllMemberList();
        List<Member> invalidMembers = new ArrayList<>();
        for (Member member : members) {
            try {
                //CA生成证书
                X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                        member.getCertificateCerFile().getBytes());
                if (!CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                    invalidMembers.add(member);
                }
            } catch (Exception e) {
                invalidMembers.add(member);
                log.error("TError"+"invalidMembers",e);
            }
        }

        //TODO 目前先打印ERROR日志提醒吧
        for (Peer peer:invalidPeers)
        {
            log.error(ModuleClassification.SM_STSMI_ +"Peer CA checkValidity is invalid,peerId={}",peer.getPeerId().getValue());
        }
        for (Member member:invalidMembers)
        {
            log.error(ModuleClassification.SM_STSMI_ +"Member CA checkValidity is invalid,memberId={}",member.getId());
        }

/*        if(!CollectionUtils.isEmpty(invalidPeers)||!CollectionUtils.isEmpty(invalidMembers)) {
            ledgerMgr.processInvalidCertificates(invalidPeers, invalidMembers);
        }*/
    }
}
