/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010 AHMCT, University of California, Davis
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

/**
 * Cell renderer sizes.
 * @see StyleSummary
 * @author Michael Darter
 */
public enum CellRendererSize {
	SMALL("S", "Small"),
	MEDIUM("M", "Medium"),
	LARGE("L", "Large");

	/** Short name */
	public final String m_sname;

	/** Name */
	public final String m_name;

	/** constructor */
	private CellRendererSize(String shortName, String name) {
		m_sname = shortName;
		m_name = name;
	}

	/** Get a size given a short name */
	public static CellRendererSize get(String sname) {
		if(sname.equals(SMALL.m_sname))
			return SMALL;
		else if(sname.equals(MEDIUM.m_sname))
			return MEDIUM;
		else if(sname.equals(LARGE.m_sname))
			return LARGE;
		else {
			assert false;
			return LARGE;
		}
	}
}
