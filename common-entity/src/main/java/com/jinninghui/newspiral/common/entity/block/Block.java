package com.jinninghui.newspiral.common.entity.block;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.*;

/**
 * @author lida
 * @date 2019/7/5 19:11
 */
@ApiModel(description = "区块")
@Data
@Slf4j
public class Block implements VerifiableData {

   @ApiModelProperty(value = "区块头")
   private BlockHeader blockHeader=new BlockHeader();

    /**
     * 不允许替换
     */
    @ApiModelProperty(value = "区块中的交易列表")
   final private List<ExecutedTransaction> transactionList = new ArrayList<>();

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    public List<ExecutedTransaction> getTransactionList() {
        return transactionList;
    }

    @ApiModelProperty(value = "业务端SDk交易")
    public List<SDKTransaction> getSDKTransactions() {
        List<SDKTransaction> sdkTx = new ArrayList<>();
        for (ExecutedTransaction tx : transactionList) {
            sdkTx.add(tx.getSdkTransaction());
        }
        return sdkTx;
    }
    public Set<String> getTxHashSet() {
        Set<String> hashSet = new HashSet<>();
        for (ExecutedTransaction tx : transactionList) {
            hashSet.add(tx.getSDKTransactionHash());
        }
        return hashSet;
    }

    /**
     * 不提供set方法，因此补一个add方法
     * @param executedTransactionList
     */
    public void addTransactionList(List<ExecutedTransaction> executedTransactionList) {
        this.transactionList.addAll(executedTransactionList);
    }

    @Override
    public void setHash(String hash) {
        blockHeader.setHash(hash);
    }

    @ApiModelProperty(value = "本区块hash值",hidden = true)
    @Override
    public String getHash() {
        return blockHeader.getHash();
    }

    @ApiModelProperty(value = "前序区块Hash值")
    public String getPrevBlockHash() {
        return blockHeader.getPrevBlockHash();
    }

    @ApiModelProperty(value = "调用者身份标识")
    @Override
    public SignerIdentityKey getSignerIdentityKey() {
        return blockHeader.getPackagerAndSign();
    }

    @Override
    public void setSignerIdentityKey(SignerIdentityKey identity) {
        if (identity != null) {
            blockHeader.setPackagerAndSign(identity.clone());
        } else {
            blockHeader.setPackagerAndSign(null);
        }
    }

    //todo:2.0.8 把下面两个函数放在基础函数库中
    public static String bytesToHexString(byte[] src){
        return Hex.encodeHexString(src,false);
    }

    public static byte[] hexStringToByte(String hexString){
        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            log.error("Block将十六进制串转换为byte数组失败，返回null",e);
            return null;
        }
    }
}
