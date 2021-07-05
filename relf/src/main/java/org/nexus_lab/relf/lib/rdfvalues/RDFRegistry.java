package org.nexus_lab.relf.lib.rdfvalues;

import androidx.annotation.NonNull;

import org.nexus_lab.relf.lib.exceptions.RDFValueNotFoundException;

import net.badata.protobuf.converter.annotation.ProtoClass;

import org.atteo.classindex.ClassIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for looking up {@link RDFValue} classes and create {@link RDFValue} objects.
 */
public class RDFRegistry {
    private static final Map<String, Class<? extends RDFValue>> REGISTRY = new HashMap<>();

    static {
        for (Class<? extends RDFValue> clazz : ClassIndex.getSubclasses(RDFValue.class,
                Objects.requireNonNull(RDFValue.class.getClassLoader()))) {
            REGISTRY.put(getName(clazz), clazz);
        }
    }

    /**
     * Create a new {@link RDFValue} object from its class name.
     *
     * @param name name of the {@link RDFValue} class to be instantiated.
     * @return new {@link RDFValue} instance.
     * @throws RDFValueNotFoundException if no RDF value found
     */
    public static RDFValue create(@NonNull String name) throws RDFValueNotFoundException {
        try {
            Class<? extends RDFValue> clazz = REGISTRY.get(name);
            if (clazz != null) {
                return clazz.newInstance();
            }
            throw new RDFValueNotFoundException("RDF type " + name + " is not in the registry.");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RDFValueNotFoundException("Cannot create RDF value of type " + name + ".", e);
        }
    }

    /**
     * Get the RDF name of the {@link RDFValue} class.
     * It will first look up the Protobuf name as the RDF name. If it is not found, it will use the
     * simple class name instead.
     *
     * @param clazz {@link RDFValue} class to get name from.
     * @return the RDF name of the given {@link RDFValue} class.
     */
    public static String getName(@NonNull Class<? extends RDFValue> clazz) {
        ProtoClass annotation = clazz.getAnnotation(ProtoClass.class);
        if (annotation != null) {
            return annotation.value().getSimpleName();
        }
        return clazz.getSimpleName();
    }
}
