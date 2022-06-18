package com.jinninghui.newspiral.common.entity;

import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;

/**
 * @author lida
 * @date 2019/7/22 19:14
 * 可验证合法性的数据，例如区块，RPC调用中使用的对象，可以是参数也可以是返回值
 */
public interface VerifiableData extends Hashable {


    SignerIdentityKey getSignerIdentityKey();

    void setSignerIdentityKey(SignerIdentityKey identity);
}
