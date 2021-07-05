package org.nexus_lab.relf.lib.rdfvalues;

import org.nexus_lab.relf.lib.exceptions.DecodeException;

import org.atteo.classindex.IndexSubclasses;

/**
 * RDF value base interface
 *
 * @param <T> raw value type
 * @author Ruipeng Zhang
 */
@IndexSubclasses
public interface RDFValue<T> {
    /**
     * @return get the age of the {@link RDFValue}
     */
    RDFDatetime getAge();

    /**
     * Set the age of the {@link RDFValue}
     *
     * @param age age in {@link RDFDatetime}
     */
    void setAge(RDFDatetime age);

    /**
     * @return the wrapped value of the {@link RDFValue}
     */
    T getValue();

    /**
     * Set wrapped value of the {@link RDFValue}
     *
     * @param value the wrapped value
     */
    void setValue(T value);

    /**
     * Decode {@link RDFValue} from serialized bytes
     *
     * @param value bytes to decode
     * @return decoded {@link RDFValue}
     * @throws DecodeException unable to decode from serialized bytes
     */
    RDFValue<T> parse(byte[] value) throws DecodeException;

    /**
     * Decode {@link RDFValue} from serialized string. Use {@link #parse(byte[])} by default.
     *
     * @param value string to decode
     * @return decoded {@link RDFValue}
     * @throws DecodeException unable to decode from serialized string
     */
    default RDFValue<T> parse(String value) throws DecodeException {
        return parse(value.getBytes());
    }

    /**
     * Convert {@link RDFValue} to bytes.
     *
     * @return serialized bytes
     */
    byte[] serialize();

    /**
     * Concert {@link RDFValue} to its string representation. Use {@link #serialize()} by default.
     *
     * @return serialized string
     */
    default String stringify() {
        return new String(serialize());
    }

    /**
     * Get RDF name of a {@link RDFValue} class.
     *
     * @return RDF name of the class.
     */
    default String getRDFName() {
        return RDFRegistry.getName(getClass());
    }

    /**
     * Make a new copy of the {@link RDFValue} by first {@link #serialize()} then {@link
     * #parse(byte[])}.
     *
     * @return a new copy of the current {@link RDFValue}
     */
    default RDFValue<T> duplicate() {
        try {
            RDFValue<T> newInstance = getClass().newInstance();
            newInstance.setAge(getAge());
            newInstance.parse(serialize());
            return newInstance;
        } catch (InstantiationException | IllegalAccessException | DecodeException e) {
            throw new RuntimeException(String.format("Cannot clone %s", getRDFName()), e);
        }
    }
}
