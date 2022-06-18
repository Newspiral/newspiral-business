package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.config.LocalConfig;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@Valid
public class LocalConfigModel {

    @NotNull
    private String key;
    @NotNull
    private String value;
    private String type;
    private int show;

    public LocalConfig toLocalConfig() {
        LocalConfig config = new LocalConfig();
        config.setType(this.type);
        config.setKey(this.key);
        config.setValue(this.value);
        config.setShow(this.show);
        return config;
    }


    public static LocalConfigModel createInstance(LocalConfig config){
        LocalConfigModel configModel = new LocalConfigModel();
        configModel.setKey(config.getKey());
        configModel.setValue(config.getValue());
        configModel.setType(config.getType());
        return configModel;
    }

}
