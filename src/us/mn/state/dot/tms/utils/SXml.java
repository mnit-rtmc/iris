/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static XML convenience methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @see SXmlTest
 */
public class SXml {

	/** newline */
	private final static String R = "\n";

	/** add a comment */
	public static StringBuilder comment(StringBuilder sb, String c) {
		sb.append("<!-- ");
		sb.append(c);
		sb.append(" -->");
		return sb;
	}

	/** return an element: '<elem>value</elem>' */
	public static String element(String elem, String val) {
		StringBuilder sb = new StringBuilder();
		elementOpen(sb, elem);
		sb.append(val);
		elementClose(sb, elem);
		return sb.toString();
	}

	/** append an element: '<elem>value</elem>' */
	public static StringBuilder element(StringBuilder sb, String elem, 
		String val)
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
	public static StringBuilder elementClose(StringBuilder sb, 
		String elem)
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

	/** Check if the node is an element */
	static public boolean isElem(Node n) {
		if(n == null || n.getNodeType() != Node.ELEMENT_NODE) {
			Log.fine("Unexpected node type=" + n + 
				", expected element.");
			return false;
		} else
			return true;
	}

	/** Check the element name */
	static public boolean isElemName(Element e, String name) {
		if(e == null) {
			Log.fine("Unexpected null element.");
			return false;
		} else if(!e.getNodeName().equals(name)) {
			Log.fine("Unexpected elem name (null), " +
				e.getNodeName() + "), expected " + name);
			return false;
		} else
			return true;
	}

	/** Get the text value of the child with specified name.
	 * @param e The parent element.
	 * @param name The name of the child.
	 * @return The text value of the child, else null on error. */
	static public String lookupChildText(Element e, String name) {
		Element c = lookupChild(e, name);
		if(c == null)
			return null;
		else
			return c.getTextContent();
	}

	/** Get the first child with the specified name.
	 * @param e The parent element
	 * @param name The name of the child element to return
	 * @return The child element. */
	static public Element lookupChild(Element e, String name) {
		NodeList children = e.getChildNodes();
		for(int c = 0; c < children.getLength(); c++) {
			Node child = children.item(c);
			if(child instanceof Element &&
			   child.getNodeName().equalsIgnoreCase(name))
				return (Element)child;
		}
		return null;
	}

	/** Retrieve the specified attribute or null on error. */
	static public String getAttr(Element e, int i) {
	        // get attribute: ID
	        NamedNodeMap attrs = e.getAttributes();
		int na = attrs.getLength();
	        if(na < 1 || i >= na)
	                return null;
	        Attr a = (Attr) attrs.item(i);
		return a.getNodeValue();
	}
}
