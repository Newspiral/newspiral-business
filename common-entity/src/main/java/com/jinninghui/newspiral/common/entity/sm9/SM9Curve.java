package com.jinninghui.newspiral.common.entity.sm9;

import com.jinninghui.newspiral.common.entity.sm9.pairing.SM9Pairing;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.field.gt.GTFiniteField;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * SM9 curve.
 *
 * Created by on 2019/4/13.
 */
public class SM9Curve {
    SecureRandom random;
    BigInteger N; //order
    CurveField G1; //G1 group
    CurveField G2; //G2 group
    GTFiniteField GT; //GT group
    SM9Pairing sm9Pairing; //SM9 Rate pairing
    CurveElement P1; //base point P1 for G1 group
    CurveElement P2; //base point P2 for G2 group

    protected static final byte HID_SIGN = 0x01; //hid for sign
    protected static final byte HID_KEY_EXCHANGE = 0x02; //hid for key exchange
    protected static final byte HID_ENCRYPT = 0x03; //hid for encryption

    public SM9Curve() {
        this(new SecureRandom());
    }

    public SM9Curve(SecureRandom random) {
        this.random = random;

        PairingParameters parameters = SM9CurveParameters.createSM9PropertiesParameters();

        this.sm9Pairing = new SM9Pairing(random, parameters);

        this.N = sm9Pairing.getN();
        this.G1 = (CurveField) sm9Pairing.getG1();
        this.G2 = (CurveField) sm9Pairing.getG2();
        this.GT = (GTFiniteField) sm9Pairing.getGT();

        //set P1
        P1 = G1.newElement();
        P1.setFromBytes(SM9CurveParameters.P1_bytes);

        //set P2
        P2 = G2.newElement();
        P2.setFromBytes(SM9CurveParameters.P2_bytes);
    }

    public Element pairing(CurveElement p1, CurveElement p2) {
        return sm9Pairing.pairing(p1, p2);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        PairingParameters pairingParameters = sm9Pairing.getPairingParameters();

        sb.append("----------------------------------------------------------------------");
        sb.append(newLine);
        sb.append("SM9 curve parameters:");
        sb.append(newLine);


        sb.append("b:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("b"))));
        sb.append(newLine);

        sb.append("t:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("t"))));
        sb.append(newLine);

        sb.append("q:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("q"))));
        sb.append(newLine);

        sb.append("N:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("r"))));
        sb.append(newLine);

        sb.append("beta:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("beta"))));
        sb.append(newLine);

        sb.append("alpha0:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("alpha0"))));
        sb.append(newLine);

        sb.append("alpha1:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.BigIntegerToBytes(pairingParameters.getBigInteger("alpha1"))));
        sb.append(newLine);

        sb.append("P1:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.G1ElementToBytes(P1)));
        sb.append(newLine);

        sb.append("P2:\n");
        sb.append(SM9Utils.toHexString(SM9Utils.G2ElementToByte(P2)));

        sb.append("----------------------------------------------------------------------");
        sb.append(newLine);

        return sb.toString();
    }

}
