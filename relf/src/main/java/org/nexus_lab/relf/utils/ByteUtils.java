package org.nexus_lab.relf.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * An utility for operating byte arrays
 *
 * @author Ruipeng Zhang
 */
public final class ByteUtils {
    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder().equals(
            ByteOrder.LITTLE_ENDIAN);

    private ByteUtils() {
    }

    /**
     * Convert a byte array to its hexadecimal representation
     *
     * @param bytes input byte array
     * @return hexadecimal string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Create a byte array from its hexadecimal representation
     *
     * @param string hexadecimal string
     * @return decoded byte array
     */
    public static byte[] fromHexString(String string) {
        try {
            int len = string.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                        + Character.digit(string.charAt(i + 1), 16));
            }
            return data;
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compress a byte array using zip
     *
     * @param data bytes to be compressed
     * @return compressed bytes
     */
    public static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(buffer);
            output.write(buffer, 0, size);
        }
        deflater.end();
        return output.toByteArray();
    }

    /**
     * Decompress a byte array using zip
     *
     * @param data bytes to be decompressed
     * @return decompressed bytes
     * @throws DataFormatException unable to decompress data
     */
    public static byte[] decompress(byte[] data) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int size = inflater.inflate(buffer);
            output.write(buffer, 0, size);
        }
        inflater.end();
        return output.toByteArray();
    }

    /**
     * Concatenate several byte arrays
     *
     * @param data input byte arrays
     * @return concatenated byte array
     */
    public static byte[] concat(byte[]... data) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] bytes : data) {
            output.write(bytes, 0, bytes.length);
        }
        return output.toByteArray();
    }

    /**
     * Find the position of a subarray in an array
     *
     * @param value the larger array
     * @param find  the target subarray
     * @return start index of the large array where subarray starts, -1 if not found
     */
    public static int find(byte[] value, byte[] find) {
        for (int i = 0; i < value.length - find.length + 1; i++) {
            for (int j = 0; j < find.length; j++) {
                if (find[j] == value[i + j]) {
                    if (j == find.length - 1) {
                        return i;
                    }
                    continue;
                }
                break;
            }
        }
        return -1;
    }

    /**
     * Replace some consecutive bytes in a byte array with another byte array
     *
     * @param value   value to be edited
     * @param find    bytes to be replaced
     * @param replace bytes to be replaced with
     * @return edited byte array
     */
    public static byte[] replace(byte[] value, byte[] find, byte[] replace) {
        int i = find(value, find);
        if (i > -1) {
            byte[] result = new byte[value.length - find.length + replace.length];
            System.arraycopy(value, 0, result, 0, i);
            System.arraycopy(replace, 0, result, i, replace.length);
            System.arraycopy(value, i + find.length, result, i + replace.length,
                    result.length - i - replace.length);
            return result;
        }
        return value;
    }

    /**
     * Create byte array from MAC address string
     *
     * @param mac input MAC address string delimited by colon
     * @return a byte array of MAC address
     */
    public static byte[] fromMacAddress(String mac) {
        String[] fragments = mac.split(":");
        return fromHexString(StringUtils.join(fragments));
    }

    /**
     * Create a MAC address string from a byte array
     *
     * @param bytes a byte array of MAC address
     * @return input MAC address string delimited by colon
     */
    public static String toMacAddress(byte[] bytes) {
        List<String> segments = new ArrayList<>();
        for (byte b : bytes) {
            segments.add(String.format("%02x", b));
        }
        return StringUtils.join(segments, ":");
    }

    /**
     * Reverse part of the byte array.
     *
     * @param array array to be reversed.
     * @param start start point of the part to be reversed (inclusive).
     * @param end   end point of the part to be reversed (exclusive).
     */
    public static void reverse(byte[] array, int start, int end) {
        for (int i = start; i < (end + start) / 2; i++) {
            byte temp = array[i];
            array[i] = array[end - 1 - i + start];
            array[end - 1 - i + start] = temp;
        }
    }

    /**
     * Convert a hex string to IPv4/v6 address.
     *
     * @param hex  IP address in hexadecimal.
     * @return the {@link InetAddress} object of the address.
     * @throws IOException hex string is illegal.
     */
    public static InetAddress decodeAddress(String hex) throws IOException {
        byte[] ip = ByteUtils.fromHexString(hex);
        if (ip.length != 4 && ip.length != 16) {
            throw new IOException("The length of IP address " + hex + " is not 4 bytes or 16 bytes.");
        }
        if (LITTLE_ENDIAN) {
            if (ip.length == 4) {
                reverse(ip, 0, 4);
            } else {
                reverse(ip, 0, 4);
                reverse(ip, 4, 8);
                reverse(ip, 8, 12);
                reverse(ip, 12, 16);
            }
        }
        return InetAddress.getByAddress(ip);
    }
}
