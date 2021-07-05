package org.nexus_lab.relf.lib.rdfvalues;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Only a wrapper of the {@link RDFValue} that needs to be sent.
 * Not a part of the original RDF values
 *
 * @author Ruipeng Zhang
 */
@EqualsAndHashCode
@NoArgsConstructor
public class TransferStore implements RDFValue<RDFValue> {
    @Setter
    @Getter
    private long length;
    @Getter
    @Setter
    private RDFValue value;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime(0);

    public TransferStore(RDFValue value, long length) {
        setAge(age);
        setValue(value);
        setLength(length);
    }

    @Override
    public TransferStore parse(byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransferStore parse(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String stringify() {
        throw new UnsupportedOperationException();
    }
}
