package org.nexus_lab.relf.lib.rdfvalues.protodict;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Dict;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * RDF wrapper of {@link Dict}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Dict.class, mapper = Proto2MapperImpl.class)
public class RDFDict extends RDFProtoStruct<Dict> implements Map<Object, Object> {
    private Map<Object, Object> data = new HashMap<>();
    private Map<Object, RDFKeyValue> mappings = new HashMap<>();
    @Getter
    @ProtoField(name = "dat")
    private List<RDFKeyValue> keyValues;

    /**
     * @param keyValues a list of {@link RDFKeyValue} to be put to the dict
     */
    public void setKeyValues(List<RDFKeyValue> keyValues) {
        for (RDFKeyValue keyValue : keyValues) {
            put(keyValue.getKey().getActualValue(), keyValue.getVal().getActualValue());
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        Object oldValue = data.get(key);
        remove(key);
        RDFKeyValue keyValue = new RDFKeyValue(new RDFDataBlob(key), new RDFDataBlob(value));
        data.put(key, value);
        mappings.put(key, keyValue);
        if (keyValues == null) {
            keyValues = new ArrayList<>();
        }
        keyValues.add(keyValue);
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        RDFKeyValue keyValue = mappings.get(key);
        mappings.remove(key);
        if (keyValues != null && keyValue != null) {
            keyValues.remove(keyValue);
        }
        return data.remove(key);
    }

    @Override
    public void putAll(Map<?, ?> m) {
        for (Entry<?, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        data.clear();
        mappings.clear();
        if (keyValues != null) {
            keyValues.clear();
        }
    }

    @Override
    public Set keySet() {
        return data.keySet();
    }

    @Override
    public Collection values() {
        return data.values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return data.entrySet();
    }
}
