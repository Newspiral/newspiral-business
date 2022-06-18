package com.jinninghui.newspiral.common.entity.sm9;

import java.math.BigInteger;

/**
 * SM9 private key on G1 group.
 *
 * Created by  on 2019/4/15.
 */
public class G1PrivateKey {
    BigInteger d;

    public G1PrivateKey(BigInteger d) {
        this.d = d;
    }

    public static G1PrivateKey fromByteArray(byte[] source) {
        BigInteger d = new BigInteger(1, source);
        return new G1PrivateKey(d);
    }

    public byte[] toByteArray() {
        return SM9Utils.BigIntegerToBytes(d, SM9CurveParameters.nBits/8);
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("sm9 private key on G1 group:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(d, SM9CurveParameters.nBits/8)));

        return sb.toString();
    }
}
