package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import lombok.Data;

/**
 * @version V1.0
 * @Title: QueryRoleApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/17 15:53
 */
@Data
public class QueryRoleApproval extends BaseApproval {
    /**
     * 查询实体role
     */
    private Role formRole;
}
