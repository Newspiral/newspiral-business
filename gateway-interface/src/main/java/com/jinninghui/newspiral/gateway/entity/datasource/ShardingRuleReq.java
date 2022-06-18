package com.jinninghui.newspiral.gateway.entity.datasource;

import com.jinninghui.newspiral.gateway.vo.req.RPCParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.blockHeightBuffer;

@Valid
@ToString
public class ShardingRuleReq extends RPCParam {

    /**
     * 分片的高度范围
     */
    @ApiModelProperty(value = "分片范围")
    @Min(value = blockHeightBuffer, message = "sharding范围必须大于冗余量，当前冗余量为" + blockHeightBuffer)
    @Getter @Setter
    Long shardingRange;

    /**
     * 启用的库个数
     */
    @ApiModelProperty(value = "当前活跃数据库个数")
    @Min(value = 2, message = "数据库个数必须为两个或两个以上")
    @Getter @Setter
    Integer dbActiveNum;
}
