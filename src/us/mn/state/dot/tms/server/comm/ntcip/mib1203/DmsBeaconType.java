/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsBeaconType object
 *
 * @author Douglas Lau
 */
public class DmsBeaconType extends ASN1Integer {

	/** Enumeration of beacon types */
	static public enum Enum {
		undefined, other, none, oneBeacon, twoBeaconSyncFlash,
		twoBeaconsOppFlash, fourBeaconSyncFlash, fourBeaconAltRowFlash,
		fourBeaconAltColumnFlash, fourBeaconAltDiagonalFlash,
		fourBeaconNoSyncFlash, oneBeaconStrobe, twoBeaconStrobe,
		fourBeaconStrobe;

		/** Get beacon type from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Set the integer value */
	public void setInteger(int v) {
		value = Enum.fromOrdinal(v).ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.dmsSignCfg.createOID(new int[] {8, 0});
	}
}
