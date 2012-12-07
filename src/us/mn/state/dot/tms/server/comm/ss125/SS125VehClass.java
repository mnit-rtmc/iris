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
package us.mn.state.dot.tms.server.comm.ss125;

import us.mn.state.dot.tms.VehLengthClass;

/**
 * SS125 protocol vehicle class enumeration.  Provides a mapping to the general
 * vehicle length classification enumeration values.
 *
 * @author Douglas Lau
 */
public enum SS125VehClass {

	MOTORCYCLE (VehLengthClass.MOTORCYCLE),
	SHORT (VehLengthClass.SHORT),
	MEDIUM (VehLengthClass.MEDIUM),
	LONG (VehLengthClass.LONG);

	/** Vehicle length class */
	public final VehLengthClass v_class;

	/** Create a new SS125 vehicle class */
	private SS125VehClass(VehLengthClass vlc) {
		v_class = vlc;
	}

	/** Get the count of SS125 vehicle classes */
	static public final int size = values().length;

	/** Get an SS125 vehicle class from an ordinal */
	static public SS125VehClass fromOrdinal(int o) {
		for(SS125VehClass vc: SS125VehClass.values()) {
			if(vc.ordinal() == o)
				return vc;
		}
		return SHORT;
	}
}
