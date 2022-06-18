package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title:
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date:
 */
@Data
public class ExitMySelfFromChannelReq extends RPCParam {
    /**
     * 新交易
     */
    @Valid
    @NotNull
   private SDKTransaction exitTransaction;
}
