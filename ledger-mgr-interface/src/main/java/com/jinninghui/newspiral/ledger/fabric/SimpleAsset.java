package com.jinninghui.newspiral.ledger.fabric;


import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

import java.util.List;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
//@Component
//@Qualifier("SimpleAsset")
public class SimpleAsset extends ChaincodeBaseBusiness {


    public SimpleAsset(BussinessContractConnector connector) {
        super(connector);
    }

    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        stub.smartContractMgr= this.smartContractMgr;
        try {
            // Get the args from the transaction proposal
            List<String> args = stub.getParameters();
            if (args.size() != 2) {
                ResponseUtils.newErrorResponse("Incorrect arguments. Expecting a key and a value");
            }

            // Set up any variables or assets here by calling stub.putState()
            // We store the key and the value on the ledger
            stub.putStringState(args.get(0), args.get(1));
            return ResponseUtils.newSuccessResponse();
        } catch (Throwable e) {
            return ResponseUtils.newErrorResponse("Failed to create asset");
        }
    }

    /**
     * Invoke is called per transaction on the chaincode. Each transaction is
     * either a 'get' or a 'set' on the asset created by Init function. The Set
     * method may create a new asset by specifying a new key-value pair.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        stub.smartContractMgr= this.smartContractMgr;
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("set")) {
                // Return result as success payload
                set(stub, params);
                return ResponseUtils.newSuccessResponse();
            } else if (func.equals("get")) {
                // Return result as success payload
                return ResponseUtils.newSuccessResponse(null, get(stub, params));
            }
            return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"set\", \"get\"");
        } catch (Throwable e) {
            return ResponseUtils.newErrorResponse(e.getMessage());
        }
    }

    /**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    public byte[] get(ChaincodeStub stub, List<String> args) {
        stub.smartContractMgr= this.smartContractMgr;
        if (args.size() != 1) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        byte[] value = stub.getState(args.get(0));
        if (value == null || value.length == 0) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    public void set(ChaincodeStub stub, List<String> args) {
        stub.smartContractMgr=   this.smartContractMgr;
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        stub.putStringState(args.get(0), args.get(1));
    }

/*    public static void main(String[] args) {
        new SimpleAsset().start(args);
    }*/

}
