package com.jinninghui.newspiral.security.impl;

import com.jinninghui.newspiral.common.entity.sm9.*;
import com.jinninghui.newspiral.security.serialize.KryoUtil;

/**
 * @version V1.0
 * @Title: sm9Test
 * @Package com.jinninghui.newspiral.common.entity.sm9
 * @Description:
 * @author: xuxm
 * @date: 2019/10/21 16:08
 */
public class sm9Test  {


    public static void main(String[] args)
    {
       /* try {
            SM9Curve sm9Curve = new SM9Curve();
            KGC kgc = new KGC(sm9Curve);
            SM9 sm9 = new SM9(sm9Curve);

            String id_A = "xuxm";

            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
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

        }*/

        //Sm3WithSm2Test();
        //privateKeyTest();
        //KryoTest();

        //test2();
        //test3();

        //test4();
        //test6();
        //test7("358C9B653D0C6F25467750BDA3DBE1B6B7966D8E8B606AD4978619BA0673E47F","peerId1OfOrg1Test");
        //test7("358C9B653D0C6F25467750BDA3DBE1B6B7966D8E8B606AD4978619BA0673E47F","peerId2OfOrg1Test");
        //test7("358C9B653D0C6F25467750BDA3DBE1B6B7966D8E8B606AD4978619BA0673E47F","peerId3OfOrg1Test");
        //test7("358C9B653D0C6F25467750BDA3DBE1B6B7966D8E8B606AD4978619BA0673E47F","peerId4OfOrg1Test");
        //test7("358C9B653D0C6F25467750BDA3DBE1B6B7966D8E8B606AD4978619BA0673E47F","peerId5OfOrg1Test");


    }

 /*   public static void  Sm3WithSm2Test()
    {
        // 生成公私钥对 ---------------------
        KeyPair kp = OsccaCinpher.generateKeyPair();

        System.out.println("1Pri:"+org.bouncycastle.util.encoders.Hex.toHexString(kp.getPrivate().getEncoded()));
        System.out.println("1Pub:"+org.bouncycastle.util.encoders.Hex.toHexString(kp.getPublic().getEncoded()));

        // 生成公私钥对 ---------------------
        KeyPair kp1 = OsccaCinpher.generateKeyPair();

        System.out.println("2Pri:"+org.bouncycastle.util.encoders.Hex.toHexString(kp1.getPrivate().getEncoded()));
        System.out.println("2Pub:"+org.bouncycastle.util.encoders.Hex.toHexString(kp1.getPublic().getEncoded()));
    }*/

    public static void privateKeyTest()
    {
        try {
            SM9Curve sm9Curve = new SM9Curve();
            KGC kgc = new KGC(sm9Curve);
            SM9 sm9 = new SM9(sm9Curve);

            String id_A = "xuxm";

            //生成签名主密钥对
            MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
            System.out.println("主私钥："+signMasterKeyPair.getPrivateKey().toString());
            //String aa= KryoObjectSerializer2.serialize(signMasterKeyPair.getPrivateKey(),MasterPrivateKey.class).toString();
            //System.out.println("主私钥序列化："+ aa );
            //反序列化
            //System.out.println("主私钥反序列化："+  ((MasterPrivateKey) KryoObjectSerializer2.deserialize(aa.getBytes(),MasterPrivateKey.class)).toString());
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

        String aa=KryoUtil.writeToString(signMasterKeyPair.getPrivateKey());
        System.out.println("序列化主私钥1："+ aa);
        MasterPrivateKey privateKey=KryoUtil.readFromString(aa);
        System.out.println("反序列化主私钥1："+ privateKey.toString());

        String jj=signMasterKeyPair.getPrivateKey().toString().replaceAll(" ","");
        System.out.println("去空格主私钥1："+ jj);
        byte[] bb=Hex.decode(jj);
        MasterPrivateKey privateKey1=MasterPrivateKey.fromByteArray(bb);
        System.out.println("主私钥1："+ privateKey1.toString());


        String a=KryoUtil.writeToString(signMasterKeyPair.getPublicKey());
        System.out.println("序列化主公钥1："+ a);
        MasterPublicKey publicKey=KryoUtil.readFromString(a);
        System.out.println("反序列化主公钥1："+ publicKey.toString());


        String j=signMasterKeyPair.getPublicKey().toString().replaceAll(" ","").replaceAll("\r|\n", "");
        System.out.println("去空格主公钥1："+ j);
        byte[] b=Hex.decode(j);
        MasterPublicKey publicKey2=MasterPublicKey.fromByteArray(sm9.getCurve(),signMasterKeyPair.getPublicKey().toString().getBytes());
        System.out.println("主公钥1："+ publicKey2.toString());

       byte[] n=signMasterKeyPair.getPublicKey().toByteArray();
        System.out.println("主公钥1 aray："+ n.toString());
        System.out.println("主公钥1 aray---："+SM9Utils.toHexString(n));
        MasterPublicKey publicKey3=MasterPublicKey.fromByteArray(new SM9Curve(),n);
        System.out.println("主公钥1--："+ publicKey3.toString());


        //重构签名主密钥对
        System.out.println("主密钥对重构测试:");
        MasterKeyPair signMasterKeyPair0 = MasterKeyPair.fromByteArray(new SM9Curve(), signMasterKeyPair.toByteArray());
        System.out.println("重构后的主密钥:");
        System.out.println(signMasterKeyPair0.toString());


    }

    public static void test3()
    {
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        SM9 sm9 = new SM9(sm9Curve);

        String id_A = "xuxm";

        //生成签名主密钥对
        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
        System.out.println("主公钥1："+signMasterKeyPair.getPublicKey().toString());
        System.out.println("主私钥1："+signMasterKeyPair.getPrivateKey().toString());
        System.out.println("----------------------------------------");

        byte[] aa=signMasterKeyPair.getPublicKey().toByteArray();
        System.out.println("主公钥1-array："+aa.toString());
        String ahex=SM9Utils.toHexString(aa);
        System.out.println("主公钥1-array-hex："+ahex);
        byte[] bb=signMasterKeyPair.getPrivateKey().toByteArray();
        System.out.println("主私钥1-array："+bb);
        String bhex=SM9Utils.toHexString(bb);
        System.out.println("主私钥1-array-hex："+bhex);

        System.out.println("----------------------------------------");
        System.out.println("主公钥1-还原："+MasterPublicKey.fromByteArray(new SM9Curve(),aa).toString());
        System.out.println("主私钥1-还原："+MasterPrivateKey.fromByteArray(bb).toString());

    }


    public static void test4()
    {
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        SM9 sm9 = new SM9(sm9Curve);

        String id_A = "xuxm";

        //生成签名主密钥对
        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
        System.out.println("主公钥1："+signMasterKeyPair.getPublicKey().toString());
        System.out.println("主私钥1："+signMasterKeyPair.getPrivateKey().toString());

        String a=KryoUtil.writeToString(signMasterKeyPair.getPublicKey());
        System.out.println("序列化主公钥1："+ a);
        MasterPublicKey publicKey=KryoUtil.readFromString(a);
        System.out.println("反序列化主公钥1："+ publicKey.toString());

        String aa=KryoUtil.writeToString(signMasterKeyPair.getPrivateKey());
        System.out.println("序列化主私钥1："+ aa);
        MasterPrivateKey privateKey=KryoUtil.readFromString(aa);
        System.out.println("反序列化主私钥1："+ privateKey.toString());

        MasterPublicKey publicKey1=MasterPublicKey.fromByteArray(new SM9Curve(),signMasterKeyPair.getPublicKey().toString());
        System.out.println("还原后的主公钥1："+ publicKey1.toString());
        MasterPrivateKey privateKey1=MasterPrivateKey.fromByteArray(signMasterKeyPair.getPrivateKey().toString());
        System.out.println("还原后的主私钥1："+ privateKey1.toString());

        if(signMasterKeyPair.getPublicKey().toString().equals(publicKey.toString())&&
                publicKey.toString().equals(publicKey1.toString())   )
        {
            System.out.println("主公钥还原:true");
        }
        else
        {
            System.out.println("主公钥还原:false");
        }

        if(signMasterKeyPair.getPrivateKey().toString().equals(privateKey.toString())&&
                privateKey.toString().equals(privateKey1.toString()))
        {
            System.out.println("主私钥还原:true");
        }
        else
        {
            System.out.println("主私钥还原:false");
        }

        System.out.println("-----------------------------");

        try {
            //生成签名私钥
            System.out.println("-------------私钥 start----------------");
            PrivateKey signPrivateKey = kgc.genPrivateKey(signMasterKeyPair.getPrivateKey(), id_A, PrivateKeyType.KEY_SIGN);
            System.out.println("私钥："+ signPrivateKey.toString());
            PrivateKey privateKey2=PrivateKey.fromByteArray(new SM9Curve(),signPrivateKey.toString());
            System.out.println("还原后私钥："+ privateKey2.toString());
            if(signPrivateKey.toString().equals(privateKey2.toString()))
            {
                System.out.println("私钥还原:true");
            }
            else
            {
                System.out.println("私钥还原:false");
            }
            System.out.println("-------------私钥 end----------------");
            String msg = "hahaha";
            ResultSignature signature = sm9.sign(signMasterKeyPair.getPublicKey(), signPrivateKey, msg.getBytes());
            System.out.println("签名signature value："+ signature.toString());
            ResultSignature signature1=ResultSignature.fromByteArray(new SM9Curve(),signature.toString());
            System.out.println("还原后签名signature value："+ signature1.toString());
            if(signature.toString().equals(signature1.toString()))
            {
                System.out.println("签名还原:true");
            }
            else
            {
                System.out.println("签名还原:false");
            }

        }
        catch (Exception e)
        {

        }


    }

    /**
     * 生成用户私钥
     */
    public static void test5()
    {
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        //解密后的主私钥
        String privateKey="";
        //用户标识
        String id_A="";
        MasterPrivateKey masterPrivateKey=MasterPrivateKey.fromByteArray(privateKey);
        try {
            PrivateKey signPrivateKey = kgc.genPrivateKey(masterPrivateKey, id_A, PrivateKeyType.KEY_SIGN);
            //生成用户私钥
            System.out.println("用户私钥："+signPrivateKey.toString());
        }
        catch (Exception e)
        {

        }

    }

    /**
     * 生成加密、解密的主密钥对
     */
    public static void test6()
    {
        EncryptMasterKeyPair encryptMasterKeyPair=SM9Mgrlmpl.getEncryptSignMasterKeyPair();

        System.out.println("加密主公钥："+encryptMasterKeyPair.getEncryptMasterPublicKey());
        System.out.println("加密主私钥："+encryptMasterKeyPair.getEncryptMasterPrivateKey());
        DecryptMasterKeyPair decryptMasterKeyPair =SM9Mgrlmpl.getDecryptSignMasterKeyPair(encryptMasterKeyPair);
        System.out.println("明文主公钥："+decryptMasterKeyPair.getDecryptMasterPublicKey());
        System.out.println("明文主私钥："+decryptMasterKeyPair.getDecryptMasterPrivateKey());

    }
    /**
     * 生成加密、解密的用户私钥
     */
    public static void test7(String masterPrivateKeyStr,String id)
    {
        String encryptPrivate=SM9Mgrlmpl.getEncryptPrivateKey(masterPrivateKeyStr,id);
        System.out.println(id+"-加密私钥："+encryptPrivate);
        System.out.println(id+"-解密私钥："+Crypto.decrypt(encryptPrivate));
    }




}
