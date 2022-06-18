package com.jinninghui.newspiral.common.entity.sm9;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;


/**
 * SM9 master key pair.
 *
 * Created by on 2019/4/13.
 */
public final class MasterKeyPair {
    MasterPrivateKey prikey;
    MasterPublicKey pubkey;

    public MasterKeyPair(MasterPrivateKey privateKey, MasterPublicKey publicKey)
    {
        prikey = privateKey;
        pubkey = publicKey;
    }

    public static MasterKeyPair fromByteArray(SM9Curve curve, byte[] source) {
        int len = SM9CurveParameters.nBits/8;
        byte[] bPrikey = Arrays.copyOfRange(source, 0, len);
        byte[] bPubkey = Arrays.copyOfRange(source, len, source.length);
        return new MasterKeyPair(MasterPrivateKey.fromByteArray(bPrikey), MasterPublicKey.fromByteArray(curve, bPubkey));
    }

    public MasterPrivateKey getPrivateKey() {
        return prikey;
    }

    public MasterPublicKey getPublicKey() {
        return pubkey;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = prikey.toByteArray();
        bos.write(temp, 0, temp.length);
        temp = pubkey.toByteArray();
        bos.write(temp, 0, temp.length);
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("SM9 Master key pair:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(prikey.toString());
        sb.append(SM9Utils.NEW_LINE);
        sb.append(pubkey.toString());

        return sb.toString();
    }
}
