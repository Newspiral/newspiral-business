package com.jinninghui.newspiral.consensus.hotstuff;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;

/**
 * @author ganzirong
 * @date 2020/12/21
 *
 */
public interface DataVerifier {
    boolean verifyBlock(Block block, String SecurityServiceKey);
    boolean verifyTransaction(SDKTransaction sdkTransaction, String SecurityServiceKey);
    boolean verifyGenericQC(GenericQC genericQC, Channel channel);
}
