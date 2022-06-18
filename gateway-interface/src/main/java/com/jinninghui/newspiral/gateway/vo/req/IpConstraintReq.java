package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.ddos.IpConstraintListParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class IpConstraintReq extends RPCParam {

    /**
     * 黑名单列表
     */
    @ApiModelProperty("黑名单列表")
    @NotEmpty
    private List<IpConstraintListParam> ipConstraintListParams;
}
