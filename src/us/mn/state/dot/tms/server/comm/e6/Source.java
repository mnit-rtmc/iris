/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

/**
 * RF Source for Frequency and Uplink Source Control properties.
 *
 * @author Douglas Lau
 */
public enum Source {
	downlink, uplink;

	/** Get source from an ordinal value */
	static public Source fromOrdinal(int o) {
		for (Source s: values())
			if (s.ordinal() == o)
				return s;
		return null;
	}
}
