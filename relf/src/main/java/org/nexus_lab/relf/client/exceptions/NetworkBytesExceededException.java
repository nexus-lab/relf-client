package org.nexus_lab.relf.client.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class NetworkBytesExceededException extends RuntimeException {
    public NetworkBytesExceededException(String message) {
        super(message);
    }
}
