package com.jinninghui.newspiral.common.entity.sm9;

/**
 * @version V1.0
 * @Title: sm9Test
 * @Package com.jinninghui.newspiral.common.entity.sm9
 * @Description:
 * @author: xuxm
 * @date: 2019/10/21 16:08
 */
public class sm9Test {
    public static void main(String[] args)
    {
        //test1();
        sm9Test();
    }

    public static void sm9Test()
    {
        try {
            SM9Curve sm9Curve = new SM9Curve();
            KGC kgc = new KGC(sm9Curve);
            SM9 sm9 = new SM9(sm9Curve);

            String id_A = "xuxm";

            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
            //生成签名私钥
            PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
            PrivateKey signPrivateKey2 = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);

            System.out.println("私钥1："+signPrivateKey.toString());
            System.out.println("私钥2："+signPrivateKey2.toString());
            String msg = "hahaha";

            ResultSignature signature = sm9.sign(signMasterKeyPair.getPublicKey(), signPrivateKey, msg.getBytes());

            //验签
            if(sm9.verify(signMasterKeyPair.getPublicKey(), id_A, msg.getBytes(), signature))
                System.out.println("verify OK");
            else
                System.out.println("verify failed");
        }

        catch (Exception e)
        {

        }
    }



    public void privateKeyTest()
    {
        try {
            SM9Curve sm9Curve = new SM9Curve();
            KGC kgc = new KGC(sm9Curve);
            SM9 sm9 = new SM9(sm9Curve);

            String id_A = "xuxm";

            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
            System.out.println("主私钥："+signMasterKeyPair.getPrivateKey().toString());

            //生成签名私钥
            PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
            String msg = "hahaha";

            ResultSignature signature = sm9.sign(signMasterKeyPair.getPublicKey(), signPrivateKey, msg.getBytes());

            //验签
            if(sm9.verify(signMasterKeyPair.getPublicKey(), id_A, msg.getBytes(), signature))
                System.out.println("verify OK");
            else
                System.out.println("verify failed");
        }

        catch (Exception e)
        {

        }
    }

    public static void test1()
    {
        try {
            SM9Curve sm9Curve = new SM9Curve();
            KGC kgc = new KGC(sm9Curve);
            SM9 sm9 = new SM9(sm9Curve);

            String id_A = "xuxm";

            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
            System.out.println("主公钥1："+signMasterKeyPair.getPublicKey().toString());
            System.out.println("主私钥1："+signMasterKeyPair.getPrivateKey().toString());


            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair2 = kgc.genSignMasterKeyPair();
            System.out.println("主公钥2："+signMasterKeyPair2.getPublicKey().toString());
            System.out.println("主私钥2："+signMasterKeyPair2.getPrivateKey().toString());
        }
        catch (Exception e)
        {

        }

    }

    public static void test2()
    {
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        SM9 sm9 = new SM9(sm9Curve);

        String id_A = "xuxm";

        //生成签名主密钥对
        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
        System.out.println("主公钥1："+signMasterKeyPair.getPublicKey().toString());
        System.out.println("主私钥1："+signMasterKeyPair.getPrivateKey().toString());

        System.out.println("序列化主私钥1："+signMasterKeyPair.getPrivateKey());

    }


}
