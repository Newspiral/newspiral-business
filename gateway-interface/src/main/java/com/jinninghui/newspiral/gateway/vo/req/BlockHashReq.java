package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @version V1.0
 * @Title: BlockHeightReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 11:45
 */
@Data
public class BlockHashReq extends RPCParam {
    /**
     * 区块Hash
     */
    @ApiModelProperty(value = "区块Hash值")
    @NotBlank
    private String  blockHash;

}
