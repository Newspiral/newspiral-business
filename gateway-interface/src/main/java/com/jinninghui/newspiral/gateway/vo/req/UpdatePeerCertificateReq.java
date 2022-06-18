package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @version V1.0
 * @Title: AddMySelfToChannelReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/16 10:37
 */
@Data
public class UpdatePeerCertificateReq extends RPCParam {

    /**
     * 需要修改的节点的信息
     */
    @Valid
    @NotEmpty
  private  List<Peer> channelPeerList;
    /**
     * 通道ID
     */
    @NotBlank
    private  String channelId;
}
