package com.jinninghui.newspiral.common.entity.util;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * @version V1.0
 * @Title: MerkleUtil
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2021/1/10 17:28
 */
@Slf4j
public class MerkleUtil {

    public static byte[] merkleBlock(Block block) {
        ArrayList<byte[]> tree = new ArrayList<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            tree.add(tx.getSdkTransaction().getHash().getBytes());
        }
        return merkle(tree);
    }

    private static byte[] combine(byte[] a, byte[] b) {
        byte[] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }


    public static byte[] merkle(ArrayList<byte[]> tree) {
        try {
            if (tree.isEmpty()) {
                //log.info("Null tree in merkle");
                return "".getBytes();
            }
            if (tree.size() == 1) {
                return calcHashBytes(tree.get(0));
            }
            ArrayList<byte[]> node = new ArrayList<>();
            node.addAll(tree);
            ArrayList<byte[]> nodeLevelUp = new ArrayList<>();
            while (node.size() > 1) {
                for (int i = 0; i < node.size(); i += 2) {
                    if (i < (node.size() - 1)) {
                        nodeLevelUp.add(calcHashBytes(combine(node.get(i), node.get(i+1))));
                    } else {
                        nodeLevelUp.add(calcHashBytes(node.get(i)));
                    }
                }
                node.clear();
                node.addAll(nodeLevelUp);
                nodeLevelUp.clear();
            }
            return node.get(0);
        } catch (Exception ex) {
            log.error("Exception in merkle", ex);
            return "".getBytes();
        }

        /*Stack<Integer> powerStack = new Stack<>();
        int size = tree.size();
        int power = 0;
        while (size > 0) {
            if ((size & 0x00000001) > 0) {
                powerStack.push(power);
            }
            power++;
            size = size >> 1;
        }
        List<ArrayList<byte[]>> trees = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        int count = 0;
        while (!powerStack.isEmpty()) {
            Integer p = powerStack.pop();
            powerList.add(p);
            int leaf = 1 << p;
            ArrayList<byte[]> bytesOfLeaf = new ArrayList<>();
            bytesOfLeaf.addAll(tree.subList(count, count+leaf));
            trees.add(bytesOfLeaf);
            count += leaf;
        }
        Map<Integer, byte[]> hashingOfLeaf = new ConcurrentHashMap<>();
        for (int i = 0; i < powerList.size(); i++) {
            hashingOfLeaf.put(i, null);
        }
        hashingOfLeaf.entrySet().parallelStream().forEach(
                integerEntry -> hashingOfLeaf.put(integerEntry.getKey(), merkleFullBinaryTree(trees.get(integerEntry.getKey())))
        );
        byte[] result = hashingOfLeaf.get(powerList.get(tree.size()-1));
        for (int i = trees.size(); i > 0; i--) {
            int m = powerList.get(i-1);
            byte[] nodeR = hashingOfLeaf.get(m);
            if (i > 1) {
                int n = powerList.get(i - 2);
                byte[] nodeL = hashingOfLeaf.get(n);
                int needHashing = n - m;
                while (needHashing > 0) {

                }
            }
        }*/
    }

    private byte[] merkleFullBinaryTree(ArrayList<byte[]> tree) {
        int leaf = tree.size();
        if (leaf <= 0) {
            return "".getBytes();
        }
        if (leaf == 1) {
            return tree.get(0);
        }
        int cnt = 0;
        int time = 0;
        while (time <= 30) {
            int i = (leaf << time) & 0x40000000;
            if (i > 0) {
                cnt++;
            }
            if (cnt > 1) {
                log.info("Not full binary tree");
                return "".getBytes();
            }
            time++;
        }
        ArrayList<byte[]> node = new ArrayList<>();
        for (int i = 0; i < leaf; i++) {
            byte[] bytes = new byte[tree.get(i).length];
            System.arraycopy(tree.get(i), 0, bytes, 0, tree.get(i).length);
            node.add(bytes);
        }
        while (leaf > 1) {
            ArrayList<byte[]> nodeLevelUp = new ArrayList<>();
            for (int i = 0; i < leaf; i += 2) {
                nodeLevelUp.add(calcHashBytes(combine(node.get(i), node.get(i+1))));
            }
            node.clear();
            node = nodeLevelUp;
            leaf = leaf >> 1;

        }
        return node.get(0);
    }

    public static byte[] calcHashBytes(byte[] contentBytes) {
        //log.info(ModuleClassification.SM_OSSI_+"OsccaSecurityServiceImpl.calcHashBytes:");
        byte[] bytes = "".getBytes();
        //synchronized (this) {
        bytes =   OsccaCinpher.calHashBySM3(contentBytes);
        // }
        return bytes;

    }
}
