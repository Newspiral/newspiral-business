package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/24 9:31
 */
@Data
public abstract class HotStuffMsg implements VerifiableData {
    /**
     * 发送时本地的viewNo
     */
    @ApiModelProperty(value = "视图序号")
    Long viewNo;
}
