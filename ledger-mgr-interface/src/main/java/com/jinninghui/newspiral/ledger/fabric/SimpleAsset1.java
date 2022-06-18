package com.jinninghui.newspiral.ledger.fabric;



import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

import java.util.List;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
public class SimpleAsset1 extends ChaincodeBaseBusiness {


    public SimpleAsset1(BussinessContractConnector connector) {
        super(connector);
    }

    /**
     * get returns the value of the specified asset key
     *
     * @param args {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    public byte[] get(List<String> args) {
        if (args.size() != 1) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        byte[] value = smartContractMgr.getState(args.get(0));
        if (value == null || value.length == 0) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param args {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    public void set( List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        smartContractMgr.putState(args.get(0), args.get(1).getBytes());
    }


    @Override
    public Response init(ChaincodeStub stub) {
        return null;
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        return null;
    }

/*    public static void main(String[] args) {
        new SimpleAsset().start(args);
    }*/

}
