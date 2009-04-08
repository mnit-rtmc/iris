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
 * Kml icon implementation.
 *
 * @author Michael Darter
 * @created 11/27/08
 * @see KmlObject
 */
public class KmlLinkImpl implements KmlLink 
{
	/** element name */
	public static final String ELEM_NAME = "Link";

	/** href */
	protected String m_href = "";

	/** set href */
	public void setKmlHref(String href) {
		m_href = href;
	}

	/** get href */
	public String getKmlHref() {
		return m_href;
	}

	/** render to kml, as below...
	 *	<Link><href>...</href></Link>
	 **/
	public String renderKml() {
		return Kml.element(ELEM_NAME, renderInnerKml());
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		return Kml.element("href", m_href);
	}
}
