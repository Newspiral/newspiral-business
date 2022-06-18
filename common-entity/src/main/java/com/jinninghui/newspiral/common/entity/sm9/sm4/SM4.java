package com.jinninghui.newspiral.common.entity.sm9.sm4;


import com.jinninghui.newspiral.common.entity.sm9.GMProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * SM4 Algorithm.
 *
 * @author
 * @since 2019/01/18
 */
public final class SM4 {
    public static final int KEY_BYTE_LENGTH = 16;
    public static final String ALGORITHM_NAME= "SM4";

    private SM4() {
    }

    public static byte[] ecbCrypt(boolean isEncrypt, byte[] key,  byte[] data, int offset, int length) throws Exception
    {
        byte[] cipherText;

            try {
                SecretKey secretKey = new SecretKeySpec(key, ALGORITHM_NAME);

                Cipher cipher = Cipher.getInstance(getAlgorithm() + "/ECB/PKCS5Padding", getCipherProvider());
                if(isEncrypt)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                else
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);

                cipherText = cipher.doFinal(data);

            } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException
                    | NoSuchPaddingException | IllegalBlockSizeException e) {
                throw new Exception("SM4 ECB crypt failed." + e.getMessage());
            }

        return cipherText;
    }

    public static Provider getCipherProvider() {
        return GMProvider.getProvider();
    }

    public static String getAlgorithm() {
        return ALGORITHM_NAME;
    }


}
