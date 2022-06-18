package com.jinninghui.newspiral.common.entity.smartcontract;

import lombok.Data;

/**
 * @version V1.0
 * @Title: SmartContractCompile
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/9/24 15:53
 */
@Data
public class SmartContractCompileResp {
    //java文件
    private String sourceContent;

    //类名
    private String className;
}
