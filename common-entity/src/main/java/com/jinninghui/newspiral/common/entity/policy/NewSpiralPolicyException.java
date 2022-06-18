package com.jinninghui.newspiral.common.entity.policy;

import lombok.Data;

@Data
public class NewSpiralPolicyException extends RuntimeException {
    private String role;
    private String rule;
    private String msg;
    public NewSpiralPolicyException(String role, String rule, String msg) {
        this.role = role;
        this.rule = rule;
        this.msg = msg;
    }
}
