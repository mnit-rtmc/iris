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
package us.mn.state.dot.tms.utils;

import java.lang.StringBuilder;

/**
 * Static XML convenience methods.
 *
 * @author Michael Darter
 * @see SXmlTest
 * @created 11/25/08
 * @company AHMCT, University of California, Davis
 */
public class SXml {

	/** newline */
	final static String R = "\n";

	/** add a comment */
	public static StringBuilder comment(StringBuilder sb, String c) {
		sb.append("<!-- ");
		sb.append(c);
		sb.append(" -->");
		return sb;
	}

	/** return an element: '<elem>value</elem>' */
	public static String element(String elem, String val)
	{
		StringBuilder sb = new StringBuilder();
		elementOpen(sb, elem);
		sb.append(val);
		elementClose(sb, elem);
		return sb.toString();
	}

	/** append an element: '<elem>value</elem>' */
	public static StringBuilder element(StringBuilder sb, String elem, String val)
	{
		elementOpen(sb, elem);
		sb.append(val);
		elementClose(sb, elem);
		return(sb);
	}

	/** append an element open: '<elem>' */
	public static StringBuilder elementOpen(StringBuilder sb, String elem)
	{
		sb.append("<");
		sb.append(elem);
		sb.append(">");
		return(sb);
	}

	/** append an element close: '</elem>' */
	public static StringBuilder elementClose(StringBuilder sb, String elem)
	{
		sb.append("</");
		sb.append(elem);
		sb.append(">");
		return(sb);
	}

	/** given html, return a string that can be used as a description */
	public static String htmlLInk(String html) {
		if(html == null)
			html = "";
		return "<![CDATA[" + html + "]]>";
	}

	/** return an html link */
	public static String htmlLink(String url, String desc) {
		if(url == null || desc == null)
			return "";
		return "<a href=\"" + url + "\">" + desc + "</a>";
	}

	/** Extract underlined text from the argument.
	 *  @return Null on failure or if no underline text exists,
	 *	    else the underlined text, which might have length 0. */
	static public String extractUnderline(String xml) {
		final String TAG_OPEN = "<u>";
		final String TAG_CLOSE = "</u>";
		if(xml == null || xml.isEmpty())
			return null;
		int s = xml.indexOf(TAG_OPEN);
		if(s < 0)
			return null;
		int e = xml.indexOf(TAG_CLOSE, s);
		if(e < 0)
			return null;
		if(s >= e)
			return "";
		return xml.substring(s + TAG_OPEN.length(), e);
	}
}
