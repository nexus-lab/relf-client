package org.nexus_lab.relf.lib.rdfvalues.protodict;

import org.nexus_lab.relf.proto.AttributedDict;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

/**
 * RDF wrapper of {@link AttributedDict}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AttributedDict.class, mapper = Proto2MapperImpl.class)
public class RDFAttributedDict extends RDFDict {
}
