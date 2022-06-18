package com.jinninghui.newspiral.common.entity.sm9;

import java.security.Provider;

/**
 * GM algorithm provider.
 *
 * Created by  on 2019/1/18.
 */
public class GMProvider {
    private static Provider sProvider = null;

    /**
     * Get GM algorithm provider.
     *
     * @return GM algorithm provider.
     */
    public static Provider getProvider()
    {
        if(sProvider==null) {
            //sProvider = new BouncyCastleProvider();
        }
        return sProvider;
    }


    private GMProvider(){
    }
}
