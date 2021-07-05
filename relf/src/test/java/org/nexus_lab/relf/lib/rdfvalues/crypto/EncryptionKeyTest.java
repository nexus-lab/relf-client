package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncryptionKeyTest {
    @Test
    public void fromHexString() throws Exception {
        EncryptionKey key = EncryptionKey.fromHexString(
                "7D1DEC478B7497C08DBDE301E28F666327EFE851571607F17B9B3261D88D729DB3067B8AB216DF274CF0D1499177F7181F721BB81EC76B7A60D6BB34F35963BD1A4A3AA1CADF5E3AEFE0A0B2081956CB22A08F9015D69AD43C8AE6312514E970CDB9445E55977E4C0AD161AC4E688A2A652D2597D3ABEBBB4FB9196DD8745F74");
        assertEquals(1024, key.length());
    }

    @Test
    public void generateKey() throws Exception {
        EncryptionKey key = EncryptionKey.generateKey(128);
        assertEquals(128, key.length());
    }
}