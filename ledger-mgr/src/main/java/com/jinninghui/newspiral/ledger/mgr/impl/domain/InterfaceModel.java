package com.jinninghui.newspiral.ledger.mgr.impl.domain;
import com.jinninghui.newspiral.common.entity.ddos.Interface;
import com.jinninghui.newspiral.common.entity.ddos.LimitParam;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class InterfaceModel {

    /**
     * 主键，非null
     */
    private Integer id;
    /**
     *  接口所在类的类名
     */
    private String className;

    /**
     *  方法名
     */
    private String methodName;

    /**
     * 接口是否限流
     */
    private String limitAccess;

    /**
     * 每秒产生的令牌数
     */
    private Integer createTokenRate;

    /**
     * 等待令牌的最大时间
     */
    private Integer waitTokenTime;


    public static InterfaceModel createInstance(Interface anInterface){
        InterfaceModel interfaceModel = new InterfaceModel();
        interfaceModel.setClassName(anInterface.getClassName());
        interfaceModel.setMethodName(anInterface.getMethodName());
        interfaceModel.setLimitAccess(anInterface.getLimitAccess());
        interfaceModel.setCreateTokenRate(anInterface.getCreateTokenRate());
        interfaceModel.setWaitTokenTime(anInterface.getWaitTokenTime());
        return interfaceModel;
    }

    public LimitParam toLimitParam(InterfaceModel interfaceModel){
        LimitParam limitParam = new LimitParam();
        limitParam.setClassName(interfaceModel.getClassName());
        limitParam.setMethodName(interfaceModel.getMethodName());
        limitParam.setLimitAccess(interfaceModel.getLimitAccess());
        limitParam.setCreateTokenRate(interfaceModel.getCreateTokenRate());
        limitParam.setWaitTokenTime(interfaceModel.getWaitTokenTime());
        return  limitParam;
    }
}
