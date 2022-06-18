package com.jinninghui.newspiral.common.entity.sm9;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * SM9 signature value.
 *
 * Created by  on 2019/4/13.
 */
public final class ResultSignature {
    BigInteger h;
    CurveElement s;

    public ResultSignature(BigInteger h, CurveElement s)
    {
        this.h = h;
        this.s = s;
    }

    public static ResultSignature fromByteArray(SM9Curve curve, byte[] data) {
        byte[] bh = Arrays.copyOfRange(data, 0, SM9CurveParameters.nBits/8);
        byte[] bs = Arrays.copyOfRange(data, SM9CurveParameters.nBits/8, data.length);

        CurveElement e = curve.G1.newElement();
        e.setFromBytes(bs);
        return new ResultSignature(new BigInteger(1, bh), e);
    }

    public byte[] toByteArray()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = SM9Utils.BigIntegerToBytes(h, SM9CurveParameters.nBits/8);
        bos.write(temp, 0, temp.length);
        temp = s.toBytes();
        bos.write(temp, 0, temp.length);
        return bos.toByteArray();
    }

    /**
     *
     * @param curve
     * @param hex
     * @return
     */
    public static ResultSignature fromByteArray(SM9Curve curve, String hex) {
        return fromByteArray( curve, Hex.decode(hex));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
/*        sb.append("sm9 signature:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append("h:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(h)));
        sb.append("s:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(SM9Utils.G1ElementToBytes(s)));
        sb.append(SM9Utils.NEW_LINE);*/

        sb.append(SM9Utils.toHexString(toByteArray()));

        return sb.toString();
    }
}
