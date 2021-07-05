package org.nexus_lab.relf.lib.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class DecodeException extends Exception {
    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
