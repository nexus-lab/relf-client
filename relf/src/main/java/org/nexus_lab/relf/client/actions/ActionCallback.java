package org.nexus_lab.relf.client.actions;

import org.nexus_lab.relf.lib.rdfvalues.RDFValue;

/**
 * @param <T> callback response type
 * @author Ruipeng Zhang
 */
public interface ActionCallback<T extends RDFValue> {
    /**
     * Called when the {@link Action} needs to send a result to the server.
     *
     * @param response response to be sent to the server
     */
    void onResponse(T response);

    /**
     * Called when the {@link Action} finishes its job. This will also trigger the status sending.
     * Cannot be called with {@link #onError(Throwable)} ()}.
     */
    default void onComplete() {
    }

    /**
     * Called when an error happens during the {@link Action} execution.
     * Cannot be called with {@link #onComplete()}.
     *
     * @param e error object
     */
    default void onError(Throwable e) {
        throw new RuntimeException(e);
    }

    /**
     * Called when an {@link Action} needs to update its progress.
     */
    default void onUpdate() {
    }
}
