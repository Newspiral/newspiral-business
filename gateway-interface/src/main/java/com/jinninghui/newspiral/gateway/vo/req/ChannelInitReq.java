package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @Title: ChannelInitReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/13 14:19
 */
@ApiModel("通道初始化请求体")
@Data
public class ChannelInitReq extends RPCParam {

    /**
     * 创建通道的组织id
     */
    //private String organizationId;
    /**
     * 一条链的基本参数
     */
    @ApiModelProperty("通道基本参数")
    @NotNull
    @Valid
    private ChannelBasicParamsReq channelBasicParams=new ChannelBasicParamsReq();


    /**
     * 预留字段，除本地节点外的其他需要加入的节点
     */
    @ApiModelProperty("预留字段，除本地节点外的其他需要加入的节点（当前版本仅支持由单个节点初始化通道，该字段参数无效）")
    private List<Peer> addPeerList=new ArrayList<>();
}
