package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.Hashable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * @author lida
 * @date 2019/7/11 16:16
 * 交易池中的交易，由客户端的execute交易调用请求产生
 */
@ApiModel(description = "交易池中的交易")
@Data
public class PooledTransaction implements Hashable {
    @ApiModelProperty(value = "交易相关hash",hidden = true)
    String hash;
    /**
     * 数据结构版本号，这个是指PooledTransaction的版本号，与SDKTransaction版本号不一定完全一样
     */
    @ApiModelProperty(value = "数据结构版本号（PooledTransaction的版本号，与SDKTransaction版本号不一定完全一样）")
    String version;

    /**
     * 不用继承的主要原因是避免转换时的属性复制的性能损耗，语义上，也说得过去，加入池的交易包含了客户端的交易
     * 甚至语义上，组合比继承来得更合理
     */
    @ApiModelProperty(value = "sdk侧交易")
    SDKTransaction sdkTransaction;
    /**
     * 加入交易池的时间戳，UTC时间1970年到发送时刻的毫秒数
     */
    @ApiModelProperty(value = "加入交易池的时间戳")
    Long add2PoolTimestamp;

    /**
     * 交易Hash值，16进制字符串
     */
    @ApiModelProperty(value = "交易Hash值")
    String hashStr;


    /**
     * 预计本交易的字节数，大概数，这个跟持久化时使用的序列化方法是有关系的，而且跟智能合约影响的状态数也是有关系的 真TM恶心啊
     * TODO 最小功能集，先写个固定值吧
     * @param hash
     */
     @ApiModelProperty(value = "预计本交易字节数")
     long predictedBytes=1000L;


    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }

    public static String bytesToHexString(byte[] src){
        return Hex.encodeHexString(src,false);
    }

    public static byte[] hexStringToByte(String hexString){
        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            //log.error("Block将十六进制串转换为byte数组失败，返回null",e);
            return null;
        }
    }
    public boolean equals(PooledTransaction transaction) {
        if (transaction.getHash().equals(this.hashStr)) {
            return true;
        } else {
            return false;
        }
    }
    public PooledTransaction()
    {
        this.version = "V1.0";
    }

}
