package com.jinninghui.newspiral.security.contract;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.common.annotation.CustomRoleToken;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Slf4j
@Component
public class BusinessContractAuthChecker {

    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;


    public boolean checkAuth(Method method, SDKTransaction sdkTransaction) {
        CustomRoleToken customRoleToken = method.getAnnotation(CustomRoleToken.class);
        if (null != customRoleToken) {
            String modifier = Modifier.toString(method.getModifiers());
            //方法修饰符不是public或者角色鉴权失败，抛出异常
            if (!modifier.contains("public") || !existAuthCustomRole(sdkTransaction, customRoleToken.customRoleName())) {
                //throw new NewspiralException(NewSpiralErrorEnum.PERMISSION_DENIED);
                return false;
            }
        }
        return true;
    }


    private boolean existAuthCustomRole(VerifiableData verifiableData, String[] roleShortName) {
        if (null == verifiableData) {
            return false;
        }
        SignerIdentityKey signerIdentityKey = verifiableData.getSignerIdentityKey();
        if (null == signerIdentityKey) return false;

        String channelId = signerIdentityKey.getIdentityKey().getChannelId();
        String publicKey = signerIdentityKey.getIdentityKey().getValue();
        Member member = memberLedgerMgr.getMemberRoleByKey(channelId, publicKey);
        if (null == member) {
            log.error(ModuleClassification.TxM_SCA_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SmartContractAspect.before,member is null,channelId:{},publicKey:{}", channelId, publicKey);
            return false;
        }
        for (String str : roleShortName) {
            for (Role role : member.getRoles()) {
                if (str.equals(role.getShortName())) {
                    // 角色身份匹配成功
                    return true;
                }
            }
        }
        log.error(ModuleClassification.TxM_SCA_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",SmartContractAspect.before,custom role mismatch,members role List:{},custom role:{}", JSONObject.toJSONString(member.getRoles()), roleShortName);
        return false;
    }
}
