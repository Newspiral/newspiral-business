package com.jinninghui.newspiral.common.entity.exception;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lida
 * @date 2019/7/12 11:
 * 业务异常
 * 便于编码，使用枚举枚举出所有错误码，统一抛出此异常
 * errorCode在构造函数中必选
 */
public class NewspiralException extends RuntimeException {
    final private String errorCode;
    final private String errorMsg;

    public NewspiralException(NewSpiralErrorEnum errorCode,Throwable cause)
    {
        super(errorCode.message,cause);
        this.errorCode=errorCode.code;
        this.errorMsg=errorCode.code;
    }

    public NewspiralException(NewSpiralErrorEnum errorCode)
    {
        super(errorCode.message);
        this.errorCode=errorCode.code;
        this.errorMsg=errorCode.message;
    }

    /**
     * 给错误码指定更详细的错误信息
     * @param errorCode
     * @param detailErrorMsg
     */
    public NewspiralException(NewSpiralErrorEnum errorCode,String detailErrorMsg)
    {
        super(errorCode.message+":"+detailErrorMsg);
        this.errorCode=errorCode.code;
        this.errorMsg=detailErrorMsg;
    }



    public String getErrorCode() {
        return errorCode;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

    public String getDetailErrorMsg() {
        return "code:"+errorCode+" msg:"+errorMsg;
    }
    public String getDefaultErrorMsg() {
        return NewSpiralErrorEnum.getMessage( this.errorCode);
    }

    //"code":"1004","message":"System error"
    public JSONObject getResponseErrorMsg()
    {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("code","1021");
        jsonObject.put("message",errorMsg);
        return jsonObject;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this);
    }
}
