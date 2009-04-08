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
 * Kml icon style implementation.
 *
 * @author Michael Darter
 * @created 11/27/08
 * @see KmlObject
 */
public class KmlIconStyleImpl extends AbstractKmlColorStyle 
	implements KmlIconStyle 
{
	/** element name */
	public static final String ELEM_NAME = "IconStyle";

	/** scale */
	protected double m_scale = 1.0;

	/** icon */
	protected KmlIcon m_icon = new KmlIconImpl();

	/** constructor */
	public KmlIconStyleImpl() {
	}

	/** render to kml, as below...
	 *	<IconStyle>
	 *		<color>ffff0000</color>
	 *		<scale>1.0</scale>
	 *		<Icon><href>...</href></Icon>
	 *	</IconStyle>
	 **/
	public String renderKml() {
		return Kml.element(ELEM_NAME, renderInnerKml());
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.renderInnerKml());
		Kml.element(sb, "scale", Double.toString(m_scale));
		sb.append(m_icon.renderKml());
		return sb.toString();
	}

	/** get scale */
	public double getKmlScale() {
		return m_scale;
	}

	/** set scale */
	public void setKmlScale(double scale) {
		m_scale = scale;
	}

	/** get icon */
	public KmlIcon getKmlIcon() {
		return m_icon;
	}

	/** set icon */
	public void setKmlIcon(KmlIcon icon) {
		m_icon = icon;
	}
}
