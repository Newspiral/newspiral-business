package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @version V1.0
 * @Title: AddMySelfToChannelReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/16 10:37
 */
@ApiModel(description = "节点请求加入自身到通道中的请求体")
@Data
public class AddMySelfToChannelReq extends RPCParam {
    /**
     * 已经在通道中的节点的集合
     */
    @ApiModelProperty("已经在通道中的节点的url集合")
    @Valid
    @NotEmpty
  private  List<String> serviceUrlForPeerList;
    /**
     * 新交易
     */
    @ApiModelProperty("系统交易，新增一个节点(addOnePeer)")
    @Valid
    @NotNull
   private SDKTransaction newMemberTransaction;
}
