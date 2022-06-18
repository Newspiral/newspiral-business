package com.jinninghui.newspiral.common.entity.common.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: BaseResponse
 * @Package com.jinninghui.newspiral.gateway.base
 * @Description:
 * @author: xuxm
 * @date: 2019/12/6 10:37
 */
@ApiModel(description = "返回响应数据")
@Data
public class BaseResponse<T> {
    /**
     * 返回码
     */
    @ApiModelProperty(value = "返回码")
    protected String code;

    /**
     * 消息
     */
    @ApiModelProperty(value = "消息")
    protected String message;

    /**
     * 返回对象
     */
    @ApiModelProperty(value = "返回对象")
    protected T data;

    public BaseResponse() {
    }

    public BaseResponse(String code) {
        this.code = code;
    }

    public BaseResponse(String code, T data) {
        this.code = code;
        this.data = data;
    }

    public BaseResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
