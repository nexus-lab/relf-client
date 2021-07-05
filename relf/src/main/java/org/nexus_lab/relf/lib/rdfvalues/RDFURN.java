package org.nexus_lab.relf.lib.rdfvalues;

import org.nexus_lab.relf.utils.PathUtils;

import net.badata.protobuf.converter.type.TypeConverter;

import java.net.URI;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RDFURN extends RDFString {
    public RDFURN(String value) {
        super(value);
    }

    /**
     * Append a path segment to the {@link RDFURN}.
     *
     * @param path path segment to append
     * @return a copy of edited {@link RDFURN}
     */
    public RDFURN join(String path) {
        RDFURN result = (RDFURN) duplicate();
        URI uri = URI.create((getValue() + '/' + path).replace('\\', '/')).normalize();
        result.setValue(uri.toString());
        return result;
    }

    /**
     * Split {@link RDFURN} by "/".
     *
     * @return path segments
     */
    public String[] split() {
        return getValue().split("/");
    }

    /**
     * Split {@link RDFURN} by "/" with a fixed segment count.
     *
     * @param count fixed segment count
     * @return path segments
     */
    public String[] split(int count) {
        String[] fragments = split();
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            if (count < fragments.length - 1) {
                result[i] = fragments[i + 1];
            } else {
                result[i] = "";
            }
        }
        return result;
    }

    /**
     * @return the directory path of the {@link RDFURN}.
     */
    public String getDirname() {
        return PathUtils.getPath(getValue());
    }

    /**
     * @return the base name of the {@link RDFURN}, without the extension.
     */
    public String getBasename() {
        return PathUtils.getBaseName(getValue());
    }

    /**
     * Get the relative path to a volume
     *
     * @param volume volume name
     * @return path without the starting volume name
     */
    public String getRelativeName(String volume) {
        String newVolume = new RDFURN().parse(volume).getValue();
        if (getValue().startsWith(newVolume)) {
            String result = getValue().substring(newVolume.length());
            if (result.startsWith("/")) {
                return result.substring(1);
            }
            return result;
        }
        return null;
    }

    @Override
    public RDFURN parse(String value) {
        String newValue = value.startsWith("aff4:/") ? value.substring(5) : value;
        setValue(URI.create(newValue.replaceAll("\\\\+", "/")).normalize().toString()
                .replaceAll("/+", "/"));
        if (!getValue().startsWith("/")) {
            setValue("/" + getValue());
        }
        return this;
    }

    @Override
    public String stringify() {
        return "aff4:" + getValue();
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link String} and {@link RDFURN}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RDFURN, String> {
        @Override
        public RDFURN toDomainValue(Object instance) {
            if (instance instanceof String) {
                return new RDFURN((String) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public String toProtobufValue(Object instance) {
            if (instance instanceof RDFURN) {
                return ((RDFURN) instance).stringify();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
