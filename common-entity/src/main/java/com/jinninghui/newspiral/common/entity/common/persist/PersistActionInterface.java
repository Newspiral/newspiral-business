package com.jinninghui.newspiral.common.entity.common.persist;

public interface PersistActionInterface<T> {


    void doAdd(T obj);

    void doRemove(T obj);

    void doModify(T obj);

    void doRest(T obj);
}
