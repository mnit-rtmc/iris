/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import us.mn.state.dot.tms.VehLengthClass;

/**
 * G4 protocol vehicle class enumeration.  Provides a mapping to the general
 * vehicle length classification enumeration values.
 *
 * @author Douglas Lau
 */
public enum G4VehClass {
	SMALL (VehLengthClass.MOTORCYCLE),	/* C0 */
	REGULAR (VehLengthClass.SHORT),		/* C1 (reporting) */
	MEDIUM (VehLengthClass.SHORT),		/* C2 */
	LARGE (VehLengthClass.MEDIUM),		/* C3 (reporting) */
	TRUCK (VehLengthClass.LONG),		/* C4 */
	EXTRA_LARGE (VehLengthClass.LONG);	/* C5 (reporting) */

	/** Vehicle length class */
	public final VehLengthClass v_class;

	/** Create a new G4 vehicle class */
	private G4VehClass(VehLengthClass vlc) {
		v_class = vlc;
	}

	/** Get the count of G4 vehicle classes */
	static public final int size = values().length;

	/** Get an RTMS G4 vehicle class from an ordinal */
	static public G4VehClass fromOrdinal(int o) {
		for(G4VehClass vc: G4VehClass.values()) {
			if(vc.ordinal() == o)
				return vc;
		}
		return SMALL;
	}
}
