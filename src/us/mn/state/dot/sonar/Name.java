/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

/**
 * A name represents a type, object or attribute in SONAR namespace.
 *
 * @author Douglas Lau
 */
public class Name {

	/** Name separator */
	static private final String SEP = "/";

	/** Test if a SONAR path is absolute (versus relative) */
	static public boolean isAbsolute(String p) {
		return p.contains(SEP);
	}

	/** Name path */
	private final String path;

	/** Name parts */
	private final String[] parts;

	/** Create a new name */
	public Name(String n) {
		path = n;
		parts = getParts();
	}

	/** Get the parts of a name */
	private String[] getParts() {
		if (path.length() < 1)
			return new String[0];
		else
			return path.split(SEP);
	}

	/** Create a name with a type and object */
	public Name(String tname, String oname) {
		this(tname + SEP + oname);
	}

	/** Create a name with a type, object and attribute */
	public Name(String tname, String oname, String aname) {
		this(tname + SEP + oname + SEP + aname);
	}

	/** Create a name for a SONAR object */
	public Name(SonarObject o) {
		this(o.getTypeName(), o.getName());
	}

	/** Create a name for an attribute of a SONAR object */
	public Name(SonarObject o, String aname) {
		this(o.getTypeName(), o.getName(), aname);
	}

	/** Check if the name is a root name */
	public boolean isRoot() {
		return parts.length == 0;
	}

	/** Check if the name is a type name */
	public boolean isType() {
		return parts.length == 1;
	}

	/** Check if the name is an object name */
	public boolean isObject() {
		return parts.length == 2;
	}

	/** Check if the name is an attribute name */
	public boolean isAttribute() {
		return parts.length == 3;
	}

	/** Get the name as a string */
	@Override
	public String toString() {
		return path;
	}

	/** Get the type part */
	public String getTypePart() {
		if (parts.length > 0)
			return parts[0];
		else
			return "";
	}

	/** Get the object part */
	public String getObjectPart() {
		if (parts.length > 1)
			return parts[1];
		else
			return "";
	}

	/** Get the attribute part */
	public String getAttributePart() {
		if (parts.length > 2)
			return parts[2];
		else
			return "";
	}

	/** Get the full object name */
	public String getObjectName() {
		return getTypePart() + SEP + getObjectPart();
	}

	/** Get the attribute name with no object specified */
	public String getAttributeName() {
		return getTypePart() + SEP + SEP + getAttributePart();
	}

	/** Check for read privilege.
	 * @param p Privilege to check. */
	public boolean checkRead(Privilege p) {
		return p.getTypeN().equals(getTypePart());
	}

	/** Check for write privilege.
	 * @param p Privilege to check. */
	public boolean checkWrite(Privilege p) {
		// NOTE: object and attribute checks are
		//       only valid for write privileges.
		return p.getTypeN().equals(getTypePart())
		    && checkObj(p)
		    && checkAttr(p);
	}

	/** Check if an object matches a privilege */
	private boolean checkObj(Privilege p) {
		String o = p.getObjN();
		return "".equals(o) || getObjectPart().matches(o);
	}

	/** Check if an attribute matches a privilege */
	private boolean checkAttr(Privilege p) {
		String a = p.getAttrN();
		return "".equals(a) || getAttributePart().equals(a);
	}
}
