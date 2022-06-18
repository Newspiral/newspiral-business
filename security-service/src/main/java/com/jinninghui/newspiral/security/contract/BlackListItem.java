package com.jinninghui.newspiral.security.contract;

public class BlackListItem {
    String pattern;

    public BlackListItem(String pattern) {
        this.pattern= pattern;
    }

    boolean matches(String name) {
        return name.startsWith(pattern) && name.indexOf('.', pattern.length())<0;
    }
}
