package com.jinninghui.newspiral.security.contract;


public interface ContractByteSource {
    byte[] getMainByteCode(String name) ;

    byte[] getInnerByteCode(byte[] bytes);
}
