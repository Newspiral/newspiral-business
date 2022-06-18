package com.jinninghui.newspiral.gateway.vo.req;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class QueryLatestBlockListReq extends RPCParam {


    /**
     * 查询个数
     */
    @Max(value = 10)
    @Min(value = 1)
    private int num;
}
