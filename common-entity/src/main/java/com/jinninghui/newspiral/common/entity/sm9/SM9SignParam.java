package com.jinninghui.newspiral.common.entity.sm9;

import lombok.Data;

/**
 * @version V1.0
 * @Title: SM9SignParam
 * @Package com.jinninghui.newspiral.common.entity.sm9
 * @Description:
 * @author: xuxm
 * @date: 2019/10/22 11:05
 */
@Data
public class SM9SignParam {
    private String channelId;
    private MasterPublicKey masterPublicKey;
    private  PrivateKey privateKey;
    private byte[] data;
    private ResultSignature resultSignature;
    /**
     *
     */
    private String ida;

}
