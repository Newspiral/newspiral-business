package com.jinninghui.newspiral.common.entity.ddos;

public enum ClassTypeEnum {

    SDK("ServiceForSDKImpl","SDK对外提供的接口，供客户端调用"),
    PEER("ServiceForPeerImpl","PEER对外提供接口，节点之间调用"),
    CONSENSUS("ServiceForConsensusImpl","CONSENSUS对外提供接口，节点之间共识消息调用"),
    CONSTRACT("SystemSmartContract","系统合约提供接口");

    private String className;
    private String message;

    ClassTypeEnum(String className,String message){
        this.className = className;
        this.message = message;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }
}
