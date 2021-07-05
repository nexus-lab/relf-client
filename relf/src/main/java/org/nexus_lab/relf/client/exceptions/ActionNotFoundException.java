package org.nexus_lab.relf.client.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class ActionNotFoundException extends RuntimeException {
    public ActionNotFoundException(String message) {
        super(message);
    }

    public ActionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
