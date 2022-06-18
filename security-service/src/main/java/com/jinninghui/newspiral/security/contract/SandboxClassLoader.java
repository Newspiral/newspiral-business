package com.jinninghui.newspiral.security.contract;

import java.util.HashMap;

public class SandboxClassLoader extends ClassLoader {
    final private ContractByteSource codesource;

    private WhiteListItem[] whitListClasses;

    private BlackListItem[] blackListItems;

    private HashMap<String, byte[]> innerMap;

    public SandboxClassLoader(ContractByteSource codesource, WhiteListItem[] whitListClasses,BlackListItem[] blackListItems,HashMap<String, byte[]> innerMap) {
        super();
        this.codesource = codesource;
        this.whitListClasses = whitListClasses;
        this.blackListItems = blackListItems;
        this.innerMap = innerMap;
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (BlackListItem black : blackListItems) {
            if (black.matches(name)){
                throw new ClassNotFoundException(name + "not available to this SmartContract");
            }
        }
        Class<?> result = findLoadedClass(name);
        if (result == null) {
            for (WhiteListItem item : whitListClasses) {
                if (item.matches(name)) {
                    if (item.loadHere) {
                        byte[] byteCode = innerMap.get(name);
                        if (null != byteCode) {
                            byteCode = codesource.getInnerByteCode(byteCode);
                        } else {
                            byteCode = codesource.getMainByteCode(name);
                        }
                        if (byteCode != null) {
                            //解析
                            result = defineClass(name, byteCode, 0, byteCode.length);
                        }
                    } else {
                        //白名单允许的情况下，如果该类是外部类，就交给上层进行加载
                        result = Thread.currentThread().getContextClassLoader().loadClass(name);
                    }
                    break;
                }
            }
        }
        if (result == null) {
            throw new ClassNotFoundException(name + " not found or not available to this SmartContract");
        }
        if (resolve) {
            resolveClass(result);
        }
        return result;
    }
}
