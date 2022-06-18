package com.jinninghui.newspiral.common.entity.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version V1.0
 * @Title: AuthRoleToken
 * @Package com.jinninghui.newspiral.common.entity.common.annotation
 * @Description:
 * @author: xuxm
 * @date: 2020/1/8 16:09
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifyAuth {

    /**
     * 访问所需的预定义的角色身份，默认为空，可以多个定义
     * @return
     */
    String Strategy() default AnnotationConstant.COMMON_STRATEGY;

    /**
     * 是否验签，默认为ture
     * @return
     */
    boolean verifySignature() default true;

    /**
     * 是否要求调用者是某个通道的成员
     */
    boolean callerHaveChannelMemberIdentity() default true;
}
