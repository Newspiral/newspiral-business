package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.util.MerkleUtil;
import com.jinninghui.newspiral.security.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;

/**
 * All hashing function in this class should not change the content of input param.
 */
@Slf4j
public class SignForConsensus {
    /**
     * This function doesn't check the hash of transaction.
     * @param genericMsg
     * @param securityService
     * @return
     */
    public static boolean verifySignOfGenericMsg(GenericMsg genericMsg, SecurityService securityService) {
        if (!securityService.verifySignatureWithoutHashCheck(genericMsg, genericMsg.getHotStuffDataNode().getBlock().getBlockHeader().getChannelId())) {
            return false;
        }
        return true;
    }

    public static String hashDataNodeDirectly(HotStuffDataNode hotStuffDataNode, SecurityService securityService) {
        String string = hotStuffDataNode.getParentNodeHashStr() + new String(hotStuffDataNode.getBlock().getHash())
                        + JSONObject.toJSON(hotStuffDataNode.getJustify()).toString();
        return Hex.encodeHexString(securityService.calcHashBytes(string.getBytes()), false);
    }

    /**
     * calculate hash of the given block with calculating the merkle root of tx
     * @param block
     * @param securityService
     * @return
     */
    public static String hashBlock(Block block, SecurityService securityService) {
        String merkleRoot = merkleBlock(block);
        String root = block.getBlockHeader().getMerkleRoot();
        block.getBlockHeader().setMerkleRoot(merkleRoot);
        String hash = hashBlockHeader(block.getBlockHeader(), securityService);
        //don't change the input block
        block.getBlockHeader().setMerkleRoot(root);
        return hash;
    }

    public static String hashBlockHeader(BlockHeader header, SecurityService securityService) {
        BlockHeader blockHeader = header.clone();
        blockHeader.setHash(null);
        blockHeader.setWitness(null);
        blockHeader.setPersistenceTimestamp(new Long(0));
        blockHeader.setConsensusTimestamp(new Long(0));
        blockHeader.setPackagerAndSign(null);
        byte[] hash = securityService.calcHashBytes(JSONObject.toJSON(blockHeader).toString().getBytes());
        return Hex.encodeHexString(hash, false);
    }

    public static String hashGenericQc(GenericQC genericQC, SecurityService securityService) {
        byte[] hash = securityService.calcHashBytes(JSONObject.toJSON(genericQC).toString().getBytes());
        return Hex.encodeHexString(hash, false);
    }

    public static String merkleBlock(Block block) {
        ArrayList<byte[]> bytesOfTx = new ArrayList<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            //log.info("tx :"+JSONObject.toJSONString(tx));
            //log.info("tx bytes:"+Arrays.toString(tx.getSdkTransaction().getHash().getBytes()));
            bytesOfTx.add(tx.getSdkTransaction().getHash().getBytes());
        }
        byte[] merkleBytes = MerkleUtil.merkle(bytesOfTx);
        //log.info("after merkle method:"+ Arrays.toString(merkleBytes));
        String merkleRoot = Hex.encodeHexString(merkleBytes);
        //log.info("merkle Hex String:"+ merkleRoot);
        return merkleRoot;
    }
}
