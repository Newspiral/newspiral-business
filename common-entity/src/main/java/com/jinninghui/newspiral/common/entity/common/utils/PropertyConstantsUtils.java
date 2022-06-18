package com.jinninghui.newspiral.common.entity.common.utils;

import java.io.IOException;
import java.util.Properties;

public class PropertyConstantsUtils {
    private static Properties properties;
    private static String defaultName = "application.properties";



    public static void setProperty(String name){
        if (properties==null) {
            properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();/**/
            try {
                properties.load(loader.getResourceAsStream(name));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getPropertiesKey(String key){
        if (properties==null) {
            setProperty(defaultName);
        }
        return properties.getProperty(key, "default");
    }
}
