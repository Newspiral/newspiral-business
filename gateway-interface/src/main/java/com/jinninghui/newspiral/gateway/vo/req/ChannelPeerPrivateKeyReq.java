package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @version V1.0
 * @Title: ChannelPeerPrivateKeyReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/16 15:02
 */
@Data
public class ChannelPeerPrivateKeyReq extends RPCParam {
    /**
     * 节点信息列表 （此节点信息需要带着节点用户私钥哦）
     */
    @Valid
    @NotEmpty
   private List<Peer> channelPeerList;
}
