package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.PackedMessageList;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link PackedMessageList}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = PackedMessageList.class, mapper = Proto2MapperImpl.class)
public class RDFPackedMessageList extends RDFProtoStruct<PackedMessageList> {
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] messageList;
    @Getter
    @Setter
    @ProtoField(converter = RDFURN.Converter.class)
    private RDFURN source;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime timestamp;
    @Getter
    @Setter
    @ProtoField(defaultValue = CompressionTypeDefaultValueImpl.class)
    private PackedMessageList.CompressionType compression;

    /**
     * Default value for {@link PackedMessageList.CompressionType}.
     */
    public static class CompressionTypeDefaultValueImpl implements DefaultValue {
        @Override
        public Object generateValue(Class<?> type) {
            return PackedMessageList.CompressionType.UNCOMPRESSED;
        }
    }
}
