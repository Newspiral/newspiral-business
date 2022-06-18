package com.jinninghui.newspiral.common.entity.sm9;

/**
 * SM9 key encapsulation result struct.
 *
 * Created by  on 2019/4/14.
 */
public final class ResultEncapsulate {
    byte[] K;
    ResultEncapsulateCipherText C;

    public ResultEncapsulate(byte[] K, ResultEncapsulateCipherText C) {
        this.K = K;
        this.C = C;
    }

    public byte[] getK() {
        return K;
    }

    public ResultEncapsulateCipherText getC() {
        return C;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("sm9 key encapsulate result:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append("K:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(K));
        sb.append(SM9Utils.NEW_LINE);
        sb.append(C.toString());

        return sb.toString();
    }
}
