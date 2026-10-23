/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020-2023  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip.mibvaisala;

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for Vaisala NTCIP RWIS controllers.
 *
 * @author Michael Darter
 */
public enum MibVaisala {

    // spectro defs
    enterprises				    (new int[] {1,3,6,1,4,1}),
    vaisala					    (enterprises, 16961),
    vaiRoadWeatherSystem		(vaisala, 5),
    vaiRwsCommon				(vaiRoadWeatherSystem, 1),
    vaiRwsRelease				(vaiRoadWeatherSystem, 2),
        spectroTableNumSensors		(vaiRwsRelease, 1),
        spectroTable				(vaiRwsRelease, 2),
        spectroEntry			    (spectroTable, 1),
            spectroIndex			    (spectroEntry, 1),
            spectroLocation		        (spectroEntry, 2),
            spectroSurfaceFrictionIndex	(spectroEntry, 3),
            spectroSurfaceIceLayer		(spectroEntry, 4),
            spectroSurfaceSnowLayer	    (spectroEntry, 5),
            spectroSurfaceWaterLayer	(spectroEntry, 6),
            spectroAirTemperature		(spectroEntry, 7),
            spectroRelativeHumidity	    (spectroEntry, 8),
            spectroDewpointTemp		    (spectroEntry, 9),
            spectroSurfaceTemperature	(spectroEntry, 10),
            spectroSurfaceStatus		(spectroEntry, 11),
            spectroHardwareStatus		(spectroEntry, 12),
    vaiRwsExperimental			(vaiRoadWeatherSystem, 3),
    vaiRwsPrivate				(vaiRoadWeatherSystem, 4);

    /** MIB node */
    public final MIBNode node;

    /** Create the root node */
    private MibVaisala(int[] n) {
        node = MIBNode.root(n, toString());
    }

    /** Create a new skyline MIB node */
    private MibVaisala(MibVaisala p, int n) {
        node = p.node.child(n, toString());
    }

    /** Make an integer */
    public ASN1Integer makeInt() {
        return new ASN1Integer(node);
    }

    /** Make an integer */
    public ASN1Integer makeInt(int r) {
        return new ASN1Integer(node, r);
    }

    /** Make an integer */
    public ASN1Integer makeInt(int r, int s) {
        return new ASN1Integer(node, r, s);
    }

    /** Make an Octet String */
    public ASN1OctetString makeOctetString() {
        return new ASN1OctetString(node);
    }

    /** Debug string */
    public String toDebugString() {
        MIBNode n = node;
        if (n == null)
            return "node=null";
        StringBuilder sb = new StringBuilder();
        sb.append("name=" + n.getName());
        int[] oa = n.oid();
        int len = oa.length;
        sb.append(" len=" + len + " array=");
        for (int i : oa)
            sb.append(i).append(".");
        return sb.toString();
    }
}
