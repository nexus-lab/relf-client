package org.nexus_lab.relf.lib.rdfvalues.structs;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.callStaticMethod;

import com.google.protobuf.MessageLite;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;

import net.badata.protobuf.converter.Converter;
import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;

import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all RDF value wrapped protobuf
 *
 * @param <T> type of the wrapped protobuf
 * @author Ruipeng Zhang
 */
public class RDFProtoStruct<T extends MessageLite> implements RDFValue<T> {
    private Converter converter;
    @Getter
    private T value;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime(0);

    public RDFProtoStruct() {
        converter = Converter.create();
    }

    private Class<? extends MessageLite> getProtoClass() {
        ProtoClass annotation = getClass().getAnnotation(ProtoClass.class);
        if (annotation == null) {
            throw new RuntimeException(String.format(
                    "Class %s is not annotated with @ProtoClass", getClass().getSimpleName()));
        }
        return annotation.value();
    }

    @Override
    public void setValue(T value) {
        try {
            this.value = value;
            callInstanceMethod(converter, "fillDomain", from(Object.class, this),
                    from(MessageLite.class, value),
                    from(ProtoClass.class, getClass().getAnnotation(ProtoClass.class)));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to parse protobuf from given value.", e);
        }
    }

    @Override
    public RDFProtoStruct<T> parse(byte[] value) {
        try {
            setValue(callStaticMethod(getProtoClass(), "parseFrom", from(byte[].class, value)));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to parse protobuf from given value.", e);
        }
        return this;
    }

    @Override
    public byte[] serialize() {
        return converter.toProtobuf(getProtoClass(), this).toByteArray();
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", getClass().getSimpleName(),
                converter.toProtobuf(getProtoClass(), this).toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return false;
        }
        RDFProtoStruct other = (RDFProtoStruct) obj;
        return Arrays.equals(other.serialize(), serialize()) && Objects.equals(other.getAge(),
                getAge());
    }

    @Override
    public int hashCode() {
        int prime = 59;
        int result = prime + Arrays.hashCode(serialize());
        result = result * prime + getAge().hashCode();
        return result;
    }

    /**
     * Clone field values from another {@link RDFProtoStruct}, which also clear local fields
     *
     * @param other the object we want to copy
     */
    public void copyFrom(RDFProtoStruct other) {
        List<Field> fields = new ArrayList<>(Arrays.asList(getClass().getDeclaredFields()));
        CollectionUtils.filter(fields,
                field -> field != null && field.getAnnotation(ProtoField.class) != null);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(this, null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("Cannot clear field value for %s", field.getName()), e);
            }
        }
        parse(other.serialize());
    }
}
