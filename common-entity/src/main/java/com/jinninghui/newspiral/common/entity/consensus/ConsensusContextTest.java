package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: ConsensusContextTest
 * @Package com.jinninghui.newspiral.common.entity.common
 * @Description:
 * @author: xuxm
 * @date: 2020/2/25 16:00
 */
@ApiModel(description = "共识上下文实体测试")
@Data
public class ConsensusContextTest  {

    /**
     * 初始化为最大int值
     * 某个QC需要收集到的最小节点支持数，适用于GenericQC中的BlockVoteMsg，也适用于NewViewMsg
     */
    @ApiModelProperty(value = "某个QC需要收集到的最小节点支持数")
    int qcMinNodeCnt;

    /**
     * 上一次超时视图序号
     */
    @ApiModelProperty(value = "上一次超时视图序号")
    Long latestTimeOutViewNo;

    /**
     * 当前超时时间
     */
    @ApiModelProperty(value = "当前超时时间")
    Long currentTimeOut;

    /**
     * 共识所属的通道
     */
    @ApiModelProperty(value = "共识所属通道")
    Channel channel;

    /**
     * 通道中的各个节点的序号，从0开始，逐一增加，因此在某个节点退出通道（并非指宕机，而是指需要共识的退出），此处需要重新计算各个节点的序号
     */
    @ApiModelProperty(value = "共识通道中有序的节点列表")
    List<Peer> orderedPeerList;

    /**
     * 节点证书黑名单,共识验证消息时应该排除
     */
    @ApiModelProperty(value = "节点证书黑名单")
    List<Peer> peerCertificateBlackList;

    /**
     * 节点自身
     */
    @ApiModelProperty(value = "节点自身")
    Peer myself;

    /**
     * 当前leader
     */
    @ApiModelProperty(value = "当前leader")
    Peer currLeader;

    /**
     * 当前view
     */
    @ApiModelProperty(value = "当前视图")
    View currView;

    /**
     * View number from genericMsg, only used in synchronization.
     */
    @ApiModelProperty(value = "同步视图号")
    Long viewNoSync = new Long(0);

    /**
     * Confirm times about viewNoSync from successive leader.
     * Only used in synchronization.
     */
    @ApiModelProperty(value = "下一任leader对于同步视图号的确认次数")
    Integer viewNoSyncConfirmTimes = new Integer(0);

    /**
     * current height of committed block
     */
    @ApiModelProperty(value = "已确认区块高度")
    long blockHeight;

    /**
     * 本节点所处的共识阶段
     */
    @ApiModelProperty(value = "本节点所处的共识阶段")
    ConsensusStageEnum consensusStageEnum;


    /**
     * 收到的本view的genericMsg
     * 如果本view中本节点是Leader，则存储的是本节点创建和广播的GenericMsg
     * 接收消息线程和主线程都会使用，但是不会有什么内部修改的动作，仅有赋值和取值动作，所以线程安全
     */
    @ApiModelProperty(value = "收到的当前视图的genericMsg")
    GenericMsg genericMsgOfCurrView;


    /**
     * 同步线程与共识线程处理过程中，持续接收收到的缓存区块消息队列，最终清0
     */
    @ApiModelProperty(value ="同步线程与共识线程处理过程中，持续接收收到的缓存区块消息队列，最终清0" )
    Map<Long, GenericMsg> genericMsgMap;

    @ApiModelProperty(value ="genericMsg缓存区块消息队列备份",hidden = true)
    Map<Long, GenericMsg> genericMsgMapBackup;

    @ApiModelProperty(value ="genericMsg最大缓存数量",hidden = true)
    Integer maxCachedGenericMsg = 20;

    /**
     * 支持genericMsgOfCurrView中的Block的副本投票消息，只有本节点是Leader时，才有数据
     * 所有加入到这里的数据都是经过检查的合法投票
     * 接收消息线程和主线程都会使用，因此使用ConcurrentHashMap
     * key为BlockVoteMsg的业务键，即如果业务键相同，则BlockVoteMsg也相同
     */
    @ApiModelProperty(value = "支持genericMsgOfCurrView中的Block的副本投票消息，只有本节点是Leader时，才有数据")
    Map<String, BlockVoteMsg> blockVoteMapOfCurrView;

    @ApiModelProperty(value = "区块投票Map",hidden=true)
    Map<String, Map<String, BlockVoteMsg>> blockVoteMap;
    /**
     * 本地收到的(GenericMsg)或自身生成的最大的合法genericQC，正常情况下是上一个view的Block的GenriceQC
     * 异常情况下则不是
     */
    @ApiModelProperty(value = "本地收到的(GenericMsg)或自身生成的最大的合法genericQC，正常情况下是上一个view的Block的GenriceQC")
    GenericQC genericQC;

    @ApiModelProperty(value = "形成two-chain,对本地的genericQc进行lock")
    GenericQC lockedQC;

    @ApiModelProperty(value = "newViewMsg中，选择最大的genericQc，为highestQc")
    GenericQC highestQC;

    /**
     * the hash of block which is voted by the peer, this block and the genericQC is of the same HotStuffDataNode.
     */
    @ApiModelProperty(value = "genericQc所认证区块的hash值")
    String hashPrePrepareBlock;

    /**
     * 接收消息线程和主线程都会使用，因此使用ConcurrentHashMap
     * 第一层key使用viewNo，第二层key使用NewViewMsg的业务键
     */
    @ApiModelProperty(value = "newViewMsgMap",hidden = true)
    Map<Long,Map<String, NewViewMsg>> newViewMsgMap;

    /**
     * key为HotStuffDataNode的block的Hash的16进制字符串表示
     * byte[]不能直接作为Map的key
     */
    @ApiModelProperty(value = "block对应的本地节点信息")
    Map<String, HotStuffDataNode> localDataNodeMap;

    long waitExecutedPoolSize;
}
