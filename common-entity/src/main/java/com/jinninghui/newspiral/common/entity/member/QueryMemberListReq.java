package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @Title: QueryMemberReq
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/21 9:45
 */
@ApiModel(description = "查询成员列表请求体")
@Data
public class QueryMemberListReq {
    /**
     * 通道编号(必传)
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    /**
     * 0正常，1证书过期，2业务冻结，3业务删除
     * 允许多个
     */
    @ApiModelProperty("成员状态：0正常，1证书过期，2业务冻结，3业务删除")
    private List<MemberStateEnum> stateEnumList;

}
