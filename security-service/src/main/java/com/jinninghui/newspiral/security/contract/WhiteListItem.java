package com.jinninghui.newspiral.security.contract;

public class WhiteListItem {
    String pattern;
    boolean loadHere;

    public WhiteListItem(String pattern, boolean loadHere) {
        this.pattern= pattern;
        this.loadHere= loadHere;
    }

    boolean matches(String name) {
        if (name.contains("/")) {
            name = name.replace("/", ".");
        }
        return name.startsWith(pattern) && name.indexOf('.', pattern.length())<0;
    }
}
