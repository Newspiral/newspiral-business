package com.jinninghui.newspiral.common.entity.identity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author
 * @date 2019/10/23
 * 身份Key，在系统间和IdentityKey转化交互时使用
 * 例如使用ECC公钥证书，则证书本身是身份，而公钥则是身份Key
 */

@Slf4j
@Data
public class IdentityKeyBrief {

    @ApiModelProperty(value = "身份标识类型")
     @NotNull
    IdentityTypeEnum type;

    /**
     * 与具体IdentityTypeEnum相关
     */
    @ApiModelProperty(value = "身份标识类型值")
    @NotBlank
    String value;


    public IdentityKeyBrief clone()
    {
        IdentityKeyBrief key = new IdentityKeyBrief();
        key.setValue(this.getValue());
        key.setType(this.getType());
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":")
                .append(type);
        sb.append(",\"value\":\"")
                .append(value).append('\"');
        sb.append('}');
        return sb.toString();
    }


}
