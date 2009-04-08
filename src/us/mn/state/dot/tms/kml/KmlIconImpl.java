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
public class KmlIconImpl extends KmlLinkImpl implements KmlIcon 
{
	/** element name */
	public static final String ELEM_NAME = "Icon";

	/** constructor */
	public KmlIconImpl() {
		super.setKmlHref("");
	}

	/** constructor */
	public KmlIconImpl(String href) {
		super.setKmlHref(href);
	}

	/** render to kml, as below...
	 *	<Icon><href>...</href></Icon>
	 **/
	public String renderKml() {
		return Kml.element(ELEM_NAME, renderInnerKml());
	}

	/** render inner elements to kml */
	public String renderInnerKml() {
		return super.renderInnerKml();
	}
}
