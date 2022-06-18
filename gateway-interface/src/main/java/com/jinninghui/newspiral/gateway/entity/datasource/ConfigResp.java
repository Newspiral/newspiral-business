package com.jinninghui.newspiral.gateway.entity.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class ConfigResp<T> {

    @ApiModelProperty(value = "成功标识")
    @Getter@Setter
    private boolean successflag;

    @ApiModelProperty(value = "返回数据")
    @Getter@Setter
    private T data;

    public ConfigResp(boolean successflag, T data) {
        this.successflag = successflag;
        this.data = data;
    }
}
