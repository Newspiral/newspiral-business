package com.jinninghui.newspiral.security.contract;

public class SandboxException extends RuntimeException{

    public SandboxException(String message, Throwable cause) {
        super(message, cause);
    }

    public SandboxException(String message) {
        super(message);
    }
}
