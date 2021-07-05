package org.nexus_lab.relf.lib.rdfvalues.protodict;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.KeyValue;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link KeyValue}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = KeyValue.class, mapper = Proto2MapperImpl.class)
public class RDFKeyValue extends RDFProtoStruct<KeyValue> {
    @Getter
    @Setter
    @ProtoField(name = "k")
    private RDFDataBlob key;
    @Getter
    @Setter
    @ProtoField(name = "v")
    private RDFDataBlob val;

    public RDFKeyValue() {
        super();
    }

    public RDFKeyValue(RDFDataBlob key, RDFDataBlob value) {
        super();
        setKey(key);
        setVal(value);
    }
}
