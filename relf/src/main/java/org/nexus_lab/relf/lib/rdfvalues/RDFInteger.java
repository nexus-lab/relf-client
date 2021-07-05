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
public class RDFInteger implements RDFValue<Long> {
    @Getter
    @Setter
    private Long value = 0L;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime();

    public RDFInteger(long value) {
        setValue(value);
    }

    @Override
    public RDFInteger parse(byte[] value) {
        return parse(new String(value));
    }

    @Override
    public RDFInteger parse(String value) {
        setValue(Long.valueOf(value));
        return this;
    }

    @Override
    public byte[] serialize() {
        return stringify().getBytes();
    }

    @Override
    public String stringify() {
        return String.valueOf(getValue());
    }
}
