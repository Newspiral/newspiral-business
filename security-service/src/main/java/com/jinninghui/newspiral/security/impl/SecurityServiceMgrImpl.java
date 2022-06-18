package com.jinninghui.newspiral.security.impl;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificateCipher;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/13 19:31
 */
@Slf4j
public class SecurityServiceMgrImpl implements SecurityServiceMgr {

    private Map<String,SecurityService> securityServiceHashMap = new HashMap<>();

    @SofaReference
    private LedgerMgr ledgerMgr;

    @Override
    public SecurityService getMatchSecurityService(String key) {
        SecurityService matchObject = securityServiceHashMap.get(key);
        if(matchObject==null)
        {
            log.warn(ModuleClassification.SM_SSMI_ +"TError"+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",根据输入Key"+key+"找不到对应的SecurityService，所以已经加载的SecurityService:"+securityServiceHashMap.keySet().toString());
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM,"根据SecurityService的key"+key+"找不到对应的SecurityService,一般是Channel配置的安全服务Key错误");
        }
        else
        {
            return matchObject;
        }
    }

    /**
     * 完成该Mgr管理的所有Security服务的初始化，框架会在初始化Bean的时候调用此方法
     * 外部如果使用其他方式，则应保证
     */
    @Override
    public void init() {
        try {
            //log.error(ModuleClassification.SM_SSMI+"TError"+"SecurityServiceMgrImpl的init方法:"+NewSpiralErrorEnum.UN_IMPLEMENTED.toString());
            OsccaSecurityServiceImpl osccaSecurityServiceImpl = new OsccaSecurityServiceImpl();
            Identity localIdentity = ledgerMgr.queryLocalIdentity();
            //Map<String, Map<String,String>> peerPrivateKeyMap=ledgerMgr.queryPeerPrivateKeyMap();
            //Map<String, String> channelPublicKeyMap=ledgerMgr.queryChannelPublicKeyMap();
            //节点证书初始化
            Map<String, Map<String, PeerCertificateCipher>> peerChannelCertificateCipherMap = ledgerMgr.querypeerChannelCertificateCipherMap();
            osccaSecurityServiceImpl.init(localIdentity, peerChannelCertificateCipherMap, ledgerMgr);
            log.info(ModuleClassification.SM_SSMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",SecurityServiceMgrImpl.name=" + osccaSecurityServiceImpl.getName());
            this.securityServiceHashMap.put(osccaSecurityServiceImpl.getName(), osccaSecurityServiceImpl);
        }
        catch(Exception ex)
        {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",SecurityServiceMgrImpl初始化异常,这会导致系统无法正常工作，退出系统:", ex);
            System.exit(1);
        }
    }
}
