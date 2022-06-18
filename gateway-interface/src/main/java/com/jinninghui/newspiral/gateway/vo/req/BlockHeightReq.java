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
public class BlockHeightReq extends RPCParam {
    /**
     * 区块高度
     */
    @ApiModelProperty(value = "区块高度（高度不能小于零）")
    @NotNull
    @Min(value = 0,message = "高度不能小于零")
    private Long height;

    /**
     * 是否使用已校验数据
     * 仅用于校验节点数据是否合法时
     */
    @ApiModelProperty(value = "仅用于校验节点数据是否合法时，需不需要从创世区块开始校验。" +
            "true代表从历史校验过的区块最高高度开始校验，" +
            "false表示从创世块开始校验，默认为true")
    private boolean flag = true;
}
