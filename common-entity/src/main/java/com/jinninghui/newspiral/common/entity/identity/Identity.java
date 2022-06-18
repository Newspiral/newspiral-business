package com.jinninghui.newspiral.common.entity.identity;

import lombok.Data;

/**
 * @author lida
 * @date 2019/9/9 19:31
 */
@Data
public class Identity {
    IdentityKey key;

    /**
     * 父身份的Key
     */
    IdentityKey parentKey;

    String extendedProps;
}
