package com.jinninghui.newspiral.security.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

public class KryoObjectSerializer implements ObjectSerializer{

    private static KryoObjectSerializer me = new KryoObjectSerializer();

    @Setter
    @Getter
    private Kryo kryo;

    @Getter
    private LinkedList<Class> registerClazzList = new LinkedList<Class>(){};

    private KryoObjectSerializer() {
    }

    public void init() {
        Kryo kryo = new Kryo();
        me.setKryo(kryo);
        me.registerClazzIfNecessary(new Class[]{Hashable.class,
                PooledTransaction.class,
                Block.class});

    }

    private void registerClazzIfNecessary(Class[] classes) {
        boolean hasNotFound = true;
        for (Class clazz : classes) {
            for (Class registerClazz : me.getRegisterClazzList()) {
                if (clazz.getCanonicalName().equals(registerClazz.getCanonicalName())) {
                    hasNotFound = false;
                }
            }
            if (hasNotFound) {
                me.getRegisterClazzList().add(clazz);
                me.getKryo().register(clazz);
            }
        };
    }

    @Override
    public byte[] serialize(Object data, Class clazz) {
        registerClazzIfNecessary(new Class[]{clazz});
        //1、创建OutputStream对象
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeObject(output, data);
        return outputStream.toByteArray();
    }


    @Override
    public Object deserialize(byte[] data, Class clazz) {
        registerClazzIfNecessary(new Class[]{clazz});
        //1、创建OutputStream对象
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        Input input = new Input(inputStream);
        return kryo.readObject(input, clazz);

    }
}
