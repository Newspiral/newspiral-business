package com.jinninghui.newspiral.common.entity.policy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NewSpiralPolicy {
    public String role() default "ASYNCHRONOUS";
    public String rule() default "ANY";
}
