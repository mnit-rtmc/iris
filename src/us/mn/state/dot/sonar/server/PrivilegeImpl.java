/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Capability;

/**
 * A privilege controls access to the SONAR namespace.
 *
 * @author Douglas Lau
 */
public class PrivilegeImpl implements Privilege {

	/** Namespace regex pattern */
	static private final Pattern PATTERN = Pattern.compile("[A-Za-z0-9_]*");

	/** Namespace object regex pattern */
	static protected final Pattern OBJ_PATTERN =
		Pattern.compile("[-A-Za-z0-9_.*+?()]*");

	/** Check for a valid namespace pattern */
	static protected void checkPattern(String n) throws NamespaceError {
		checkPattern(PATTERN, n);
	}

	/** Check for a valid namespace pattern */
	static protected void checkPattern(Pattern p, String n)
		throws NamespaceError
	{
		Matcher m = p.matcher(n);
		if (!m.matches())
			throw NamespaceError.nameInvalid(n);
	}

	/** Destroy a privilege */
	@Override
	public void destroy() {
		// Subclasses must remove privilege from backing store
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Privilege name */
	private final String name;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return name;
	}

	/** Create a new privilege */
	public PrivilegeImpl(String n) {
		name = n;
	}

	/** Create a new privilege */
	public PrivilegeImpl(String n, Capability c) {
		name = n;
		capability = c;
	}

	/** Capability */
	private Capability capability;

	/** Get the capability */
	@Override
	public Capability getCapability() {
		return capability;
	}

	/** Type name */
	private String typeN = "";

	/** Get the type name */
	@Override
	public String getTypeN() {
		return typeN;
	}

	/** Set the type name */
	@Override
	public void setTypeN(String n) {
		typeN = n;
	}

	/** Set the type name */
	public void doSetTypeN(String n) throws Exception {
		checkPattern(n);
		setTypeN(n);
	}

	/** Object name */
	private String objN = "";

	/** Get the object name */
	@Override
	public String getObjN() {
		return objN;
	}

	/** Set the object name */
	@Override
	public void setObjN(String n) {
		objN = n;
	}

	/** Set the object name */
	public void doSetObjN(String n) throws Exception {
		checkPattern(OBJ_PATTERN, n);
		setObjN(n);
	}

	/** Group name */
	private String groupN = "";

	/** Get the group name */
	@Override
	public String getGroupN() {
		return groupN;
	}

	/** Set the group name */
	@Override
	public void setGroupN(String n) {
		groupN = n;
	}

	/** Set the group name */
	public void doSetGroupN(String n) throws Exception {
		checkPattern(n);
		setGroupN(n);
	}

	/** Attribute name */
	private String attrN = "";

	/** Get the attribute name */
	@Override
	public String getAttrN() {
		return attrN;
	}

	/** Set the attribute name */
	@Override
	public void setAttrN(String n) {
		attrN = n;
	}

	/** Set the attribute name */
	public void doSetAttrN(String n) throws Exception {
		checkPattern(n);
		setAttrN(n);
	}

	/** Write access privilege */
	private boolean write = false;

	/** Get the write privilege */
	@Override
	public boolean getWrite() {
		return write;
	}

	/** Set the write privilege */
	@Override
	public void setWrite(boolean w) {
		write = w;
	}
}
