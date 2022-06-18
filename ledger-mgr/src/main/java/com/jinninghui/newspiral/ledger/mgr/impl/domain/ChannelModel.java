package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberAddChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberModifyChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberRemoveChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.PeerAddChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleAddStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleDelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleModifyStrategyEnum;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractDeplyStrategyEnum;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

public class ChannelModel {

    private Long id;

    private String channelId;

    private String name;

    private Long blockMaxSize;

    private Long blockMaxInterval;

    private Integer maxPeerCount;

    private String peerAddStrategy;

    private String modifyStrategy;

    private String scDepolyStrategy;

    private String consensusAlgorithm;

    private Long txPoolSize;

    private Long allowTimeErrorSeconds;

    private String securityServiceKey;

    private String extendsParams;

    private Long maxTransactionCount;

    private Integer available;

    private Date createTimestamp;

    private String organizationId;

    private String memberAddStrategy;
    private String memberModifyStrategy;
    private String memberDelStrategy;

    private String roleAddStrategy;
    private String roleDelStrategy;
    private String roleModifyStrategy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBlockMaxSize() {
        return blockMaxSize;
    }

    public void setBlockMaxSize(Long blockMaxSize) {
        this.blockMaxSize = blockMaxSize;
    }

    public Long getBlockMaxInterval() {
        return blockMaxInterval;
    }

    public void setBlockMaxInterval(Long blockMaxInterval) {
        this.blockMaxInterval = blockMaxInterval;
    }

    public Integer getMaxPeerCount() {
        return maxPeerCount;
    }

    public void setMaxPeerCount(Integer maxPeerCount) {
        this.maxPeerCount = maxPeerCount;
    }

    public String getPeerAddStrategy() {
        return peerAddStrategy;
    }

    public void setPeerAddStrategy(String peerAddStrategy) {
        this.peerAddStrategy = peerAddStrategy;
    }

    public String getModifyStrategy() {
        return modifyStrategy;
    }

    public void setModifyStrategy(String modifyStrategy) {
        this.modifyStrategy = modifyStrategy;
    }

    public String getScDepolyStrategy() {
        return scDepolyStrategy;
    }

    public void setScDepolyStrategy(String scDepolyStrategy) {
        this.scDepolyStrategy = scDepolyStrategy;
    }

    public String getConsensusAlgorithm() {
        return consensusAlgorithm;
    }

    public void setConsensusAlgorithm(String consensusAlgorithm) {
        this.consensusAlgorithm = consensusAlgorithm;
    }

    public Long getTxPoolSize() {
        return txPoolSize;
    }

    public void setTxPoolSize(Long txPoolSize) {
        this.txPoolSize = txPoolSize;
    }

    public Long getAllowTimeErrorSeconds() {
        return allowTimeErrorSeconds;
    }

    public void setAllowTimeErrorSeconds(Long allowTimeErrorSeconds) {
        this.allowTimeErrorSeconds = allowTimeErrorSeconds;
    }

    public String getSecurityServiceKey() {
        return securityServiceKey;
    }

    public void setSecurityServiceKey(String securityServiceKey) {
        this.securityServiceKey = securityServiceKey;
    }

    public String getExtendsParams() {
        return extendsParams;
    }

    public void setExtendsParams(String extendsParams) {
        this.extendsParams = extendsParams;
    }

    public Long getMaxTransactionCount() {
        return maxTransactionCount;
    }

    public void setMaxTransactionCount(Long maxTransactionCount) {
        this.maxTransactionCount = maxTransactionCount;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Date createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getMemberAddStrategy() {
        return memberAddStrategy;
    }

    public void setMemberAddStrategy(String memberAddStrategy) {
        this.memberAddStrategy = memberAddStrategy;
    }

    public String getMemberDelStrategy() {
        return memberDelStrategy;
    }

    public void setMemberDelStrategy(String memberDelStrategy) {
        this.memberDelStrategy = memberDelStrategy;
    }

    public String getRoleAddStrategy() {
        return roleAddStrategy;
    }

    public void setRoleAddStrategy(String roleAddStrategy) {
        this.roleAddStrategy = roleAddStrategy;
    }

    public String getRoleDelStrategy() {
        return roleDelStrategy;
    }

    public void setRoleDelStrategy(String roleDelStrategy) {
        this.roleDelStrategy = roleDelStrategy;
    }

    public String getRoleModifyStrategy() {
        return roleModifyStrategy;
    }

    public void setRoleModifyStrategy(String roleModifyStrategy) {
        this.roleModifyStrategy = roleModifyStrategy;
    }

    public String getMemberModifyStrategy() {
        return memberModifyStrategy;
    }

    public void setMemberModifyStrategy(String memberModifyStrategy) {
        this.memberModifyStrategy = memberModifyStrategy;
    }

    /**
     * 并未设置节点列表、智能合约列表
     *
     * @return
     */
    public Channel toChannel() {
        Channel channel = new Channel();

        channel.setAllowTimeErrorSeconds(this.getAllowTimeErrorSeconds());
        channel.setBlockMaxInterval(this.getBlockMaxInterval());
        channel.setBlockMaxSize(this.getBlockMaxSize());
        channel.setChannelId(this.getChannelId());
        channel.setConsensusAlgorithm(JSON.parseObject(this.getConsensusAlgorithm(), ConsensusAlgorithmEnum.class));
        channel.setExtendsParams(JSON.parseObject(this.getExtendsParams(), Map.class));
        channel.setMaxPeerCount(this.getMaxPeerCount());
        channel.setModifyStrategy(JSON.parseObject(this.getModifyStrategy(), ChannelModifyStrategyEnum.class));
        channel.setName(this.getName());
        channel.setPeerAddStrategyEnum(JSON.parseObject(this.getPeerAddStrategy(), PeerAddChannelStrategyEnum.class));
        channel.setSmartContractDeplyStrategy(JSON.parseObject(this.getScDepolyStrategy(), SmartContractDeplyStrategyEnum.class));
        channel.setSecurityServiceKey(this.getSecurityServiceKey());
        channel.setTxPoolSize(this.getTxPoolSize());
        channel.setMaxTransactionCount(this.maxTransactionCount);
        channel.setAvailable(this.available);
        channel.setCreateTimestamp(this.getCreateTimestamp().getTime());
        channel.setOrganizationId(this.organizationId);
        channel.setMemberAddChannelStrategyEnum(JSON.parseObject(this.memberAddStrategy, MemberAddChannelStrategyEnum.class));
        channel.setMemberRemoveChannelStrategyEnum(JSON.parseObject(this.memberDelStrategy, MemberRemoveChannelStrategyEnum.class));
        if (StringUtils.isEmpty(this.roleAddStrategy)) {
            channel.setRoleAddStrategyEnum(RoleAddStrategyEnum.MANAGER_AGREE);
        } else {
            channel.setRoleAddStrategyEnum(JSON.parseObject(this.roleAddStrategy, RoleAddStrategyEnum.class));
        }

        if (StringUtils.isEmpty(this.roleDelStrategy)) {
            channel.setRoleDelStrategyEnum(RoleDelStrategyEnum.MANAGER_AGREE);
        } else {
            channel.setRoleDelStrategyEnum(JSON.parseObject(this.roleDelStrategy, RoleDelStrategyEnum.class));
        }

        if (StringUtils.isEmpty(this.roleModifyStrategy)) {
            channel.setRoleModifyStrategyEnum(RoleModifyStrategyEnum.MANAGER_AGREE);
        } else {
            channel.setRoleModifyStrategyEnum(JSON.parseObject(this.roleModifyStrategy, RoleModifyStrategyEnum.class));
        }

        if (StringUtils.isEmpty(this.memberModifyStrategy)) {
            channel.setMemberModifyChannelStrategyEnum(MemberModifyChannelStrategyEnum.MANAGER_AGREE);
        } else {
            channel.setMemberModifyChannelStrategyEnum(JSON.parseObject(this.memberModifyStrategy, MemberModifyChannelStrategyEnum.class));
        }

        return channel;
    }

    /**
     * 不用带参构造函数
     *
     * @param c
     * @return
     */
    public static ChannelModel createInstance(Channel c) {
        ChannelModel model = new ChannelModel();
        model.setAllowTimeErrorSeconds(c.getAllowTimeErrorSeconds());
        model.setBlockMaxInterval(c.getBlockMaxInterval());
        model.setBlockMaxSize(c.getBlockMaxSize());
        model.setChannelId(c.getChannelId());
        model.setConsensusAlgorithm(JSON.toJSONString(c.getConsensusAlgorithm()));
        model.setExtendsParams(JSON.toJSONString(c.getExtendsParams()));
        model.setMaxPeerCount(c.getMaxPeerCount());
        model.setModifyStrategy(JSON.toJSONString(c.getModifyStrategy()));
        model.setName(c.getName());
        model.setPeerAddStrategy(JSON.toJSONString(c.getPeerAddStrategyEnum()));
        model.setScDepolyStrategy(JSON.toJSONString(c.getSmartContractDeplyStrategy()));
        model.setSecurityServiceKey(c.getSecurityServiceKey());
        model.setTxPoolSize(c.getTxPoolSize());
        model.setMaxTransactionCount(c.getMaxTransactionCount());
        model.setAvailable(c.getAvailable());
        model.setCreateTimestamp(c.getCreateTimestamp() == null ? new Date() : new Date(c.getCreateTimestamp()));
        model.setOrganizationId(c.getOrganizationId());
        model.setMemberAddStrategy(JSON.toJSONString(c.getMemberAddChannelStrategyEnum()));
        model.setMemberDelStrategy(JSON.toJSONString(c.getMemberRemoveChannelStrategyEnum()));

        model.setRoleAddStrategy(JSON.toJSONString(c.getRoleAddStrategyEnum()));
        model.setRoleDelStrategy(JSON.toJSONString(c.getRoleDelStrategyEnum()));
        model.setRoleModifyStrategy(JSON.toJSONString(c.getRoleModifyStrategyEnum()));

        model.setMemberModifyStrategy(JSON.toJSONString(c.getMemberModifyChannelStrategyEnum()));
        return model;
    }

}
