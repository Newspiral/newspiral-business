package com.jinninghui.newspiral.gateway.vo.resp;

import com.jinninghui.newspiral.common.entity.chain.PeerStageEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @version V1.0
 * @Title: WorldStateResp
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2020/2/10 9:54
 */
@Data
public class PeerStateResp implements Serializable {

    /**
     * 节点Id
     */
    @ApiModelProperty(value = "节点Id")
    private String peerId;
    /**
     * 节点状态
     */
    @ApiModelProperty(value = "节点状态")
    private PeerStageEnum peerStageEnum;
    /**
     * 节点程序版本
     */
    @ApiModelProperty(value = "节点程序版本")
    private String codeVersion;
    /**
     * 节点jvm版本
     */
    @ApiModelProperty(value = "节点jvm版本")
    private String jvmVersion;
    /**
     * 同步世界状态变更历史线程的工作状态
     */
    @ApiModelProperty("同步世界状态变更历史线程的工作状态")
    private String syncWorldStateThreadState;
    /**
     * 系统信息
     */
    @ApiModelProperty(value = "系统信息")
    private SystemInformation systemInformation;







    //下面两个属性未用到，自测完成删除
    /**
     * IP:PORT
     */
    //private List<String> addressIP=new ArrayList<>();

    /**
     * 通道信息
     */
    //private List<ChannelShort> channelShorts;


}
