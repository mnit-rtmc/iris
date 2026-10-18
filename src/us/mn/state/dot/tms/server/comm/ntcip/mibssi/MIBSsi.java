/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026   Alaska DOT&PF
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.ntcip.mibssi;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * Custom MIB for SSI LX-RPU 1.24+
 * <p>
 * This MIB contains the object definition for non-NTCIP
 * compliant components, Specifically, this allows us to
 * read data from the MRC Temperature Data Probe (TDP) and
 * Vaisala's 10-min precipitation accumulation data (if it exists).
 * <p>
 * Each "Report" is an OCTET String containing signed and unsigned
 * integers. Integers are given in LSB and signed integers are in
 * two's compliment.
 *
 * @author Darren Jaeckel, Wostmann & Associates
 */
public enum MIBSsi {
    ssi                 (MIB1201._private, 12),
    ssiESS              (ssi, 5),
    ssiESSReports       (ssiESS, 2),
        atmospheric         (ssiESSReports, 1), // contains NTCIP atmo fields
        surface             (ssiESSReports, 2), // contains NTCIP pavement table
        subSurface          (ssiESSReports, 3),
        atmoExt             (ssiESSReports, 9),
    ;

    /** MIB node */
    public final MIBNode node;

    /** Create a node with MIB1201 parent */
    private MIBSsi(MIB1201 p, int n) {
        node = p.node.child(n, toString());
    }

    /** Create a new MRC MIB node */
    private MIBSsi(MIBSsi p, int n) {
        node = p.node.child(n, toString());
    }

    /** Make an Octet String */
    public ASN1OctetString makeOctetString() {
        return new ASN1OctetString(node);
    }

    /**
     * Make an integer
     * <p>
     * HACK - node actually contains a hex string,
     * so we set the value manually
     */
    public ASN1Integer makeInt(int value) {
        ASN1Integer asn1Int = new ASN1Integer(node);
        asn1Int.setInteger(value);
        return asn1Int;
    }

    /**
     * Converts the byte to its <i>unsigned</i> integer value
     * Note: The "& 0xFF" operation removes the sign
     */
    public static Integer unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Converts two bytes into a signed integer
     * <p>
     * Expects bytes in little-endian
     *
     * @param lsb Least significant byte
     * @param msb Most significant byte
     */
    public static Integer signedBytesToInt(byte lsb, byte msb) {
        return (msb << 8) | lsb & 0xFF;
    }

    /**
     * Converts a little-endian four byte array
     * into an integer.
     *
     * @param bytes Four byte array
     * @return Integer
     */
    public static int unsignedBytesToInt(byte[] bytes) {
        if(bytes.length != 4) {
            return 0;
        }
        return (bytes[3] & 0xFF) << 24
                | (bytes[2] & 0xFF) << 16
                | (bytes[1] & 0xFF) << 8
                | (bytes[0] & 0xFF);
    }
}
