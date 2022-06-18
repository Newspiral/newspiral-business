package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName PeerChannelIssuerReq
 * @Author owen
 * @Date 2021-04-21 2021-04-21 15:25
 * @Version 1.0
 * @Description 查询相同通道相同组织下的节点
 **/
@Data
public class PeerChannelIssuerReq extends RPCParam {

    /**
     * 组织ID
     */
    @ApiModelProperty(value = "组织ID")
    @NotNull
    private String issuerID;
}
