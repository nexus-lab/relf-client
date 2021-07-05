package org.nexus_lab.relf.lib.rdfvalues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RDFZippedBytes extends RDFBytes {
    /**
     * @param value compressed bytes
     */
    public RDFZippedBytes(byte[] value) {
        super(value);
    }

    /**
     * Unzip compressed bytes
     *
     * @return decompress bytes
     * @throws IOException         unable to close output stream
     * @throws DataFormatException unable to decompress zipped bytes
     */
    public byte[] decompress() throws IOException, DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(getValue());
        ByteArrayOutputStream out = new ByteArrayOutputStream(getValue().length);
        byte[] buffer = new byte[1024];
        while (!decompressor.finished()) {
            int count = decompressor.inflate(buffer);
            out.write(buffer, 0, count);
        }
        out.close();
        return out.toByteArray();
    }
}
