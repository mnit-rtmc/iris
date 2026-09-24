/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Iteris Inc.
 * Copyright (C) 2026  Alaska DOT&PF
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


/**
 * Spectro hardware status as defined by Vaisala Spectro.
 * @author Michael Darter
 * @author Darren Jaeckel, Wostmann & Associates
 */
public enum SpectroHwStatus {

    // Spectro hardware status
    undefined(-1),
    hwOKwindowClear(0),
    cpuHwWarningwindowClear(1),
    transmitterhwWarningwindowClear(2),
    dataMissingExcessiveAmbientLightwindowClear(3),
    dataMissingLowVisibilitywindowClear(4),
    hwOKwindowContaminated(10),
    cpuHwWarningwindowContaminated(11),
    transmitterhwWarningwindowContaminated(12),
    dataMissingExcessiveAmbientLightwindowContaminated(13),
    datamissingLowVisibilitywindowContaminated(14),
    hwOKwindowHeavilyContaminated(20),
    cpuHwWarningwindowHeavilyContaminated(21),
    transmitterhwWarningwindowHeavilyContaminated(22),
    dataMissingExcessiveAmbientLightwindowHeavilyContaminated(23),
    dataMissingLowVisibilitywindowHeavilyContaminated(24);

    /** Numeric value */
    final public int val;

    /** Constructor */
    private SpectroHwStatus(int nv) {
        val = nv;
    }

    /** Get an enum from a numeric value */
    static public SpectroHwStatus fromVal(Integer v) {
        if (v != null) {
            for (SpectroHwStatus hws : SpectroHwStatus.values()) {
                if (hws.val == v) {
                    return hws;
                }
            }
        }
        return undefined;
    }

    /** Get an enum from an ordinal value */
    static public SpectroHwStatus fromOrdinal(int o) {
        return (o >= 0 && o < values().length ? values()[o] : undefined);
    }

    /** Get an enum from an ordinal value */
    static public SpectroHwStatus fromOrdinal(Integer o) {
        return (o != null ? fromOrdinal(o.intValue()) : undefined);
    }
}
