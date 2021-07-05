package org.nexus_lab.relf.lib.rdfvalues.protodict;

import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;

import android.util.Log;

import org.nexus_lab.relf.lib.rdfvalues.RDFRegistry;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.DataBlob;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of built-in Java values
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = DataBlob.class, mapper = Proto2MapperImpl.class)
public class RDFDataBlob extends RDFProtoStruct<DataBlob> {
    private static final String TAG = RDFDataBlob.class.getSimpleName();

    private static final String NONE_VALUE = "None";
    private static final String[] FIELDS = {
            "integer",
            "string",
            "data",
            "booleanValue",
            "list",
            "dict",
            "rdfValue",
            "floatValue",
            "set"
    };

    @Getter
    @Setter
    @ProtoField
    private Long integer;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] data;
    @Getter
    @Setter
    @ProtoField
    private String string;
    @Getter
    @Setter
    @ProtoField
    private String protoName;
    @Getter
    @Setter
    @ProtoField
    private String none;
    @Getter
    @Setter
    @ProtoField(name = "boolean")
    private Boolean booleanValue;
    @Getter
    @Setter
    @ProtoField
    private RDFBlobArray list;
    @Getter
    @Setter
    @ProtoField
    private RDFDict dict;
    @Getter
    @Setter
    @ProtoField
    private RDFEmbeddedRDFValue rdfValue;
    @Getter
    @Setter
    @ProtoField(name = "float")
    private Float floatValue;
    @Getter
    @Setter
    @ProtoField
    private RDFBlobArray set;
    @Getter
    @Setter
    @ProtoField
    private DataBlob.CompressionType compression;

    public RDFDataBlob() {
        super();
    }

    /**
     * @param value value to be wrapped
     */
    public RDFDataBlob(Object value) {
        super();
        setActualValue(value);
    }

    /**
     * @return wrapped raw value
     */
    public Object getActualValue() {
        List<Object> values = new ArrayList<>();
        for (String field : FIELDS) {
            try {
                Object value = getFieldValue(this, field);
                if (value != null) {
                    values.add(value);
                }
            } catch (ReflectiveOperationException e) {
                Log.w(TAG, e);
            }
        }
        if (values.size() != 1 || NONE_VALUE.equals(none)) {
            return null;
        }
        if (rdfValue != null) {
            try {
                RDFValue value = RDFRegistry.create(rdfValue.getName());
                value.parse(rdfValue.getData());
                value.setAge(rdfValue.getEmbeddedAge());
                return value;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (list != null) {
            List<Object> result = new ArrayList<>();
            for (RDFDataBlob value : list.getContent()) {
                result.add(value.getActualValue());
            }
            return result;
        }
        if (set != null) {
            Set<Object> result = new HashSet<>();
            for (RDFDataBlob value : set.getContent()) {
                result.add(value.getActualValue());
            }
            return result;
        }
        return values.get(0);
    }

    /**
     * @param value value to be valued
     */
    public void setActualValue(Object value) {
        if (value == null) {
            none = NONE_VALUE;
        } else if (value instanceof RDFDict) {
            dict = (RDFDict) ((RDFDict) value).duplicate();
        } else if (value instanceof RDFValue) {
            if (rdfValue == null) {
                rdfValue = new RDFEmbeddedRDFValue();
            }
            rdfValue.setData(((RDFValue) value).serialize());
            rdfValue.setEmbeddedAge(((RDFValue) value).getAge());
            rdfValue.setName(((RDFValue) value).getRDFName());
        } else if (value instanceof byte[]) {
            data = (byte[]) value;
        } else if (value.getClass().isArray()) {
            if (list == null) {
                list = new RDFBlobArray();
            }
            list.addAll((Object[]) value);
        } else if (value instanceof List) {
            if (list == null) {
                list = new RDFBlobArray();
            }
            list.addAll((List) value);
        } else if (value instanceof Set) {
            if (set == null) {
                set = new RDFBlobArray();
            }
            set.addAll((Set) value);
        } else if (value instanceof String) {
            string = (String) value;
        } else if (value instanceof Boolean) {
            booleanValue = (Boolean) value;
        } else if (value instanceof Integer || value instanceof Long) {
            integer = ((Number) value).longValue();
        } else if (value instanceof Float || value instanceof Double) {
            floatValue = ((Number) value).floatValue();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported type for DataBlob: %s", value));
        }
    }
}
