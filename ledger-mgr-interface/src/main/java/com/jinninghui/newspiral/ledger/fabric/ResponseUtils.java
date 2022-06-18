/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.jinninghui.newspiral.ledger.fabric;


import static com.jinninghui.newspiral.ledger.fabric.Chaincode.Response.Status.INTERNAL_SERVER_ERROR;
import static com.jinninghui.newspiral.ledger.fabric.Chaincode.Response.Status.SUCCESS;

public  class ResponseUtils {


    public ResponseUtils() {
    }

    /**
     * @param message
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse( String message,  byte[] payload) {
        return new Chaincode.Response(SUCCESS, message, payload);
    }

    /**
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse() {
        return newSuccessResponse(null, null);
    }

    /**
     * @param message
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse( String message) {
        return newSuccessResponse(message, null);
    }

    /**
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse( byte[] payload) {
        return newSuccessResponse(null, payload);
    }

    /**
     * @param message
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse( String message,  byte[] payload) {
        return new Chaincode.Response(INTERNAL_SERVER_ERROR, message, payload);
    }

    /**
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse() {
        return newErrorResponse(null, null);
    }

    /**
     * @param message
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse( String message) {
        return newErrorResponse(message, null);
    }

    /**
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse( byte[] payload) {
        return newErrorResponse(null, payload);
    }

    /**
     * @param throwable
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse( Throwable throwable) {
        // Responses should not include internals like stack trace but make sure it gets
        // logged

        String message = null;
        byte[] payload = null;
            message = "Unexpected error";

        return ResponseUtils.newErrorResponse(message, null);
    }
}
