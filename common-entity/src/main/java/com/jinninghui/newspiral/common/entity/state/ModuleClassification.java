package com.jinninghui.newspiral.common.entity.state;

/**
 * 模块分类
 * A_B
 * A为模块名
 * B为类名缩写
 */
public enum ModuleClassification {

    /**
     * 共识模块_hotstuff处理
     * NewSpiralHotStuffProcessor
     * com.jinninghui.newspiral.consensus.impl.hotstuff;
     */
    ConM_NSHP_,


    /**
     * 共识模块_共识消息处理
     * ConsensusMsgProcessorImpl
     * com.jinninghui.newspiral.consensus.impl.hotstuff;
     */
    ConM_CMPI_,

    /**
     * 共识上下文
     * ConsensusContext
     * com.jinninghui.newspiral.consensus.impl.hotstuff;
     */
    ConM_CC_,

    /**
     * 共识同步
     * NewSpiralSyncHistoryBlockProcessor
     * com.jinninghui.newspiral.consensus.impl.hotstuff;
     */
    ConM_Sync_,

    /**
     * 共识或者同步校验
     */
    ConM_Verify_,

    /**
     * 通道信息
     * Channel
     * com.jinninghui.newspiral.common.entity.chain;
     */
    CE_CHANNEL_,

    /**
     * 交易模块
     * SmartContractAspect
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_SCA_,

    /**
     * 交易模块
     * SmartContractCache
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_SCC_,

    /**
     * 交易模块
     * SmartContractClassLoader
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_SCCL_,

    /**
     * 交易模块
     * SystemSmartContract
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_SSC_,

    /**
     * 交易模块
     * TxExecutor
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_TxE_,

    /**
     * 交易模块
     * TxExecutorConsensusCache
     * com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;
     */
    TxM_TxECC_,

    /**
     * 交易模块
     * TransactionMgrImpl
     * com.jinninghui.newspiral.transaction.mgr.impl;
     */
    TxM_TMI_,

    /**
     * 交易模块
     * TransactionPool
     * com.jinninghui.newspiral.transaction.mgr.impl;
     */
    TxM_TP_,

    /**
     * 账本模块
     * LedgerMgrImpl
     * com.jinninghui.newspiral.ledger.mgr.impl;
     */
    LedM_LMI_,
    /**
     * PersistBlockProcessor
     */
    LedM_PBP_,

    /**
     * 账本模块
     * MemberLedgerMgrImpl
     * com.jinninghui.newspiral.ledger.mgr.impl;
     */
    LedM_MLI_,

    /**
     * 账本模块
     * SmartContractMgrImpl
     * com.jinninghui.newspiral.ledger.mgr.impl;
     */
    LedM_SCMI_,

    /**
     * p2p模块
     * ServiceForPeerClientMgr
     * com.jinninghui.newspiral.p2p.impl;
     */
    P2PM_SFPCM_,

    /**
     * p2p模块
     * ChannelClientMgr
     * com.jinninghui.newspiral.p2p.impl.hotstuff;
     */
    P2PM_CCM_,

    /**
     * p2p模块
     * ServiceForPeerClient
     * com.jinninghui.newspiral.p2p.impl.hotstuff;
     */
    P2PM_SFPC_,

    /**
     * 安全模块
     * Crypto
     * com.jinninghui.newspiral.security.impl;
     */
    SM_CPO_,

    /**
     * 安全模块
     * DataSecurityMgrImpl
     * com.jinninghui.newspiral.security.impl;
     */
    SM_DSMI_,

    /**
     * 安全模块
     * OsccaSecurityServiceImpl
     * com.jinninghui.newspiral.security.impl;
     */
    SM_OSSI_,

    /**
     * 安全模块
     * SecurityServiceMgrImpl
     * com.jinninghui.newspiral.security.impl;
     */
    SM_SSMI_,

    /**
     * 安全模块
     * SM9Mgrlmpl
     * com.jinninghui.newspiral.security.impl;
     */
    SM_SM9MI_,

    /**
     * 安全模块
     * StartSecurityMgrImpl
     * com.jinninghui.newspiral.security.impl;
     */
    SM_STSMI_,

    /**
     * 安全模块
     * OsccaCinpher
     * com.jinninghui.newspiral.security.oscca;
     */
    SM_OC_,

    /**
     * 安全模块
     * CertificateTest
     * com.jinninghui.newspiral.security.utils;
     */
    SM_CT_,

    /**
     * 安全模块
     * CertificateUtil
     * com.jinninghui.newspiral.security.utils;
     */
    SM_CU_,

    /**
     * 安全模块
     * GMCertificateUtil
     * com.jinninghui.newspiral.security.utils;
     */
    SM_GMCU_,

    /**
     * 安全模块
     * RSAUtil
     * com.jinninghui.newspiral.security.utils;
     */
    SM_RSAU_,

    /**
     * 传输模块
     */
    TransM_,

    /**
     * Peer模块
     * ServiceForPeerImpl
     * com.jinninghui.newspiral.gateway;
     */
    Peer_SFPI_,

    /**
     * Peer模块
     * ServiceForSDKImpl
     * com.jinninghui.newspiral.gateway;
     */
    Peer_SFSI_,

    /**
     * Peer模块
     * TransactionAdapter
     * com.jinninghui.newspiral.gateway.adapter
     */
    Peer_TA_,

    /**
     * Peer模块
     * ApplicationReadyEventListener
     * com.jinninghui.newspiral.gateway.config;
     */
    Peer_AREL_,

    /**
     * Peer模块
     * AuthRoleTokenAspect
     * com.jinninghui.newspiral.gateway.config;
     */
    Peer_ARTA_,

    /**
     * Peer模块
     * GracefulShutdown
     * com.jinninghui.newspiral.gateway.config;
     */
    Peer_GS_,

    /**
     * Peer模块
     * SignalConfig
     * com.jinninghui.newspiral.gateway.config;
     */
    Peer_SC_,

    /**
     * Peer模块
     * SignAspect
     * com.jinninghui.newspiral.gateway.config;
     */
    Peer_SA_,

    /**
     * Peer模块
     * ServiceForSDKController
     * com.jinninghui.newspiral.gateway.controller;
     */
    Peer_SFSC_,

    /**
     * Peer模块
     * PeerStateService
     * com.jinninghui.newspiral.gateway.service;
     */
    Peer_PSS_,

    /**
     * Peer模块
     * StopPeerService
     * com.jinninghui.newspiral.gateway.service;
     */
    Peer_SPS_,

    /**
     * Peer模块
     * GMCertificateUtil
     * com.jinninghui.newspiral.gateway.signTest;
     */
    Peer_GCU_,

    /**
     * Peer模块
     * HashUtil
     * com.jinninghui.newspiral.gateway.signTest;
     */
    Peer_HU_,

    /**
     * Peer模块
     * OsccaCinpher
     * com.jinninghui.newspiral.gateway.signTest;
     */
    Peer_OC_,

    /**
     * Peer模块
     * PeerDateVerifyMth
     * com.jinninghui.newspiral.gateway.verify
     */
    Peer_PDVM_,

    /**
     * Peer模块
     * VerifyAuthAspect
     * com.jinninghui.newspiral.gateway.aspect
     */
    Peer_VAA_,
}
