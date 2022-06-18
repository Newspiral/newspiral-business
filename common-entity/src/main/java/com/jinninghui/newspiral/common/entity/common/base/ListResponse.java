package com.jinninghui.newspiral.common.entity.common.base;

import lombok.Data;


@Data
public class ListResponse<T> {
    public ListResponse(){

    }
    public ListResponse(String code, String message, BizVO<T> bizVO) {
        this.code = code;
        this.message = message;
        this.bizVO = bizVO;
    }


    /**
     * 返回码
     */
    protected String code;

    /**
     * 消息
     */
    protected String message;

    private BizVO<T> bizVO;

}
