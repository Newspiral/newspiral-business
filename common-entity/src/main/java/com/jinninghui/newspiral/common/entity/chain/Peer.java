package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lida
 * @date 2019/7/5 19:12
 * 节点信息
 * 代表一个运行的节点程序
 */
@ApiModel(description = "内存中的节点实体")
@Data
public class Peer implements Serializable {

    /**
     * 节点所属组织的根身份标识
     */
    @ApiModelProperty(value = "节点所属组织的标识")
    IdentityKey orgId;


    /**
     * 节点本身的身份标识
     */
    @ApiModelProperty(value = "节点本身的身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;


    /**
     * 本节点提供的服务URL
     */
    @ApiModelProperty(value = "本节点提供的服务URL")
    @Valid
    @NotNull
    PeerServiceUrls serviceUrls;


    /**
     * 当前节点在通道中的信息
     */
    @ApiModelProperty(value = "节点在特定通道中的最新信息",hidden = true)
    PeerChannelRelation peerChannelRelation = new PeerChannelRelation();

    /**
     * 当前节点在通道中的历史信息
     */
    @ApiModelProperty(value = "节点在特点通道中的历史信息,按照操作顺序排序",hidden = true)
    List<PeerChannelRelation> peerChannelRelationList = new ArrayList<>();

    /**
     * 是否是本地节点
     */
    @ApiModelProperty(value = "是否是本地的节点")
    Boolean isLocalPeer;

    /**
     * ca证书
     */
    @ApiModelProperty(value = "ca证书")
    private byte[] certificateCerFile=null;

    /**
     * 密钥库
     */
    @ApiModelProperty(value = "密钥库")
    private byte[] certificateKeyStoreFile=null;

    /**
     * 别名
     */
    @ApiModelProperty(value = "证书别名")
    private String certificateAlias;

    /**
     * 密钥库密钥
     */
    @ApiModelProperty(value = "密钥库密匙")
    private String certificateStorePass;

    /**
     * 证书hash
     */
    @ApiModelProperty(value = "证书hash")
    private String certificateHash;

    /**
     * 证书列表
     */
    @ApiModelProperty(value = "证书列表",hidden = true)
    private List<PeerCert> peerCert =new ArrayList<>();

    @ApiModelProperty(value = "是否为可用状态，通过rpc请求结果来控制")
    private boolean available = true;
    /**
     * 组织
     */
    @ApiModelProperty(value = "组织",hidden = true)
    private PeerOrganization peerOrganization;

    public static Peer createInstance(PeerInfo peerInfo) {
        Peer peer = new Peer();
        peer.setPeerId(peerInfo.peerId);
        peer.setOrgId(peerInfo.orgId);
        peer.setCertificateCerFile(peerInfo.getCertificateCerFile().clone());
        peer.setCertificateAlias(peerInfo.getCertificateAlias());
        peer.setPeerOrganization(peerInfo.getPeerOrganization().clone());
        peer.setServiceUrls(peerInfo.getServiceUrls());

        return peer;
    }

    /**
     * 复制一份
     * @return
     */
    public Peer clone()
    {
        Peer peer = new Peer();
        peer.setIsLocalPeer(this.getIsLocalPeer());
        peer.setOrgId(this.getOrgId().clone());
        peer.setPeerId(this.getPeerId().clone());
        peer.setServiceUrls(this.getServiceUrls().clone());
        peer.setCertificateCerFile(this.getCertificateCerFile());
        peer.setCertificateKeyStoreFile(this.getCertificateKeyStoreFile());
        peer.setCertificateAlias(this.getCertificateAlias());
        peer.setCertificateStorePass(this.getCertificateStorePass());
        peer.setPeerChannelRelation(this.getPeerChannelRelation().clone());
        peer.setPeerCert(PeerCert.clones(this.getPeerCert()));
        peer.setPeerOrganization(this.getPeerOrganization().clone());
        return peer;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o )
            return true;
        if(o instanceof Peer)
        {
            return this.getPeerId().equals(((Peer) o).getPeerId());
        }
        else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        //此处的目的是为了保证不同的peer对象，如果equals的结果为true，则set容器中的contains方法能返回true

        return this.getPeerId().getValue().length();
    }


/*    public static void main(String args[]) {
        PeerServiceUrls serviceUrls=new PeerServiceUrls();
        serviceUrls.setUrlOfType("192.168.1.2","FOR_PEER");
        serviceUrls.setUrlOfType("192.168.1.1","FOR_SDK");

        System.out.println(JSONObject.toJSON(serviceUrls));
        System.out.println(JSON.toJSONString(serviceUrls));
    }*/

/*    public void setPeerCert(List<PeerCert> peerCert) {
        this.peerCert = peerCert;
        if(!CollectionUtils.isEmpty(peerCert))
        {
         boolean flag=peerCert.parallelStream().anyMatch(peerCert1 -> ("0".equals(peerCert1.getFlag())));
         if(!flag)
         {
             this.state=false;
         }
        }
    }*/

    /**
     * 节点是否处于正常状态
     * @return
     */
    public boolean isState() {
        //如果最新的操作记录是冻结解冻操作，且outBlockHeight高度为0，说明冻结了，返回false
        if (PeerActionTypeEnum.FROZEN.getCode().equals(peerChannelRelation.getActionType())
            && peerChannelRelation.getOutBlockHeight() == 0L) {
            return false;
        }

        //如果最新的操作记录是进入退出操作，而且outBlockHight不等于0，说明已经退出通道了，返回false
        if (PeerActionTypeEnum.IN_OUT.getCode().equals(peerChannelRelation.getActionType())
            && peerChannelRelation.getOutBlockHeight()!=0L) {
            return false;
        }

        return true;
    }

    public boolean isExitChannel(){
        //如果最新的操作记录是进入退出操作，而且outBlockHight不等于0，说明已经退出通道了，返回true
        if (PeerActionTypeEnum.IN_OUT.getCode().equals(peerChannelRelation.getActionType())
                && peerChannelRelation.getOutBlockHeight()!=0L) {
            return true;
        }
        return false;
    }

}
