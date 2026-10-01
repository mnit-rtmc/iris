package us.mn.state.dot.tms.server.comm.snmp;

import junit.framework.TestCase;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SNMPTest extends TestCase {

    public void testEncodeObjectIdentifier_ThreeByte() throws IOException {
        int[] oid = new int[] {1, 2, 16383 };
        SNMP snmp = new SNMP();
        snmp.encodeObjectIdentifier(oid);
        assertEquals(
            "06 03 2a ff 7f",
            BerToHexString(snmp.getEncodedData()));
    }

    public void testEncodeObjectIdentifier_FourByte() throws IOException {
        int[] oid = new int[] {1, 2, 16384 };
        SNMP snmp = new SNMP();
        snmp.encodeObjectIdentifier(oid);
        assertEquals(
            "06 04 2a 81 80 00",
            BerToHexString(snmp.getEncodedData()));
    }

    public void testEncodeObjectIdentifier_EssNtcip() throws IOException {
        int[] oid = MIB1204.essNtcip.node.oid();
        SNMP snmp = new SNMP();
        snmp.encodeObjectIdentifier(oid);
        assertEquals(
            "06 0c 2b 06 01 04 01 89 36 04 02 05 02 00",
            BerToHexString(snmp.getEncodedData()));
    }

    /** This example is taken from the asn1 layman guide:
     *    https://luca.ntop.org/Teaching/Appunti/asn1.html
     */
    public void testEncodeObjectIdentifier_RsaDataSecurity() throws IOException {
        int[] oid = new int[] {1, 2, 840, 113549};
        SNMP snmp = new SNMP();
        snmp.encodeObjectIdentifier(oid);
        assertEquals(
                "06 06 2a 86 48 86 f7 0d",
                BerToHexString(snmp.getEncodedData()));
    }

    private String BerToHexString(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .map(i -> bytes[i])
                .mapToObj(b -> String.format("%02x", b & 0xFF))
                .collect(Collectors.joining(" "));
    }
}