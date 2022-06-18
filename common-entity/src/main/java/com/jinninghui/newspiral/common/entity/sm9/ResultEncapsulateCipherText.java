package com.jinninghui.newspiral.common.entity.sm9;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

/**
 * The cipher text of key in SM9 key encapsulation result struct.
 *
 * Created by  on 2019/4/14.
 */
public final class ResultEncapsulateCipherText {
    CurveElement C;

    public ResultEncapsulateCipherText(CurveElement C) {
        this.C = C;
    }

    public static ResultEncapsulateCipherText fromByteArray(SM9Curve curve, byte[] data) {
        CurveElement e = curve.G1.newElement();
        e.setFromBytes(data);
        return new ResultEncapsulateCipherText(e);
    }

    public byte[] toByteArray()
    {
        return C.toBytes();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("sm9 key encapsulate cipher text of key:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(SM9Utils.G1ElementToBytes(C)));

        return sb.toString();
    }
}
