package com.jinninghui.newspiral.common.entity.sm9;

/**
 * SM9 private key type, use to generate private key.
 *
 * Created by on 2019/4/15.
 */
public enum PrivateKeyType {
    /** SM9 signed private key. */
    KEY_SIGN,
    /** SM9 key exchange private key(also is a encrypted private key). */
    KEY_KEY_EXCHANGE,
    /** SM9 encrypted private key. */
    KEY_ENCRYPT
}
