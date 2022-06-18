package com.jinninghui.newspiral.common.entity.identity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/5 19:21
 * 数据签名中身份标识，在身份标识基础上增加签名值
 */
@ApiModel(description = "数据签名的身份标识")
@Data
public class SignerIdentityKey {

    @ApiModelProperty(value = "身份标识")
    IdentityKey identityKey;

    @ApiModelProperty(value = "针对约定参数的签名（见签名）")
    private String signature;

    @ApiModelProperty(value = "签名时间戳（见签名说明）")
    private String timeStamp;

    @ApiModelProperty(value = "防重放随机数（见签名说明）")
    private String nonce;

    @ApiModelProperty("调用者的ip地址，仅用于数据透传，不需要赋值")
    private String callerIp;

    public boolean equalsWithIdentityKey(IdentityKey identityKey) {
        return this.identityKey.equals(identityKey);
    }

    public SignerIdentityKey clone() {
        SignerIdentityKey signerIdentityKey = new SignerIdentityKey();
        signerIdentityKey.setIdentityKey(this.identityKey.clone());
        signerIdentityKey.setNonce(this.nonce);
        signerIdentityKey.setSignature(this.signature);
        signerIdentityKey.setTimeStamp(this.timeStamp);
        //todo:2.0.8
        return signerIdentityKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"identityKey\":")
                .append(identityKey);
        sb.append(",\"timeStamp\":\"")
                .append(timeStamp).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
