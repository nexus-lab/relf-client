package org.nexus_lab.relf.lib.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class SignatureException extends Exception {
    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
