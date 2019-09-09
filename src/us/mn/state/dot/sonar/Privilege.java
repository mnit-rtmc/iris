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
package us.mn.state.dot.sonar;

/**
 * A privilege controls access to the SONAR namespace.
 * Type name is used to match either read or write privileges.
 * Object, group and attribute names match only with write privileges.
 * For read privileges, these must be blank (empty string).
 *
 * @author Douglas Lau
 */
public interface Privilege extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "privilege";

	/** Get the capability */
	Capability getCapability();

	/** Get the type name */
	String getTypeN();

	/** Set the type name */
	void setTypeN(String n);

	/** Get the object name */
	String getObjN();

	/** Set the object name */
	void setObjN(String n);

	/** Get the group name */
	String getGroupN();

	/** Set the group name */
	void setGroupN(String n);

	/** Get the attribute name */
	String getAttrN();

	/** Set the attribute name */
	void setAttrN(String n);

	/** Get the write privilege */
	boolean getWrite();

	/** Set the write privilege */
	void setWrite(boolean w);
}
