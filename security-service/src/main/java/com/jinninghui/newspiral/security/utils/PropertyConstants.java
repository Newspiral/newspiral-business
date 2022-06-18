package com.jinninghui.newspiral.security.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * @version V1.0
 * @Title: PropertyConstants
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2020/5/26 19:22
 */
public class PropertyConstants {
    private static Properties properties;

    private static void setProperty(){
        if (properties==null) {
            properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();/**/
            try {
                properties.load(loader.getResourceAsStream("security.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getPropertiesKey(String key){
        if (properties==null) {
            setProperty();
        }
        return properties.getProperty(key, "default");
    }

}
