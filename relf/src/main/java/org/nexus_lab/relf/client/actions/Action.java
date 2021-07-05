package org.nexus_lab.relf.client.actions;

import android.content.Context;

import androidx.annotation.Nullable;

import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.utils.ReflectionUtils;

import org.atteo.classindex.IndexSubclasses;

import java.lang.reflect.Type;

/**
 * Base interface for all client actions.
 *
 * @param <T> action parameter type.
 * @param <E> action result type.
 * @author Ruipeng Zhang
 */
@IndexSubclasses
public interface Action<T extends RDFValue, E extends RDFValue> {
    /**
     * Main working logic of all the actions.
     *
     * @param context  application context
     * @param request  execution parameter
     * @param callback action result, error and progress callback
     */
    void execute(Context context, @Nullable T request, ActionCallback<E> callback);

    /**
     * Get the class of the action request parameter.
     *
     * @return class of the request.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default Class<T> getRequestType() {
        Type[] types = ReflectionUtils.getGenericTypes(getClass(), Action.class);
        if (types != null && types.length > 0 && !RDFNull.class.equals(types[0])) {
            return (Class<T>) types[0];
        }
        return null;
    }

    /**
     * Get the class of the action response.
     *
     * @return class of the response.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default Class<T> getResponseType() {
        Type[] types = ReflectionUtils.getGenericTypes(getClass(), Action.class);
        if (types != null && types.length > 1 && !RDFNull.class.equals(types[1])) {
            return (Class<T>) types[1];
        }
        return null;
    }
}
