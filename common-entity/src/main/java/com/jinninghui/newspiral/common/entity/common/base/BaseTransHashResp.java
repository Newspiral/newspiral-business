package com.jinninghui.newspiral.common.entity.common.base;

import lombok.Data;

/**
 * @version V1.0
 * @Title: BaseTransHashResp
 * @Package com.jinninghui.newspiral.common.entity.common.base
 * @Description:
 * @author: xuxm
 * @date: 2021/4/23 9:44
 */
@Data
public class BaseTransHashResp {
    /** 交易hash**/
    private String transHash;
    /** 返回信息**/;
    private  NewspiralStateCodes newspiralStateCodes;


    public BaseTransHashResp(String transHash)
    {
        this.transHash=transHash;
    }

    public BaseTransHashResp(String transHash,NewspiralStateCodes newspiralStateCodes)
    {
        this.transHash=transHash;
        this.newspiralStateCodes=newspiralStateCodes;
    }
}
