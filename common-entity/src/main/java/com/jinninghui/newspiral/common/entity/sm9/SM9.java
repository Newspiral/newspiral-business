package com.jinninghui.newspiral.common.entity.sm9;

import com.jinninghui.newspiral.common.entity.sm9.*;
import com.jinninghui.newspiral.common.entity.sm9.sm4.SM4;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/**
 * SM9 algorithm.
 *
 * Created by on 2019/4/13.
 */
public class SM9 {
    protected SM9Curve mCurve;

    public SM9(SM9Curve curve){
        mCurve = curve;
    }

    public SM9Curve getCurve() {
        return mCurve;
    }

    /**
     * SM9 key encapsulate.
     *
     * @param masterPublicKey encrypt master public key
     * @param id other's ID
     * @param keyByteLen key byte length
     * @return A random key and key cipher text.
     */
    public ResultEncapsulate keyEncapsulate(MasterPublicKey masterPublicKey, String id, int keyByteLen)
    {
        byte[] K;
        CurveElement C;

        //Step1 : QB=[H1(IDB||hid, N)]P1+Ppub-e
        BigInteger h1 = SM9Utils.H1(id, SM9Curve.HID_ENCRYPT, mCurve.N);
        CurveElement QB = mCurve.P1.duplicate().mul(h1).add(masterPublicKey.Q);

        do {
            //Step2: generate r
            BigInteger r = SM9Utils.genRandom(mCurve.random, mCurve.N);

            //Step3 : C=[r]QB
            C = QB.mul(r);

            //Step4 : g=e(Ppub-e, P2)
            Element g = mCurve.pairing(masterPublicKey.Q, mCurve.P2);

            //Step5 : calculate w=g^r
            Element w = g.duplicate().pow(r);

            //Step6 : K = KDF(C || w || IDB, klen)
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] temp = SM9Utils.G1ElementToBytes(C);
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.GTFiniteElementToByte(w);
            bos.write(temp, 0, temp.length);
            temp = id.getBytes();
            bos.write(temp, 0, temp.length);
            K = SM9Utils.KDF(bos.toByteArray(), keyByteLen);

        } while (SM9Utils.isAllZero(K));

        //Step7 : output (K,C)
        return new ResultEncapsulate(K, new ResultEncapsulateCipherText(C));
    }

    /**
     * SM9 key decapsulate.
     *
     * @param privateKey encrypt private key
     * @param id myself ID
     * @param keyByteLen key byte length. Should same with {@link #keyEncapsulate(MasterPublicKey, String, int)}.
     * @param cipherText key cipher text from {@link #keyEncapsulate(MasterPublicKey, String, int)}.
     * @return The key.
     * @throws Exception If error occurs.
     */
    public byte[] keyDecapsulate(PrivateKey privateKey, String id, int keyByteLen, ResultEncapsulateCipherText cipherText) throws Exception
    {
        // Step1 : check if C is on G1
        if(!cipherText.C.isValid())
            throw new Exception("C is not on G1");

        //Step2 : calculate w=e(C,de)
        Element w = mCurve.pairing(cipherText.C, privateKey.d);

        //Step3 : K = KDF(C || w || IDB, klen)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = SM9Utils.G1ElementToBytes(cipherText.C);
        bos.write(temp, 0, temp.length);
        temp = SM9Utils.GTFiniteElementToByte(w);
        bos.write(temp, 0, temp.length);
        temp = id.getBytes();
        bos.write(temp, 0, temp.length);
        byte[] K = SM9Utils.KDF(bos.toByteArray(), keyByteLen);

        if(SM9Utils.isAllZero(K))
            throw new Exception("K is all zero");

        //Step4 : output K
        return K;
    }

    /**
     * SM9 encryption.
     *
     * @param masterPublicKey encrypt master public key
     * @param id other's ID
     * @param data source data
     * @param isBaseBlockCipher true present use base block cipher way; false present use KDF way
     * @param macKeyByteLen the key length of {@link SM9Utils#MAC(byte[], byte[])}, 32 bytes(Hash algorithm digest size of SM3) at least.
     * @return Cipher text, including C1, C2, C3.
     * @throws Exception If error occurs.
     */
    public ResultCipherText encrypt(MasterPublicKey masterPublicKey, String id,
                                    byte[] data, boolean isBaseBlockCipher, int macKeyByteLen)
            throws Exception
    {
        CurveElement C1;
        byte[] C2, C3, K1, K2;

        //Step1 : QB=[H1(IDB||hid, N)]P1+Ppub-e
        BigInteger h1 = SM9Utils.H1(id, SM9Curve.HID_ENCRYPT, mCurve.N);
        CurveElement QB = mCurve.P1.duplicate().mul(h1).add(masterPublicKey.Q);

        do {
            //Step2: generate r
            BigInteger r = SM9Utils.genRandom(mCurve.random, mCurve.N);

            //Step3 : C1=[r]QB
            C1 = QB.mul(r);

            //Step4 : g=e(Ppub-e, P2)
            Element g = mCurve.pairing(masterPublicKey.Q, mCurve.P2);

            //Step5 : calculate w=g^r
            Element w = g.duplicate().pow(r);

            //Step6_1 : K = KDF(C1 || w || IDB, klen)
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] temp = SM9Utils.G1ElementToBytes(C1);
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.GTFiniteElementToByte(w);
            bos.write(temp, 0, temp.length);
            temp = id.getBytes();
            bos.write(temp, 0, temp.length);

            int k1Len = SM4.KEY_BYTE_LENGTH;
            if(!isBaseBlockCipher)
                k1Len = data.length;

            byte[] K = SM9Utils.KDF(bos.toByteArray(), k1Len+ macKeyByteLen);
            K1 = Arrays.copyOfRange(K, 0, k1Len);
            K2 = Arrays.copyOfRange(K, k1Len, K.length);
        } while(SM9Utils.isAllZero(K1));

        //Step6_2
        if(isBaseBlockCipher) {
            //C2=Enc(K1,M)
            C2 = SM4.ecbCrypt(true, K1, data, 0, data.length);
        } else {
            //C2=M^K1
            C2 = SM9Utils.xor(data, K1);
        }

        //Step7 : C3=MAC(K2,C2)
        C3 = SM9Utils.MAC(K2, C2);

        //Step8 : C=C1|C3|C2
        return new ResultCipherText(C1, C2, C3);
    }

    /**
     * SM9 decryption.
     *
     * @param resultCipherText cipher text from {@link #encrypt(MasterPublicKey, String, byte[], boolean, int)}
     * @param privateKey encrypt private key
     * @param id myself memberId
     * @param isBaseBlockCipher true present use base block cipher way; false present use KDF way
     * @param macKeyByteLen the key length of {@link SM9Utils#MAC(byte[], byte[])}, 32 bytes(Hash algorithm digest size of SM3) at least.
     * @return source data.
     * @throws Exception If error occurs.
     */
    public byte[] decrypt(ResultCipherText resultCipherText, PrivateKey privateKey, String id,
                          boolean isBaseBlockCipher, int macKeyByteLen)
            throws Exception
    {
        // Step1 : check if C1 is on G1
        if(!resultCipherText.C1.isValid())
            throw new Exception("C1 is not on G1 group");

        // Step2 : w=e(C1,de)
        Element w = mCurve.pairing(resultCipherText.C1, privateKey.d);

        // Step3_1 : K = KDF(C1 || w || IDB, klen)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp = SM9Utils.G1ElementToBytes(resultCipherText.C1);
        bos.write(temp, 0, temp.length);
        temp = SM9Utils.GTFiniteElementToByte(w);
        bos.write(temp, 0, temp.length);
        temp = id.getBytes();
        bos.write(temp, 0, temp.length);

        int k1Len = SM4.KEY_BYTE_LENGTH;
        if(!isBaseBlockCipher)
            k1Len = resultCipherText.C2.length;

        byte[] K = SM9Utils.KDF(bos.toByteArray(),k1Len+macKeyByteLen);
        byte[] K1 = Arrays.copyOfRange(K, 0, k1Len);
        byte[] K2 = Arrays.copyOfRange(K, k1Len, K.length);

        if(SM9Utils.isAllZero(K1))
            throw new Exception("K1 is all zero");

        // Step3_2
        byte[] M;
        if( isBaseBlockCipher ) {
            // M=Dec(K1,C2)
            M = SM4.ecbCrypt(false, K1, resultCipherText.C2, 0, resultCipherText.C2.length);
        } else {
            // M=C2^K1
            M = SM9Utils.xor(resultCipherText.C2, K1);
        }

        // Step4 : u=MAC(K2,C2)
        byte[] u = SM9Utils.MAC(K2, resultCipherText.C2);
        if(!SM9Utils.byteEqual(u, resultCipherText.C3))
            throw new Exception("C3 verify failed");

        // Step5
        return M;
    }

    /**
     * SM9 key exchange, step 1: generate a key pair on G1 group.
     *
     * @param masterPublicKey encrypt master public key
     * @param id other's ID
     * @return A key pair on G1 group.
     */
    public G1KeyPair keyExchangeInit(MasterPublicKey masterPublicKey, String id)
    {
        //Step1 : QB =[H1(IDB||hid, N)]P1 +Ppub-e or QA = [H1(IDA || hid, N)]P1 + Ppub-e
        BigInteger h1 = SM9Utils.H1(id, SM9Curve.HID_KEY_EXCHANGE, mCurve.N);
        CurveElement QB = mCurve.P1.duplicate().mul(h1).add(masterPublicKey.Q);

        //Step2: generate r
        BigInteger r = SM9Utils.genRandom(mCurve.random, mCurve.N);

        //Step3 : RA = [rA]QB or RB= [rB]QA
        CurveElement R = QB.mul(r);

        return new G1KeyPair(new G1PrivateKey(r), new G1PublicKey(R));
    }

    /**
     * SM9 key exchange, step 2: calculate share key and options hash values.
     *
     * @param masterPublicKey encrypt master public key
     * @param isSponsor true present sponsor; false present responder
     * @param myId myself ID
     * @param othId other's ID
     * @param myPrivateKey myself encrypt private key
     * @param myTempKeyPair myself key pair on G1 group
     * @param othTempPublicKey other's public key on G1 group
     * @param keyByteLen the byte length of key want to exchange
     * @return A key and two option hash value.
     * @throws Exception  If error occurs.
     */
    public ResultKeyExchange keyExchange(MasterPublicKey masterPublicKey, boolean isSponsor, String myId, String othId,
                                         PrivateKey myPrivateKey, G1KeyPair myTempKeyPair, G1PublicKey othTempPublicKey, int keyByteLen)
            throws Exception
    {
        //check R is on G1
        if(!othTempPublicKey.Q.isValid())
            throw new Exception("R is not on G1");

        //StepA5_B4
        Element g1, g2, g3;
        Element gTemp0 = mCurve.pairing(masterPublicKey.Q, mCurve.P2);
        Element gTemp2 = mCurve.pairing(othTempPublicKey.Q, myPrivateKey.d);
        if(isSponsor) {
            g1 = gTemp0.pow(myTempKeyPair.prikey.d);
            g2 = gTemp2.duplicate();
        } else {
            g1 = gTemp2.duplicate();
            g2 = gTemp0.pow(myTempKeyPair.prikey.d);
        }
        g3 = gTemp2.pow(myTempKeyPair.prikey.d);

        //Step6 : S1 or SB
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] temp;
        if(isSponsor) {
            temp = myId.getBytes();
            bos.write(temp, 0, temp.length);
            temp = othId.getBytes();
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.G1ElementToBytes(myTempKeyPair.pubkey.Q);
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.G1ElementToBytes(othTempPublicKey.Q);
            bos.write(temp, 0, temp.length);
        } else {
            temp = othId.getBytes();
            bos.write(temp, 0, temp.length);
            temp = myId.getBytes();
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.G1ElementToBytes(othTempPublicKey.Q);
            bos.write(temp, 0, temp.length);
            temp = SM9Utils.G1ElementToBytes(myTempKeyPair.pubkey.Q);
            bos.write(temp, 0, temp.length);
        }
        byte[] bIDR = bos.toByteArray();

        bos.reset();
        temp = SM9Utils.GTFiniteElementToByte(g2);
        bos.write(temp, 0, temp.length);
        temp = SM9Utils.GTFiniteElementToByte(g3);
        bos.write(temp, 0, temp.length);
        byte[] bG2G3 = bos.toByteArray();

        byte[] bG1 = SM9Utils.GTFiniteElementToByte(g1);

        bos.reset();
        bos.write(bG2G3, 0, bG2G3.length);
        bos.write(bIDR, 0, bIDR.length);
        byte[] bHashIDRG2G3 = SM9Utils.Hash(bos.toByteArray());

        //SB1
        bos.reset();
        bos.write(0x82);
        bos.write(bG1, 0, bG1.length);
        bos.write(bHashIDRG2G3, 0, bHashIDRG2G3.length);
        byte[] SB1 = SM9Utils.Hash(bos.toByteArray());

        //StepA8_B7 : SA or S2
        bos.reset();
        bos.write(0x83);
        bos.write(bG1, 0, bG1.length);
        bos.write(bHashIDRG2G3, 0, bHashIDRG2G3.length);
        byte[] SA2 = SM9Utils.Hash(bos.toByteArray());

        //StepA7_B5 : SKA or SKB
        bos.reset();
        bos.write(bIDR, 0, bIDR.length);
        bos.write(bG1, 0, bG1.length);
        bos.write(bG2G3, 0, bG2G3.length);
        byte[] SK = SM9Utils.KDF(bos.toByteArray(), keyByteLen);

        return new ResultKeyExchange(SK, SA2, SB1);
    }

    /**
     * SM9 sign.
     *
     * @param masterPublicKey signed master public key
     * @param privateKey signed private key
     * @param data source data
     * @return signature value, including h and s.
     */
    public ResultSignature sign(MasterPublicKey masterPublicKey, PrivateKey privateKey, byte[] data)
    {
        BigInteger l, h;

        //Step1 : g = e(P1, Ppub-s)
        Element g = mCurve.pairing(mCurve.P1, masterPublicKey.Q);

        do {
            //Step2: generate r
            BigInteger r = SM9Utils.genRandom(mCurve.random, mCurve.N);

            //Step3 : calculate w=g^r
            Element w = g.duplicate().pow(r);

            //Step4 : calculate h=H2(M||w,N)
            h = SM9Utils.H2(data, w, mCurve.N);

            //Step5 : l=(r-h)mod N
            l = r.subtract(h).mod(mCurve.N);
        } while(l.equals(BigInteger.ZERO));

        //Step6 : S=[l]dSA=(xS,yS)
        CurveElement s = privateKey.d.duplicate().mul(l);

        //Step7 : signature=(h,s)
        return new ResultSignature(h, s);
    }

    /**
     * SM9 verify.
     *
     * @param masterPublicKey signed master public key
     * @param id signed private key
     * @param data source data
     * @param signature SM9 signature value for source data
     * @return true present verify success.
     */
    public boolean verify(MasterPublicKey masterPublicKey, String id, byte[] data, ResultSignature signature)
    {
        // Step1 : check if h in the range [1, N-1]
        if(!SM9Utils.isBetween(signature.h, mCurve.N))
            return false;

        // Step2 : check if S is on G1
        if(!signature.s.isValid())
            return false;

        // Step3 : g = e(P1, Ppub-s)
        Element g = mCurve.pairing(mCurve.P1, masterPublicKey.Q);

        // Step4 : calculate t=g^h
        Element t = g.pow(signature.h);

        // Step5 : calculate h1=H1(IDA||hid,N)
        BigInteger h1 = SM9Utils.H1(id, SM9Curve.HID_SIGN, mCurve.N);

        // Step6 : P=[h1]P2+Ppubs
        CurveElement p = mCurve.P2.duplicate().mul(h1).add(masterPublicKey.Q);

        // Step7 : u=e(S,P)
        Element u = mCurve.pairing(signature.s, p);

        // Step8 : w=u*t
        Element w2 = u.mul(t);

        // Step9 : h2=H2(M||w,N)
        BigInteger h2 = SM9Utils.H2(data, w2, mCurve.N);

        return h2.equals(signature.h);
    }

}
