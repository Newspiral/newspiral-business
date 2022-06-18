package com.jinninghui.newspiral.ledger.fabric;

import java.util.HashMap;
import java.util.Map;

/**
 * @version V1.0
 * @Title: Chaincode
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.fabric
 * @Description:
 * @author: xuxm
 * @date: 2020/1/2 14:44
 */
public interface Chaincode {
    /**
     * Called during an instantiate transaction after the container has been
     * established, allowing the chaincode to initialize its internal data.
     *
     * @param stub the chaincode stub
     * @return the chaincode response
     */
    Response init(ChaincodeStub stub);

    /**
     * Called for every Invoke transaction. The chaincode may change its state
     * variables.
     *
     * @param stub the chaincode stub
     * @return the chaincode response
     */
     Response invoke(ChaincodeStub stub);

    /**
     * Wrapper around protobuf Response, contains status, message and payload.
     * Object returned by call to {@link #init(ChaincodeStub)}
     * and{@link #invoke(ChaincodeStub)}
     */
    class Response {

        private  int statusCode;
        private  String message;
        private  byte[] payload;

        public Response( Status status,  String message,  byte[] payload) {
            this.statusCode = status.getCode();
            this.message = message;
            this.payload = payload;
        }

        public Response( int statusCode,  String message,  byte[] payload) {
            this.statusCode = statusCode;
            this.message = message;
            this.payload = payload;
        }

        public Status getStatus() {
            if (Status.hasStatusForCode(statusCode)) {
                return Status.forCode(statusCode);
            } else {
                return null;
            }
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }

        /**
         * {@link Response} status enum.
         */
        public enum Status {
            SUCCESS(200), ERROR_THRESHOLD(400), INTERNAL_SERVER_ERROR(500);

            private static  Map<Integer, Status> CODETOSTATUS = new HashMap<>();
            private  int code;

            Status( int code) {
                this.code = code;
            }

            public int getCode() {
                return code;
            }

            public static Status forCode( int code) {
                final Status result = CODETOSTATUS.get(code);
                if (result == null) {
                    throw new IllegalArgumentException("no status for code " + code);
                }
                return result;
            }

            public static boolean hasStatusForCode( int code) {
                return CODETOSTATUS.containsKey(code);
            }

            static {
                for ( Status status : Status.values()) {
                    CODETOSTATUS.put(status.code, status);
                }
            }

        }

    }
}
