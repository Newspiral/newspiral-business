package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.common.persist.Action;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractsAuthorized;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author lida
 * @date 2019/7/11 17:28
 * 通道信息，当前某条通道的基础信息
 */
@ApiModel(description = "通道信息")
@Slf4j
@Data
public class Channel extends ChannelBasicParams implements Serializable {
    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;

    /**
     * 最新的通道改变高度
     */
    @ApiModelProperty(value = "最新的通道发生改变时的高度值")
    private Long latestChannelChangeHeight;

    /**
     * 节点成员列表，当前通道中包含的节点列表，包括已经被移除部分，每次取得时候要根据高度来取合法部分
     */
    @ApiModelProperty(value = "通道节点成员列表")
    private List<Peer> memberPeerList = new ArrayList<>();

    /**
     * 通道上的智能合约列表
     */
    @ApiModelProperty(value = "通道上的智能合约列表")
    private List<SmartContract> smartContractList = new ArrayList<>();

    /**
     * 角色信息列表
     */
    @ApiModelProperty(value = "角色信息列表")
    private List<Role> roles = new ArrayList<>();

    /**
     * 成员信息列表
     */
    @ApiModelProperty(value = "成员信息列表")
    private List<Member> members = new ArrayList<>();

    @ApiModelProperty(value = "通道是否可用")
    private Integer available = new Integer(1);

    /**
     * 合约授权，一般都是为空，方便共识后批处理
     */
    @ApiModelProperty(value = "合约授权")
    private SmartContractsAuthorized smartContractsAuthorized;
    /**
     * 通道中的变更
     */
    @ApiModelProperty(value = "通道变更")
    private ChannelChange channelChange;

    /**
     * 用于存放initBlock
     */
    private List<Block> blockList;

    /**
     * 根据输入参数创建一个Channel对象，该Channel仅包含一个本地节点一个节点
     *
     * @param params
     * @param localPeer
     * @return
     */
    public static Channel createInstance(ChannelInitParams params, Peer localPeer, String certificateHash) {
        Channel newChannel = new Channel();
        /**
         * 偷个懒，这个方法调用次数会很少，不用考虑性能问题
         */
        BeanUtils.copyProperties(params, newChannel);
        newChannel.setOrganizationId(localPeer.getOrgId().getValue());
        /**
         * Channel的扩展属性
         */
        //TODO 测评专用，测评完使用后需要改回来
        //newChannel.setChannelId("43DD2523475F445688816154366295D8");
        newChannel.setChannelId(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        PeerCert.clonePeerCers(localPeer, newChannel.getChannelId(), certificateHash);
        newChannel.getMemberPeerList().add(localPeer);
        //创建时，智能合约为空，所以不用单独设置smartContractList
        return newChannel;
    }

    /**
     * 如果存在某个成员节点的PeerId与输入newPeer的PeerId相同，则返回true
     *
     * @param newPeer
     * @return
     */
    public boolean containPeer(Peer newPeer) {
        for (Peer peer : this.getMemberPeerList()) {
            if (peer.getPeerId().equals(newPeer.getPeerId())) {
                return true;
            }
        }
        return false;
    }

    public boolean containPeerId(IdentityKey identityKey) {
        for (Peer peer : this.getMemberPeerList()) {
            if (peer.getPeerId().equals(identityKey)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 添加一个新节点到通道中
     *
     * @param newMemberPeer
     */
    public void addOnePeer(Peer newMemberPeer) {
        Peer optPeer = this.getOptPeer(newMemberPeer);
        //如果节点存在，并且没有退出通道，则报错
        if (optPeer!=null
            && !optPeer.isExitChannel()) {
            throw new NewspiralException(NewSpiralErrorEnum.PEER_ALREDAY_IN_CHANNEL);
        } else {
            memberPeerList.add(newMemberPeer);
        }
    }

    /**
     * remove one peer from the channel
     *
     * @param removedPeer
     */
    public void removeOnePeer(Peer removedPeer) {
        Peer optPeer = this.getOptPeer(removedPeer);
        //如果不存在该节点，或者节点已经退出通道，则报错
        if (optPeer == null
                || optPeer.isExitChannel()) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Peer not exist");
        } else {
            memberPeerList.remove(removedPeer);
        }
    }

    //从通道中获取当前操作的peer信息
    public Peer getOptPeer(Peer optPeer){
        if (!CollectionUtils.isEmpty(this.memberPeerList)) {
            for (Peer peer : this.getMemberPeerList()) {
                if (peer.getPeerId().equals(optPeer.getPeerId()))
                    return peer;
            }
        }
        return null;
    }

    public static List<SmartContract> compareSmartContractList(List<SmartContract> oldScList,List<SmartContract> newScList)
    {
        List<SmartContract> changeScList=new ArrayList<>();
        //旧的先存起来
        Map<String, SmartContract> oldScMap = new HashMap<>();
        if (oldScList != null) {
            for (SmartContract oldSc : oldScList) {
                oldScMap.put(oldSc.getId() + oldSc.getFlag(), oldSc);
            }
        }
        //新的加入
        if (newScList != null) {
            for (SmartContract sc : newScList) {
                if (oldScMap.get(sc.getId() + sc.getFlag()) == null) {
                    changeScList.add(sc);
                }
            }
        }
        return changeScList;
    }

    /**
     * 查询该高度通道中所有没有退出通道的节点列表
     * @param height
     * @return
     */
    public List<Peer> getMemberPeerListNotExitChannel(Long height){
        List<Peer> peerList = new ArrayList<>();
        for (Peer peer : this.getMemberPeerList()) {
            //拿到历史记录
            List<PeerChannelRelation> peerChannelRelationList = peer.getPeerChannelRelationList();
            //首先找出类型为INOUT的inBlockHeight小于该height的最大一条记录
            List maxInOutPeerRecord =  getMaxInOutPeerRecordLessThanHeight(peerChannelRelationList,height);
            if (CollectionUtils.isEmpty(maxInOutPeerRecord)) {
                //为空说明该节点在该高度还未加入
                continue;
            } else {
                //不为空，说明该节点在该高度已经加入了，判断是否退出
                PeerChannelRelation peerChannelRelation = (PeerChannelRelation) maxInOutPeerRecord.get(1);
                Long hasAlreadyExitFromChannelHeight = peerChannelRelation.getOutBlockHeight() + 3;
                if ( height < hasAlreadyExitFromChannelHeight
                        || peerChannelRelation.getOutBlockHeight() == 0) {
                    //在该高度还没有退出通道
                    peerList.add(peer);
                }
            }
        }
        return peerList;
    }

    /**
     * 查询该高度通道中所有退出通道的节点列表
     * @param height
     * @return
     */
    public List<Peer> getMemberPeerListExitChannel(Long height){
        List<Peer> peerList = new ArrayList<>();
        for (Peer peer : this.getMemberPeerList()) {
            //拿到历史记录
            List<PeerChannelRelation> peerChannelRelationList = peer.getPeerChannelRelationList();
            //首先找出类型为INOUT的inBlockHeight小于该height的最大一条记录
            List maxInOutPeerRecord =  getMaxInOutPeerRecordLessThanHeight(peerChannelRelationList,height);
            if (CollectionUtils.isEmpty(maxInOutPeerRecord)) {
                //为空说明该节点在该高度还未加入
                peerList.add(peer);
            } else {
                //不为空，说明该节点在该高度已经加入了，判断是否退出
                PeerChannelRelation peerChannelRelation = (PeerChannelRelation) maxInOutPeerRecord.get(1);
                Long hasAlreadyExitFromChannelHeight = peerChannelRelation.getOutBlockHeight() + 3;
                if ( !(height < hasAlreadyExitFromChannelHeight
                        || peerChannelRelation.getOutBlockHeight() == 0)) {
                    //已经退出
                    peerList.add(peer);
                }
            }
        }
        return peerList;
    }

    /**
     * 根据高度查询有效的节点列表，用了多个continue是为了罗列各种情况，增加易读性
     * 返回的节点列表是正常的节点，不包括已经退出的节点和冻结的节点
     * @param height
     * @return
     */
    public List<Peer> getValidMemberPeerList(Long height) {
        //log.info("getValidMemberPeerList by height {}", height);
        List<Peer> peerList = new ArrayList<>();
        for (Peer peer : this.getMemberPeerList()) {
            //拿到历史记录
            List<PeerChannelRelation> peerChannelRelationList = peer.getPeerChannelRelationList();
            //首先找出类型为INOUT的inBlockHeight小于该height的最大一条记录
            List maxInOutPeerRecord =  getMaxInOutPeerRecordLessThanHeight(peerChannelRelationList,height);
            if (CollectionUtils.isEmpty(maxInOutPeerRecord)) {
                //为空说明该节点在该高度还未加入
                continue;
            } else {
                //不为空，说明该节点在该高度已经加入了，判断是否退出
                PeerChannelRelation peerChannelRelation = (PeerChannelRelation) maxInOutPeerRecord.get(1);
                Long hasAlreadyExitFromChannelHeight = peerChannelRelation.getOutBlockHeight() + 3;
                if (height < hasAlreadyExitFromChannelHeight
                    || peerChannelRelation.getOutBlockHeight() == 0L) {
                    //说明在该高度未退出通道
                    //查询inBlockHeight大于maxInOutPeerRecord且小于height的类型为Frozen的记录
                    PeerChannelRelation maxFrozenPeerRecord = getMaxFrozenPeerRecordLessThanHeight(peerChannelRelationList,maxInOutPeerRecord,height);
                    if (maxFrozenPeerRecord == null) {
                        //没找到，说明节点未进行冻结解冻操作，节点是正常的
                        peerList.add(peer);
                    } else {
                        //找到了，说明节点进行了冻结解冻操作，判断是否退出了
                        long hasAlreadyUnfrozenFromChannel = maxFrozenPeerRecord.getOutBlockHeight()+3;
                        if (height >= hasAlreadyUnfrozenFromChannel
                            && maxFrozenPeerRecord.getOutBlockHeight() !=0L) {
                            //已经解冻了，节点是正常的
                            peerList.add(peer);
                        } else {
                            //没有解冻，节点是不正常的
                            continue;
                        }
                    }
                }
            }
        }
        return peerList;
    }

    /**
     * 找到范围查找高度范围内的最大的一条Frozen类型的记录
     */
    private PeerChannelRelation getMaxFrozenPeerRecordLessThanHeight(List<PeerChannelRelation> peerChannelRelationList, List maxInOutPeerRecord, Long height) {
        //拿到终止的高度
        int end = (int)maxInOutPeerRecord.get(0);
        PeerChannelRelation peerChannelRelationInOut = (PeerChannelRelation) maxInOutPeerRecord.get(1);
        for (int i = 0 ; i <= end ; i++) {
            PeerChannelRelation peerChannelRelation = peerChannelRelationList.get(i);
            if (PeerActionTypeEnum.FROZEN.getCode().equals(peerChannelRelation.getActionType())
                    && height >= peerChannelRelation.getInBlockHeight()+3
                    && peerChannelRelation.getInBlockHeight() >= peerChannelRelationInOut.getInBlockHeight()) {
                return peerChannelRelation;
            }
        }
        return null;
    }

    /**
     * 找出类型为INOUT的inBlockHeight小于该height的最大一条记录
     */
    private List getMaxInOutPeerRecordLessThanHeight(List<PeerChannelRelation> peerChannelRelationList, Long height) {
        List relationList = new ArrayList();
        for (int i = 0 ; i < peerChannelRelationList.size() ; i++) {
            PeerChannelRelation peerChannelRelation = peerChannelRelationList.get(i);
            if (PeerActionTypeEnum.IN_OUT.getCode().equals(peerChannelRelation.getActionType())
                && (peerChannelRelation.getInBlockHeight() + 3 <= height
                || peerChannelRelation.getInBlockHeight() == 0L)) {
                relationList.add(i);//索引
                relationList.add(peerChannelRelation);
                return relationList;
            }
        }
        return relationList;
    }


    public static boolean isModifyPeer(Peer newPeer, Peer oldPeer) {
        //证书不等
        if (oldPeer.getPeerCert().size() != newPeer.getPeerCert().size()) {
            return true;
        }
        Map<String, PeerCert> oldPeerCertMap = new HashMap<>();
        for (PeerCert oldPeerCert : oldPeer.getPeerCert()) {
            oldPeerCertMap.put(oldPeerCert.getId() + oldPeerCert.getFlag(), oldPeerCert);
        }

        for (PeerCert newPeerCert : newPeer.getPeerCert()) {
            PeerCert peerCert = oldPeerCertMap.get(newPeerCert.getId() + newPeerCert.getFlag());
            if (peerCert == null) {
                return true;
            }

        }

        return false;
    }



    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Channel)) {
            return false;
        }

        Channel newChannel = (Channel) obj;
        if (!newChannel.getChannelId().equals(this.channelId)
                || !newChannel.getName().equals(this.getName())
                || newChannel.getBlockMaxSize().longValue() != this.getBlockMaxSize().longValue()
                || newChannel.getBlockMaxInterval().longValue() != this.getBlockMaxInterval().longValue()
                || newChannel.getAllowTimeErrorSeconds().longValue() != this.getAllowTimeErrorSeconds().longValue()
                || !newChannel.getChannelChange().equals(this.channelChange)
        ) {
            return false;
        }

        return true;
    }


    /**
     * 获取当前高度有效节点，例如添加节点移除节点
     * @param height
     * @return
     */
    public List<Peer> getCurrentValidMemberPeerListByHeight(Long height) {
        List<Peer> peerList = new ArrayList<>();
        for (Peer peer : this.getMemberPeerList()) {
            //for debug
            if (peer.getPeerChannelRelation() == null) {
                log.info(ModuleClassification.CE_CHANNEL_+"peerchannelRelation null");
            }
            if ((peer.getPeerChannelRelation().getInBlockHeight()  <= height ||
                    peer.getPeerChannelRelation().getInBlockHeight() == 0)
                    &&
                    (peer.getPeerChannelRelation().getOutBlockHeight()  > height ||
                            peer.getPeerChannelRelation().getOutBlockHeight() == 0)) {

                peerList.add(peer);
                //for debug
                // log.info("add peer of peerId {}", peer.getPeerId().getValue());
            }
        }

        return peerList;
    }

}
