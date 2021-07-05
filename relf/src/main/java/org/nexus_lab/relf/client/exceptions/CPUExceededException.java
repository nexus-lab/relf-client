package org.nexus_lab.relf.client.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class CPUExceededException extends RuntimeException {
    public CPUExceededException(String message) {
        super(message);
    }
}
