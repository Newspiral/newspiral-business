package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/9/9 17:46
 */
@ApiModel(description = "节点服务url")
@Data
public class PeerServiceUrl {
    @ApiModelProperty("url类型")
    String type;
    @ApiModelProperty(value = "url地址")
    String url;
}
