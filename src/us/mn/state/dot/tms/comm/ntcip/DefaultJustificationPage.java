/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip DefaultJustificationPage object
 *
 * @author Douglas Lau
 */
public class DefaultJustificationPage extends MultiCfg implements ASN1Integer {

	/** Undefined page justification */
	static public final int UNDEFINED = 0;

	/** Whatever 'other' justification means */
	static public final int OTHER = 1;

	/** Top-justify text on a page */
	static public final int TOP = 2;

	/** Middle-justify text on a page */
	static public final int MIDDLE = 3;

	/** Bottom-justify text on a page */
	static public final int BOTTOM = 4;

	/** Justification descriptions */
	static protected final String[] JUSTIFICATION = {
		"???", "other", "top", "middle", "bottom"
	};

	/** Create a new DefaultJustificationPage object */
	public DefaultJustificationPage(int j) {
		super(7);
		setInteger(j);
	}

	/** Get the object name */
	protected String getName() { return "defaultJustificationPage"; }

	/** Actual default page justification */
	protected int justification;

	/** Set the integer value */
	public void setInteger(int value) {
		justification = value;
		if(justification < 0 || justification >= JUSTIFICATION.length)
			justification = UNDEFINED;
	}

	/** Get the integer value */
	public int getInteger() { return justification; }

	/** Get the object value */
	public String getValue() { return JUSTIFICATION[justification]; }
}
