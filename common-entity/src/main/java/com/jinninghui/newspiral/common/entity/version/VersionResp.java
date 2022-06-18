package com.jinninghui.newspiral.common.entity.version;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: VersionResp
 * @Package com.jinninghui.newspiral.common.entity.version
 * @Description:
 * @author: xuxm
 * @date: 2021/4/9 14:26
 */
@Data
public class VersionResp {
    @ApiModelProperty(value = "系统版本号")
    private String version;
    @ApiModelProperty(value = "系统版本说明")
    private String remark;
}
