package org.nexus_lab.relf.lib.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class RDFValueNotFoundException extends RuntimeException {
    public RDFValueNotFoundException(String message) {
        super(message);
    }

    public RDFValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
