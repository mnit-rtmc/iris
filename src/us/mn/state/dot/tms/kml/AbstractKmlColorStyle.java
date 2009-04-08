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
 * Abstract kml color style.
 *
 * @author Michael Darter
 * @created 11/27/08
 * @see KmlObject
 */
public abstract class AbstractKmlColorStyle 
	implements KmlColorStyle 
{
	/** color */
	protected KmlColor m_color = new KmlColorImpl();

	/** set color */
	public void setKmlColor(KmlColor c) {
		m_color = c;
	}

	/** get color */
	public KmlColor getKmlColor() {
		return m_color;
	}

	/** render to kml, as below...
	 *	<color>ffff0000</color>
	 **/
	public String renderKml() {
		return renderInnerKml();
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		if(m_color == null)
			return "";
		return m_color.renderKml();
	}
}
