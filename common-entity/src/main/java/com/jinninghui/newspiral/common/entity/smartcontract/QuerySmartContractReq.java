package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: QuerySmartContractReq
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/8/27 18:38
 */
@ApiModel("查询智能合约请求体")
@Data
public class QuerySmartContractReq {
    /**
     * 通道编号
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    //别名
    @ApiModelProperty("合约别名")
    private String alisa;

    //版本号
    @ApiModelProperty("版本号")
    private String version;


    public QuerySmartContractReq() {
    }

    public QuerySmartContractReq(String channelId, String version, String alisa) {
        this.channelId = channelId;
        this.alisa = alisa;
        this.version = version;
    }
}
