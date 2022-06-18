package com.jinninghui.newspiral.common.entity.util;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Data
@Component
public class SpringBeanUtil {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //这里不用加锁，因为不经常使用
        if (SpringBeanUtil.applicationContext == null) {
            SpringBeanUtil.applicationContext = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return SpringBeanUtil.applicationContext.getBean(clazz);
    }

    public static Object getBean(String name) {
        return SpringBeanUtil.applicationContext.getBean(name);
    }

    public static String getProperty(String key) {
        return SpringBeanUtil.applicationContext.getEnvironment().getProperty(key);
    }
}
