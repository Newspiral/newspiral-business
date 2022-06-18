package com.jinninghui.newspiral.common.entity.sm9;

import com.jinninghui.newspiral.common.entity.sm9.sm3.SM3;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * SM9 cipher struct for encrypt/decrypt.
 *
 * Created by  on 2019/4/15.
 */
public final class ResultCipherText {
    CurveElement C1;
    byte[] C2;
    byte[] C3;

    public ResultCipherText(CurveElement C1, byte[] C2, byte[] C3) {
        this.C1 = C1;
        this.C2 = C2;
        this.C3 = C3;
    }
    public static ResultCipherText fromByteArray(SM9Curve curve, byte[] data) {
        int offset = 0;
        byte[] bC1 = Arrays.copyOfRange(data, offset, offset + 32*2);
        offset += 32*2;
        CurveElement C1 = curve.G1.newElement();
        C1.setFromBytes(bC1);

        byte[] bC3 = Arrays.copyOfRange(data, offset, offset + SM3.DIGEST_SIZE);
        offset += SM3.DIGEST_SIZE;

        byte[] bC2 = Arrays.copyOfRange(data, offset, data.length);

        return new ResultCipherText(C1, bC2, bC3);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = C1.toBytes();
        bos.write(temp, 0, temp.length);
        bos.write(C3, 0, C3.length);
        bos.write(C2, 0, C2.length);
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("SM9 encrypt cipher:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append("C1:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(SM9Utils.G1ElementToBytes(C1)));
        sb.append(SM9Utils.NEW_LINE);
        sb.append("C2:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(C2));
        sb.append(SM9Utils.NEW_LINE);
        sb.append("C3:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(SM9Utils.toHexString(C3));
        sb.append(SM9Utils.NEW_LINE);

        return sb.toString();
    }
}
