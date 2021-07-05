package org.nexus_lab.relf.lib.rdfvalues.protodict;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.BlobArray;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link BlobArray}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = BlobArray.class, mapper = Proto2MapperImpl.class)
public class RDFBlobArray extends RDFProtoStruct<BlobArray> {
    @Getter
    @Setter
    @ProtoField
    private List<RDFDataBlob> content;

    /**
     * @param values collection of Java objects
     */
    public void addAll(Collection values) {
        if (content == null) {
            content = new ArrayList<>();
        }
        for (Object value : values) {
            RDFDataBlob blob = new RDFDataBlob();
            blob.setActualValue(value);
            content.add(blob);
        }
    }

    /**
     * @param values array of Java objects
     */
    public void addAll(Object[] values) {
        addAll(Arrays.asList(values));
    }
}
