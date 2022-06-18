package com.jinninghui.newspiral.common.entity.ddos;

public enum ConstraintTypeEnum {
    NO_ACCESS("NoAccess","禁止访问"),
    PEER_ACCESS("PeerAccess","节点白名单，运行节点之间相互调用");


    private String constraintType;
    private String message;
    ConstraintTypeEnum(String constraintType, String message){
        this.constraintType = constraintType;
        this.message = message;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public String getMessage() {
        return message;
    }
}
