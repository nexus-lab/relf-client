package org.nexus_lab.relf.lib.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class VerificationException extends Exception {
    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
