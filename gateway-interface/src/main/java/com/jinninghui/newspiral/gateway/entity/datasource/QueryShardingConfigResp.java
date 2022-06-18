package com.jinninghui.newspiral.gateway.entity.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class QueryShardingConfigResp {

    @ApiModelProperty(value = "数据源列表")
    @Getter
    @Setter
    List<DataSourceConfigResp> dataSourceConfigResps = new ArrayList<>();
    @ApiModelProperty(value = "分库范围")
    @Getter
    @Setter
    long shardingRange;
    @ApiModelProperty(value = "数据库活跃个数")
    @Getter
    @Setter
    int dbActiveNum;
    @ApiModelProperty(value = "用于分库的表名")
    @Getter
    @Setter
    String shardingDataSourceTables;
    @ApiModelProperty(value = "用于分库的字段名")
    @Getter
    @Setter
    String shardingDataSourceColumn;
    @ApiModelProperty(value = "当前活跃的通道个数")
    @Getter
    @Setter
    int channelNums;
    @ApiModelProperty(value = "当前分库总数据量")
    @Getter
    @Setter
    long totalDataNums;
    @ApiModelProperty(value = "是否需要分库标识符")
    @Getter
    @Setter
    boolean needExpand;
    @ApiModelProperty(value = "配置是否已经被sharidng使用")
    @Getter
    @Setter
    boolean configActive;

}
