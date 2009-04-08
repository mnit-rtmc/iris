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

import us.mn.state.dot.tms.utils.SXml;

import java.lang.StringBuilder;
import java.util.ArrayList;

/**
 * Static KML convenience methods.
 *
 * @author Michael Darter
 * @created 11/25/08
 * @see KmlObject
 */
public class Kml extends SXml 
{
	/** newline */
	final static String R = "\n";

	/** return kml doc start */
	public static StringBuilder start(StringBuilder sb) {
		sb.append("<?xml version=\"1.0\"?> " + R);
		sb.append("<kml xmlns=\"http://earth.google.com/kml/2.1\">" + R);
		return sb;
	}

	/** return kml doc end */
	public static StringBuilder end(StringBuilder sb) {
		sb.append("</kml>" + R);
		return sb;
	}

	/** given html, return a string that can be used as a description */
	public static String htmlDesc(String html) {
		if(html == null)
			html = "";
		return "<![CDATA[" + html + "]]>";
	}

	/** build description item */
	public static String descItem(String label, String value) {
		return "<b>" + label + "</b>: " + value + "<br>";
	}
}

