package org.nexus_lab.relf.lib.rdfvalues.paths;

import org.nexus_lab.relf.lib.rdfvalues.RDFString;

import net.badata.protobuf.converter.type.TypeConverter;

/**
 * A glob expression for a client path.
 * (Only used in server)
 *
 * @author Ruipeng Zhang
 */
public class GlobExpression extends RDFString {
    /**
     * {@link TypeConverter} for inter-conversion between {@link String} and {@link GlobExpression}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<GlobExpression, String> {
        @Override
        public GlobExpression toDomainValue(Object instance) {
            if (instance instanceof String) {
                return (GlobExpression) new GlobExpression().parse((String) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public String toProtobufValue(Object instance) {
            if (instance instanceof GlobExpression) {
                return ((GlobExpression) instance).stringify();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
