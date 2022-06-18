package com.jinninghui.newspiral.gateway.vo.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @version V1.0
 * @Title: TransHashReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 14:58
 */
@Data
public class QueryTransactionsByBlockHashReq extends RPCParam {
    /**
     *blockhash值
     */
    @NotBlank
    private String blockHash;

}
