/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.kml;

import java.lang.StringBuilder;

/**
 * Kml color implementation.
 *
 * @author Michael Darter
 * @created 11/27/08
 * @see KmlObject
 */
public class KmlColorImpl implements KmlColor 
{
	/** element name */
	public static final String ELEM_NAME = "color";

	/** predefined colors: AABBGGRR */
	public static KmlColor White = new KmlColorImpl("ffffffff");
	public static KmlColor Black = new KmlColorImpl("ff000000");
	public static KmlColor Blue = new KmlColorImpl("ffff5500");
	public static KmlColor Green = new KmlColorImpl("ff00ff00");
	public static KmlColor Red = new KmlColorImpl("ff0000ff");
	public static KmlColor LightBlue = new KmlColorImpl("ffff0000");
	public static KmlColor Yellow = new KmlColorImpl("ff00ffff");
	public static KmlColor Orange = new KmlColorImpl("ff00aaff");
	public static KmlColor Gray = new KmlColorImpl("ff767676");
	public static KmlColor LightGray = new KmlColorImpl("ffcccccc");

	/** color */
	protected String m_color = "ffffffff";

	/** get color string */
	public String getColorString() {
		return m_color;
	}

	/** set color string */
	public void setColorString(String cs) {
		if(cs == null)
			return;
		if(cs.length() != 8) {
			assert false : "Bogus color: "+cs+".";
			return;
		}
		m_color = cs;
	}

	/** constructor */
	public KmlColorImpl() {
		m_color = KmlColorImpl.White.getColorString();
	}

	/** constructor */
	public KmlColorImpl(String cstring) {
		setColorString(cstring);
	}

	/** constructor */
	public KmlColorImpl(KmlColor c) {
		setColorString(c.getColorString());
	}

	/** render to kml, e.g. <color>ffff0000</color> */
	public String renderKml() {
		return Kml.element(ELEM_NAME, getColorString());
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		return "";
	}
}
