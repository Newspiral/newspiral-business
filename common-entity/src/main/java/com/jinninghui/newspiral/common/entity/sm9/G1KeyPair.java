package com.jinninghui.newspiral.common.entity.sm9;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * SM9 key pair on G1 group.
 *
 * Created by  on 2019/4/15.
 */
public final class G1KeyPair {
    G1PrivateKey prikey;
    G1PublicKey pubkey;

    public G1KeyPair(G1PrivateKey privateKey, G1PublicKey publicKey)
    {
        this.prikey = privateKey;
        this.pubkey = publicKey;
    }

    public G1PrivateKey getPrivateKey() {
        return prikey;
    }

    public G1PublicKey getPublicKey() {
        return pubkey;
    }

    public static G1KeyPair fromByteArray(SM9Curve curve, byte[] source) {
        int len = SM9CurveParameters.nBits/8;
        byte[] bPrikey = Arrays.copyOfRange(source, 0, len);
        byte[] bPubkey = Arrays.copyOfRange(source, len, source.length);
        return new G1KeyPair(G1PrivateKey.fromByteArray(bPrikey), G1PublicKey.fromByteArray(curve, bPubkey));
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

        sb.append("SM9 key pair on G1 group:");
        sb.append(SM9Utils.NEW_LINE);
        sb.append(prikey.toString());
        sb.append(SM9Utils.NEW_LINE);
        sb.append(pubkey.toString());

        return sb.toString();
    }
}
