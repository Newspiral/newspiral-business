package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import lombok.Data;

/**
 * @version V1.0
 * @Title: QueryMemberApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/21 09:53
 */
@Data
public class QueryMemberListApproval extends BaseApproval {
    /**
     * 成员信息列表的查询条件
     */
    private QueryMemberListReq queryMemberListReq;
}
