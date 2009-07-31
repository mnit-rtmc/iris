/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 *  A fake system attribute. This is used for junit testing purposes
 *  so system attributes can be created without having to connect to 
 *  a running server.
 *
 * @author Michael Darter
 */
public class SystemAttrFake implements SystemAttribute {
	/** value */
	private String m_value = "";

	/** Constructor */
	public SystemAttrFake(String value) {
		m_value = value;
	}

	/** Set the attribute value */
	public void setValue(String v) {
		m_value = v;
	}

	/** Get the attribute value */
	public String getValue() {
		return m_value;
	}

	/** Get attribute name */
	public String getName() {
		return "";
	}

	/** Get type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Destroy */
	public void destroy() {}
}
