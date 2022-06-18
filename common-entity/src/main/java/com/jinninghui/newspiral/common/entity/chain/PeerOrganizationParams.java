package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

/**
 * @version V1.0
 * @Title: QueryWorldStateReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2020/2/8 11:11
 */
@Data
public class PeerOrganizationParams {

    //使用组织公钥证书
    private String organizationPublicCertFile;

}
