package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.record.InterfaceRecord;
import lombok.Data;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class InterfaceRecordModel {

    /**
     * 自增主键
     */
    @NotNull
    private Long Id;

    /**
     * 被调用的接口名称
     */
    @NotNull
    private String methodName;


    /**
     * 被调用的接口协议类型
     */
    @NotNull
    private String protocolName;

    /**
     * 调用方身份，IdentityTypeEnum key
     */
    @NotBlank
    private String identityType;

    /**
     * 调用方身份，vulue
     */
    @NotBlank
    private String identityValue;

    /**
     * 调用所属通道ID
     */
    private String channelId;

    /**
     * 调用发生时间戳，毫秒
     */
    @NotNull
    private Date startTime;

    /**
     * 调用完成时间戳，毫秒
     */
    @NotNull
    private Date endTime;

    /**
     * 接口处理时长，毫秒
     */
    @NotNull
    private Long callTime;

    /**
     * 调用结果，1：Success:；0：Failed
     */
    @NotNull
    private Integer successed;

    /**
     * 调用错误信息
     */
    private String errorMsg;

    /**
     * 调用合约别名
     */
    private String scAlisa;

    /**
     * 合约方法名
     */
    private String scMethodName;

    /**
     * 调用合约版本号
     */
    private String scVersion;

    public static InterfaceRecordModel createInstance(InterfaceRecord interfaceRecord){
        InterfaceRecordModel interfaceRecordModel = new InterfaceRecordModel();
        interfaceRecordModel.setId(interfaceRecord.getId());
        interfaceRecordModel.setMethodName(interfaceRecord.getMethodName());
        interfaceRecordModel.setProtocolName(interfaceRecord.getProtocolName());
        interfaceRecordModel.setIdentityType(interfaceRecord.getIdentityType()==null?null:interfaceRecord.getIdentityType().getCode());
        interfaceRecordModel.setIdentityValue(interfaceRecord.getIdentityValue());
        interfaceRecordModel.setChannelId(interfaceRecord.getChannelId());
        interfaceRecordModel.setStartTime(new Date(interfaceRecord.getStartTime()));
        interfaceRecordModel.setEndTime(new Date(interfaceRecord.getEndTime()));
        interfaceRecordModel.setCallTime(interfaceRecord.getCallTime());
        interfaceRecordModel.setSuccessed(interfaceRecord.getSuccessed());
        interfaceRecordModel.setErrorMsg(interfaceRecord.getErrorMsg());
        interfaceRecordModel.setScAlisa(interfaceRecord.getScAlisa());
        interfaceRecordModel.setScMethodName(interfaceRecord.getScMethodName());
        interfaceRecordModel.setScVersion(interfaceRecord.getScVersion());
        return interfaceRecordModel;
    }

}
