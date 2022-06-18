package com.jinninghui.newspiral.common.entity.sm9;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.io.ByteArrayOutputStream;

/**
 * SM9 private key.
 *
 * A sign private key is a multiplying point with t2 of the base point of G1 group.
 * A encrypt private key is a multiplying point with t2 of the base point of G2 group.
 *
 * Created by on 2019/4/13.
 */
public class PrivateKey {
    CurveElement d;
    byte hid;

    public PrivateKey(CurveElement point, byte hid) {
        this.d = point;
        this.hid = hid;
    }

    public static PrivateKey fromByteArray(SM9Curve curve, byte[] source) {
        byte hid = source[0];
        CurveElement d;
        if(hid==SM9Curve.HID_SIGN)
            d = curve.G1.newElement();
        else
            d = curve.G2.newElement();
        d.setFromBytes(source, 1);

        return new PrivateKey(d, hid);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(hid);
        byte[] temp = d.toBytes();
        bos.write(temp, 0, temp.length);
        return bos.toByteArray();
    }

    /**
     *
     * @param curve
     * @param hex
     * @return
     */
    public static PrivateKey fromByteArray(SM9Curve curve, String hex) {
        return  fromByteArray( curve, Hex.decode(hex));
    }

    @Override
    public String toString() {
/*        if(hid==SM9Curve.HID_SIGN)
            return "SM9 private key:"+SM9Utils.NEW_LINE+SM9Utils.toHexString(SM9Utils.G1ElementToBytes(d));
        else
            return "SM9 private key:"+SM9Utils.NEW_LINE+SM9Utils.toHexString(SM9Utils.G2ElementToByte(d));*/

        return SM9Utils.toHexString(toByteArray());
    }
}
