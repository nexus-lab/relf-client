package org.nexus_lab.relf.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;

/**
 * @author Ruipeng Zhang
 */
public class ByteUtilsTest {
    private static final byte[] DATA = new byte[100];
    private static final byte[] COMPRESSED;
    private static final String HEX = "00010203040001020304";
    private static final byte[] MAC_BYTE = new byte[]{73, -108, 32, -36, 38, -114};
    private static final String MAC_STRING = "49:94:20:DC:26:8E";

    static {
        for (byte i = 0; i < DATA.length; i++) {
            DATA[i] = (byte) (i % 5);
        }
        COMPRESSED = new byte[]{120, -100, 99, 96, 100, 98, 102, 97, -96, 45, 1, 0, 39, 16, 0, -55};
    }

    @Test
    public void toHexString() {
        String result = ByteUtils.toHexString(Arrays.copyOf(DATA, 10));
        assertEquals(HEX, result);
    }

    @Test
    public void fromHexString() {
        byte[] result = ByteUtils.fromHexString(HEX);
        assertArrayEquals(Arrays.copyOf(DATA, 10), result);
    }

    @Test
    public void compress() {
        byte[] result = ByteUtils.compress(DATA);
        assertArrayEquals(COMPRESSED, result);
    }

    @Test
    public void decompress() throws DataFormatException {
        byte[] result = ByteUtils.decompress(COMPRESSED);
        assertArrayEquals(DATA, result);
    }

    @Test
    public void concat() {
        byte[] result = ByteUtils.concat(new byte[]{1}, new byte[]{2}, new byte[]{3});
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    public void find() {
        byte[] original = {1, 2, 3, 4, 5};
        int index = ByteUtils.find(original, new byte[]{3, 4});
        assertEquals(2, index);
        index = ByteUtils.find(original, new byte[]{5, 4});
        assertEquals(-1, index);
    }

    @Test
    public void replace() {
        byte[] original = {1, 2, 3, 4, 5};
        byte[] result = ByteUtils.replace(original, new byte[]{3, 4}, new byte[]{6, 7, 8});
        assertArrayEquals(new byte[]{1, 2, 6, 7, 8, 5}, result);
        result = ByteUtils.replace(original, new byte[]{5, 4}, new byte[]{1});
        assertEquals(original, result);
    }

    @Test
    public void fromMacAddress() {
        byte[] result = ByteUtils.fromMacAddress(MAC_STRING);
        assertArrayEquals(MAC_BYTE, result);
    }

    @Test
    public void toMacAddress() {
        String result = ByteUtils.toMacAddress(MAC_BYTE);
        assertEquals(MAC_STRING, result.toUpperCase());
    }

    @Test
    public void reverse() {
        byte[] test = new byte[]{1, 2, 3, 4};
        ByteUtils.reverse(test, 0, 4);
        assertArrayEquals(new byte[]{4, 3, 2, 1}, test);
        test = new byte[]{1, 2, 3, 4, 5};
        ByteUtils.reverse(test, 0, 5);
        assertArrayEquals(new byte[]{5, 4, 3, 2, 1}, test);
        test = new byte[]{1, 2, 3, 4, 5};
        ByteUtils.reverse(test, 1, 3);
        assertArrayEquals(new byte[]{1, 3, 2, 4, 5}, test);
    }

    @Test
    public void decodeAddress() throws IOException {
        assertEquals("127.0.0.1", ByteUtils.decodeAddress("0100007F").getHostAddress());
        assertEquals("192.168.29.204", ByteUtils.decodeAddress("CC1DA8C0").getHostAddress());
        assertEquals("0.0.0.0", ByteUtils.decodeAddress("00000000").getHostAddress());

        assertEquals("127.0.0.1",
                ByteUtils.decodeAddress("0000000000000000FFFF00000100007F").getHostAddress());
        assertEquals("fd90:e032:1d08:8835:ffff:ffff:ffff:ffff",
                ByteUtils.decodeAddress("32e090fd3588081dffffffffffffffff").getHostAddress());
    }
}