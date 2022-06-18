package com.jinninghui.newspiral.security.serialize;

public interface ObjectSerializer {

    byte[] serialize(Object data, Class clazz);
    Object deserialize(byte[] data, Class clazz);
}
