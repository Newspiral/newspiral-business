package com.jinninghui.newspiral.gateway.entity.datasource;

import com.jinninghui.newspiral.gateway.vo.req.RPCParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@ToString
public class AddDataSourceConfigReq extends RPCParam {


    @ApiModelProperty(value = "数据库ip")
    @Getter
    @Setter
    @NotBlank
    private String ip;

    @ApiModelProperty(value = "数据库端口")
    @Getter
    @Setter
    @Min(1)
    private String port;

    @ApiModelProperty(value = "数据库名")
    @Getter
    @Setter
    @NotBlank
    private String dbName;

    @ApiModelProperty(value = "数据库用户名")
    @Getter
    @Setter
    @NotBlank
    private String username;

    @ApiModelProperty(value = "数据库密码")
    @Getter
    @Setter
    @NotBlank
    private String password;

}
