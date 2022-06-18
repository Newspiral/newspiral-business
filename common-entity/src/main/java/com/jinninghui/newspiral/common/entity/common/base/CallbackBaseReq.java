package com.jinninghui.newspiral.common.entity.common.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: CallbackBaseReq
 * @Package com.jinninghui.newspiral.common.entity.common.base
 * @Description:
 * @author: xuxm
 * @date: 2021/2/18 13:16
 */
@ApiModel(description = "回调请求数据")
@Data
public class CallbackBaseReq<T> {
    /**
     * 类型
     */
    @ApiModelProperty(value = "类型")
    protected CallbackEnum callbackEnum;

    /**
     * 对象
     */
    @ApiModelProperty(value = "对象")
    protected T data;
}
