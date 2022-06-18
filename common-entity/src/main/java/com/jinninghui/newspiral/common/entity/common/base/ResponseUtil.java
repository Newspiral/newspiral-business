package com.jinninghui.newspiral.common.entity.common.base;

/**
 * @version V1.0
 * @Title: ResponseUtil
 * @Package com.jinninghui.newspiral.gateway.base
 * @Description:
 * @author: xuxm
 * @date: 2019/12/6 10:59
 */
public class ResponseUtil<T>  {

    /**
     * 成功响应返回数据
     * @return
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<T>(NewspiralStateCodes.SUCCESS.getCode(), NewspiralStateCodes.SUCCESS.getMsg());
    }
    /**
     * 成功响应返回数据
     * @param t
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T t) {
        return new BaseResponse<>( NewspiralStateCodes.SUCCESS.getCode(), t);
    }

    /**
     * 错误响应
     * @param status
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(NewspiralStateCodes status) {
        return new BaseResponse<T>(status.getCode(), status.getMsg());
    }

    /**
     * 错误响应返回数据
     * @param status
     * @param t
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(NewspiralStateCodes status, T t) {
        return new BaseResponse<>(status.getCode(), status.getMsg(),  t);
    }


    public static <T> ListResponse<T> error(NewspiralStateCodes status,BizVO<T> bizVO){
        return new ListResponse(status.getCode(),status.getMsg(),bizVO);
    }

    public static <T> ListResponse<T> success(NewspiralStateCodes status, BizVO<T> bizVO){
        return new ListResponse(status.getCode(),status.getMsg(),bizVO);
    }
}
