package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.cert.CertData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: CertDataReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2021/2/3 14:33
 */
@Data
public class UserCertDataReq extends RPCParam {
    @ApiModelProperty(value = "证书生成实体")
    private CertData certData;
    //private HttpServletResponse response;

    @ApiModelProperty(value = "根证书文件")
    private String rootCert;
    @ApiModelProperty(value = "根证书文件私钥")
    private String rootPrivateKey;
}
