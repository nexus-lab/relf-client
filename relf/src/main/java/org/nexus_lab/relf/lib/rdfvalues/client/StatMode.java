package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFInteger;

import net.badata.protobuf.converter.type.TypeConverter;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class StatMode extends RDFInteger {
    public StatMode(long value) {
        super(value);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link Long} and {@link StatMode}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<StatMode, Long> {
        @Override
        public StatMode toDomainValue(Object instance) {
            if (instance instanceof Long) {
                return new StatMode((Long) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public Long toProtobufValue(Object instance) {
            if (instance instanceof StatMode) {
                return ((StatMode) instance).getValue();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
