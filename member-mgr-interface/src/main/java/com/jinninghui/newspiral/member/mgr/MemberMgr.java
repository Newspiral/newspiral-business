package com.jinninghui.newspiral.member.mgr;

import com.jinninghui.newspiral.common.entity.chain.*;
import com.jinninghui.newspiral.common.entity.common.base.BaseResponse;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;

/**
 * @author lida
 * @date 2019/7/15 9:50
 * 成员管理模块需要对外提供的接口定义
 */
public interface MemberMgr {

    /**
     * 根据输入参数创建一条通道，通道中仅包含一个节点，返回该通道的通道ID
     * 根证书才可以创建
     * @param initParams
     * @return
     */
    CreateChannel createChannel(ChannelInitParams initParams);

    /**
     * 根据通道ID查询通道信息，如果不存在，抛出异常
     * 注意仅根据本地已持久化的数据查询
     * @param channelId
     * @return
     */
    Channel queryChannel(String channelId);

    /**
     * 根据通道的配置信息，创建通道的创世块，前提是通道相关的库表已经创建完毕
     * @param channel
     */
    void createGenesisBlockForNewChannel(Channel channel);

    /**
     * 将本节点加入到某个已经存在的通道，调用此接口需要保证节点管理平台已经收集到了足够多该通道内已有成员的approvol
     * 添加自己到某个通道是一个特殊的系统交易调用，因为本节点并不在通道中，因此有此单独方法，而不走通用的系统交易调用流程
     * @param request
     */
    BaseResponse addMySelf2Channel(AddMySelfToChannelRequest request);

    /**
     * remove the given peer from the channel, with enough approvals from members in the channel.
     * @param removePeerFromChannelRequest
     */
    String removePeerFromChannel(RemovePeerFromChannelRequest removePeerFromChannelRequest);

    /**
     * 用于本节点新增组织身份，同时将该身份通知给所有同通道节点。该接口供节点管理平台使用，需使用根证书发起调用
     */
    void addMyIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity);

    /**
     * 用于本节点删除组织身份，同时将该身份通知给所有同通道节点。该接口供节点管理平台使用，需使用根证书发起调用
     * @param identityKey
     */
    void deleteMyIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity);

    /**
     * 新节点加入通道，供其他节点调用，这个接口成功调用，说明已经接受该请求
     * 加入是否成功，需要调用queryChannel接口来确定
     * @param peerAddChannelTransaction
     */
    BaseTransHashResp addNewPeer2Channel(SDKTransaction peerAddChannelTransaction);


    String removePeerFromChannel(SDKTransaction rmPeerTransaction);

    /**
     * 添加其他组织的身份，供其他节点调用
     * @param identityKey
     * @param callerIdentity
     */
    void addOrgIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity);


    /**
     * 删除其他组织的身份,供其他节点调用
     * @param identityKey
     * @param callerIdentity
     */
    void deleteOrgIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity);

    /**
     * peer state
     * @return
     */
    boolean getPeerState();

    /**
     * 初始化通道 当前节点并进入通道
     * @param channel
     */
    void initChannel(Channel channel);
}
