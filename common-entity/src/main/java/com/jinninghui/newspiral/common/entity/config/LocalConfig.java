package com.jinninghui.newspiral.common.entity.config;

import lombok.Data;

@Data
public class LocalConfig {

    private String key;

    private String value;

    private String type;

    private transient int show;
}
