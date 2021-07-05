package org.nexus_lab.relf.lib.rdfvalues.standard;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A semantic regular expression.
 *
 * @author Ruipeng Zhang
 */
@EqualsAndHashCode
@NoArgsConstructor
public class RegularExpression implements RDFValue<Pattern> {
    @Getter
    @Setter
    private Pattern value;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime(0);

    public RegularExpression(String value) {
        parse(value);
    }

    /**
     * Search the text for the given regular expression
     *
     * @param text content for searching
     * @return a {@link Matcher} if pattern is found, otherwise null
     */
    public Matcher match(String text) {
        if (getValue() == null) {
            throw new RuntimeException("The regular expression value is not set.");
        }
        return getValue().matcher(text);
    }

    @Override
    public RegularExpression parse(byte[] value) {
        parse(new String(value));
        return this;
    }

    @Override
    public RegularExpression parse(String value) {
        setValue(Pattern.compile(value, CASE_INSENSITIVE | DOTALL | MULTILINE));
        return this;
    }

    @Override
    public byte[] serialize() {
        return stringify().getBytes();
    }

    @Override
    public String stringify() {
        return getValue().toString();
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link String} and {@link
     * RegularExpression}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RegularExpression, String> {
        @Override
        public RegularExpression toDomainValue(Object instance) {
            if (instance instanceof String) {
                return new RegularExpression((String) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public String toProtobufValue(Object instance) {
            if (instance instanceof RegularExpression) {
                return ((RegularExpression) instance).stringify();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
