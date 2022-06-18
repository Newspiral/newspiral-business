package com.jinninghui.newspiral.gateway.vo.resp;

import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.ChannelDynamicParams;
import com.jinninghui.newspiral.common.entity.chain.PeerBasicParams;
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
public class PeerChannelResp {

    /**
     * 所属通道ID
     */
    @ApiModelProperty("所属通道ID")
    private String channelId;

    /**
     * 通道的公钥证书
     */
    @ApiModelProperty("通道的公钥证书")
    private String publicCertFile;

    /**
     * 通道的基础信息
     */
    @ApiModelProperty("通道的基础信息")
    private ChannelBasicParams channelBasicParams;

    /**
     * 通道动态数据
     */
    @ApiModelProperty("通道动态数据")
    private ChannelDynamicParams channelDynamicParams;

    /**
     * 节点信息列表
     */
    @ApiModelProperty("节点信息列表")
    private List<PeerBasicParams> peerBasicParams;

}
