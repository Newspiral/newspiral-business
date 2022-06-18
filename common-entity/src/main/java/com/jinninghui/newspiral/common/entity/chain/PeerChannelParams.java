package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @Title: QueryWorldStateReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2020/2/8 11:11
 */
@Data
public class PeerChannelParams {

    /**
     * 所属通道ID
     */
    @ApiModelProperty(value = "所属通道ID")
    private String channelId;

    /**
     * 通道的组织公钥证书
     */
    @ApiModelProperty(value = "通道的组织公钥证书")
    private String publicCertFile;

    /**
     * 通道的基础信息
     */
    @ApiModelProperty(value = "通道的基础信息")
    private ChannelBasicParams channelBasicParams;

    /**
     * 通道动态数据
     */
    @ApiModelProperty(value = "通道动态数据")
    private ChannelDynamicParams channelDynamicParams;

    /**
     * 节点信息列表
     */
    @ApiModelProperty(value = "节点信息列表")
    private List<PeerBasicParams> peerBasicParams;

}
