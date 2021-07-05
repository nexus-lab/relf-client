package org.nexus_lab.relf.client.actions;

import androidx.annotation.NonNull;

import org.nexus_lab.relf.client.exceptions.ActionNotFoundException;

import org.atteo.classindex.ClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for looking up {@link Action} classes and create {@link Action} objects.
 */
public class ActionRegistry {
    private static final Map<String, Class<? extends Action>> REGISTRY = new HashMap<>();

    static {
        for (Class<? extends Action> clazz : ClassIndex.getSubclasses(Action.class,
                Objects.requireNonNull(Action.class.getClassLoader()))) {
            REGISTRY.put(getName(clazz), clazz);
        }
    }

    /**
     * Create a new {@link Action} object from its class name.
     *
     * @param name name of the {@link Action} class to be instantiated.
     * @return new {@link Action} instance.
     * @throws ActionNotFoundException if no action is found
     */
    public static Action create(@NonNull String name) throws ActionNotFoundException {
        try {
            Class<? extends Action> clazz = REGISTRY.get(name);
            if (clazz != null) {
                return clazz.newInstance();
            }
            throw new ActionNotFoundException(
                    "Action type " + name + " is not found in the registry.");
        } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
            throw new ActionNotFoundException("Cannot create action of type " + name + ".", e);
        }
    }

    /**
     * Get the name of the {@link Action} class.
     * For now, this will be the simple name of the class.
     *
     * @param clazz {@link Action} class to get name from.
     * @return the name of the given {@link Action} class.
     */
    public static String getName(@NonNull Class<? extends Action> clazz) {
        return clazz.getSimpleName();
    }
}
