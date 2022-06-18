package com.jinninghui.newspiral.common.entity.smartcontract;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import lombok.Data;

/**
 * @version V1.0
 * @Title: SmartContractAuthorizedApproval
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/8/31 9:38
 */
@Data
public class SmartContractAuthorizedApproval extends BaseApproval {

    private SmartContractsAuthorized smartContractsAuthorized;
}
