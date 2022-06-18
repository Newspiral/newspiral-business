package com.jinninghui.newspiral.common.entity.sm9;

/**
 * SM9 key exchange result struct.
 *
 * Created by  on 2019/4/15.
 */
public class ResultKeyExchange {
    byte[] SK;
    byte[] SA2;
    byte[] SB1;

    public ResultKeyExchange(byte[] SK, byte[] SA2, byte[] SB1)
    {
        this.SK = SK;
        this.SA2 = SA2;
        this.SB1 = SB1;
    }

    public byte[] getSK() {
        return SK;
    }

    public byte[] getSA2() {
        return SA2;
    }

    public byte[] getSB1() {
        return SB1;
    }


}
