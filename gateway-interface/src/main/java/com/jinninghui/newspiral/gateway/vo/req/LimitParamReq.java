package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.ddos.LimitParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class LimitParamReq extends RPCParam {

    /**
     * 限流参数列表
     */
    @ApiModelProperty("限流参数参数列表")
    @NotEmpty
    List<LimitParam> limitParams;
}
