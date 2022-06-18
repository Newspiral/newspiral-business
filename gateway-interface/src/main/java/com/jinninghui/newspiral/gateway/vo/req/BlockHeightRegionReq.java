package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: BlockHeightReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 11:45
 */
@Data
public class BlockHeightRegionReq extends RPCParam {
    /**
     * 区块起始高度
     */
    @ApiModelProperty("区块起始高度（高度不能小于零）")
    @NotNull
    @Min(value = 0,message = "高度不能小于零")
    private Long fromHeight;

    /**
     * 区块结束高度
     */
    @ApiModelProperty(value = "区块结束高度（高度不能小于零）")
    @NotNull
    @Min(value = 0,message = "高度不能小于零")
    private Long toHeight;

}
