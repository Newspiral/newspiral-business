package com.jinninghui.newspiral.common.entity.sm9;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.math.BigInteger;

/**
 * SM9 Key Generator Center.
 *
 * Created by  on 2019/4/13.
 */
public class KGC {
    protected SM9Curve mCurve;
    
    public KGC(SM9Curve curve) {
        mCurve = curve;
    }

    public SM9Curve getCurve() {
        return mCurve;
    }

    /**
     * Generate SM9 signed master key pair.
     */
    public MasterKeyPair genSignMasterKeyPair()
    {
        BigInteger ks = SM9Utils.genRandom(mCurve.random, mCurve.N);
        CurveElement ppubs = mCurve.P2.duplicate().mul(ks);
        return new MasterKeyPair(new MasterPrivateKey(ks), new MasterPublicKey(ppubs, true));
    }

    /**
     * Generate SM9 encrypted master key pair.
     */
    public MasterKeyPair genEncryptMasterKeyPair()
    {
        BigInteger ke = SM9Utils.genRandom(mCurve.random, mCurve.N);
        CurveElement ppube = mCurve.P1.duplicate().mul(ke);
        return new MasterKeyPair(new MasterPrivateKey(ke), new MasterPublicKey(ppube, false));
    }

    protected BigInteger T2(MasterPrivateKey privateKey, String id, byte hid) throws Exception {
        BigInteger h1 = SM9Utils.H1(id, hid, mCurve.N);
        BigInteger t1 = h1.add(privateKey.d).mod(mCurve.N);
        if(t1.equals(BigInteger.ZERO))
            throw new Exception("Need to update the master private key");

        return privateKey.d.multiply(t1.modInverse(mCurve.N)).mod(mCurve.N);
    }

    /**
     * Generate SM9 private key.
     *
     * @param masterPrivateKey master private key
     * @param id user ID
     * @param privateKeyType master private key type
     * @return A master private key
     * @throws Exception If error occurs.
     */
    public PrivateKey genPrivateKey(MasterPrivateKey masterPrivateKey, String id, PrivateKeyType privateKeyType) throws Exception {
        if(privateKeyType==PrivateKeyType.KEY_SIGN)
            return genSignPrivateKey(masterPrivateKey, id);
        else if(privateKeyType==PrivateKeyType.KEY_KEY_EXCHANGE)
            return genEncryptPrivateKey(masterPrivateKey, id, SM9Curve.HID_KEY_EXCHANGE);
        else if(privateKeyType==PrivateKeyType.KEY_ENCRYPT)
            return genEncryptPrivateKey(masterPrivateKey, id, SM9Curve.HID_ENCRYPT);
        else
            throw new Exception("Not support private key type");
    }

    PrivateKey genSignPrivateKey(MasterPrivateKey privateKey, String id) throws Exception {
        BigInteger t2 = T2(privateKey, id, SM9Curve.HID_SIGN);
        CurveElement ds = mCurve.P1.duplicate().mul(t2);
        return new PrivateKey(ds, SM9Curve.HID_SIGN);
    }

    PrivateKey genEncryptPrivateKey(MasterPrivateKey privateKey, String id, byte hid) throws Exception {
        BigInteger t2 = T2(privateKey, id, hid);
        CurveElement de = mCurve.P2.duplicate().mul(t2);
        return new PrivateKey(de, hid);
    }
}
