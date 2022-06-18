package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @version V1.0
 * @Title: ChannelMasterPublicKeyReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/16 15:05
 */
@Data
public class ChannelMasterPublicKeyReq extends RPCParam {
    /**
     * 通道主公钥
     */
    @NotBlank
   private String masterPublicKey;
    /**
     *节点信息
     */
    @Valid
    @NotEmpty
   private List<Peer> channelPeerList;

}
