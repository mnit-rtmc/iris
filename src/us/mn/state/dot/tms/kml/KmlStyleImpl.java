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
 * Kml style implementation.
 *
 * @author Michael Darter
 * @created 11/27/08
 * @see KmlObject
 */
public class KmlStyleImpl implements KmlStyle 
{
	/** element name */
	public static final String ELEM_NAME = "Style";

	/** IconStyle */
	protected KmlIconStyle m_iconstyle;

	/** constructor */
	public KmlStyleImpl() {
		m_iconstyle = new KmlIconStyleImpl();
	}

	/** render to kml */
	public String renderKml() {
		return Kml.element(ELEM_NAME, renderInnerKml());
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		return m_iconstyle.renderKml();
	}

	/** get icon style */
	public KmlIconStyle getIconStyle() {
		return m_iconstyle;
	}

	/** set icon style */
	public void setIconStyle(KmlIconStyle is) {
		m_iconstyle = is;
	}
}
