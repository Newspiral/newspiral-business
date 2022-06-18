package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @Title: PeerBasicParams
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/8/6 11:47
 */
@Data
public class PeerBasicParams {

    //使用公钥证书信息
    @ApiModelProperty(value = "使用公钥证书信息")
    private String publicCertFile;

    //使用组织公钥证书
    @ApiModelProperty(value = "使用组织公钥证书")
    private String organizationPublicCertFile;

    /**
     * 本节点提供的服务URL
     */
    @ApiModelProperty(value = "本节点提供的服务URL")
    PeerServiceUrls serviceUrls;

    //加入通道时间
    @ApiModelProperty(value = "加入通道时间")
    private Long joinTime;
    //1在线、2掉线、3冻结
    @ApiModelProperty(value = "1在线、2掉线、3冻结")
    private String currentState;
    /**
     * 节点Id
     */
    @ApiModelProperty(value = "节点Id")
    private String peerId;

    /**
     * 当前节点所述组织ID
     */
    @ApiModelProperty(value = "当前节点所属的组织ID")
    private String organizationID;

    /**
     * 节点的历史操作记录
     */
    List<PeerChannelOperationRecord> historyActionList;

}
