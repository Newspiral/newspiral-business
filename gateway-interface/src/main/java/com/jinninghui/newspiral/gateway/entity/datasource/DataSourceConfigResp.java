package com.jinninghui.newspiral.gateway.entity.datasource;


import com.jinninghui.newspiral.common.entity.config.ChannelShardingInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
public class DataSourceConfigResp {

    @ApiModelProperty(value = "数据源别名")
    @Getter
    @Setter
    String dsName;
    @ApiModelProperty(value = "数据库jdbc链接")
    @Getter
    @Setter
    String jdbcUrl;
    @ApiModelProperty(value = "数据源已经能被路由到，才可以为true")
    @Getter
    @Setter
    boolean routeable;
    @ApiModelProperty(value = "分库中存在的通道数量")
    @Getter
    @Setter
    int channelNums;
    @ApiModelProperty(value = "该分库中通道的相关信息")
    @Getter
    @Setter
    List<ChannelShardingInfo> channelShardingInfos;
}
