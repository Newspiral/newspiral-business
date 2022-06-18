package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.version.VersionResp;
import lombok.Data;

import java.util.Date;

@Data
public class SystemVersionModel {

    private Long id;

    private String version;

    private String remark;

    private Date createTime;


    public VersionResp toVersionResp(){
        VersionResp versionResp = new VersionResp();
        versionResp.setVersion(this.getVersion());
        versionResp.setRemark(this.getRemark());
        return versionResp;
    }
}
