package org.nexus_lab.relf.lib.rdfvalues;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@EqualsAndHashCode
@NoArgsConstructor
public class RDFString implements RDFValue<String> {
    @Getter
    @Setter
    private String value;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime();

    public RDFString(String value) {
        parse(value);
    }

    @Override
    public RDFString parse(byte[] value) {
        return parse(new String(value));
    }

    @Override
    public RDFString parse(String string) {
        setValue(string);
        return this;
    }

    @Override
    public byte[] serialize() {
        return stringify().getBytes();
    }

    @Override
    public String stringify() {
        return getValue();
    }
}
