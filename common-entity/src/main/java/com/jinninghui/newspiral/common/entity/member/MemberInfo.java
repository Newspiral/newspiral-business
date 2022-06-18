package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(description = "成员信息")
@Data
public class MemberInfo implements Serializable {

    @ApiModelProperty(value = "公钥证书")
    private String certificateCerFile;


    @ApiModelProperty(value = "扩展属性数据")
    private Map<String, String> extendedData = new HashMap<String, String>();

    @ApiModelProperty(value = "角色ID组")
    private List<String> roleIdList = new ArrayList<>();

    public MemberInfo clone() {
        MemberInfo info = new MemberInfo();
        info.setCertificateCerFile(certificateCerFile);
        info.setExtendedData(extendedData);
        info.setRoleIdList(roleIdList);
        return info;
    }
}
