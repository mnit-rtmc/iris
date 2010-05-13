/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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
 * DmsIllumControl determines how the light output is controlled.  Note: when
 * switching to one of the manual modes, dmsIllumManLevel is automatically set
 * to the current brightness level.  This means that setting dmsIllumManLevel
 * must be done in a seperate set-request.
 *
 * @author Douglas Lau
 */
public class DmsIllumControl extends ASN1Integer {

	/** Enumeration of illumination control types */
	static public enum Enum {
		undefined,
		other,		// deprecated in v2
		photocell,
		timer,
		manual,		// deprecated in v2
		manualDirect,	// added in v2
		manualIndexed;	// added in v2

		/** Get illumination control from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Create a new DmsIllumControl object */
	public DmsIllumControl() {
		super(MIB1203.illum.create(new int[] {1, 0}));
	}

	/** Set the control value */
	public void setEnum(Enum c) {
		value = c.ordinal();
	}

	/** Test if the brightness level control is "manual" */
	public boolean isManual() {
		Enum c = Enum.fromOrdinal(value);
		switch(c) {
		case manual:
		case manualDirect:
		case manualIndexed:
			return true;
		default:
			return false;
		}
	}

	/** Test if the brightness level control is "photocell" */
	public boolean isPhotocell() {
		return Enum.fromOrdinal(value) == Enum.photocell;
	}
}
