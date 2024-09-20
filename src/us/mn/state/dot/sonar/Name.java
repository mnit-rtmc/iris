/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.AccessLevel;

/**
 * A name represents a type, object or attribute in SONAR namespace.
 *
 * @author Douglas Lau
 */
public class Name {

	/** Name separator */
	static public final String SEP = "/";

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
		return (path.length() > 0) ? path.split(SEP) : new String[0];
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
		return (parts.length > 0) ? parts[0] : "";
	}

	/** Get the object part */
	public String getObjectPart() {
		return (parts.length > 1) ? parts[1] : "";
	}

	/** Get the attribute part */
	public String getAttributePart() {
		return (parts.length > 2) ? parts[2] : "";
	}

	/** Get the full object name */
	public String getObjectName() {
		return getTypePart() + SEP + getObjectPart();
	}

	/** Get the attribute name with no object specified */
	public String getAttributeName() {
		return getTypePart() + SEP + SEP + getAttributePart();
	}

	/** Write access exceptions for OPERATE level */
	static final String[][] WRITE_OPERATE = {
		{ "action_plan", "phase" },
		{ "beacon", "state" },
		{ "camera", "ptz" },
		{ "camera", "publish" },
		{ "camera", "recallPreset" },
		{ "controller", "deviceRequest" },
		{ "detector", "fieldLength" },
		{ "detector", "forceFail" },
		{ "dms", "msgUser" },
		{ "gate_arm_array", "armState" },
		{ "lane_marking", "deployed" },
		{ "lcs_array", "lcsLock" },
		{ "ramp_meter", "mLock" },
		{ "ramp_meter", "rate" },
		{ "video_monitor", "camera" },
	};

	/** Write access exceptions for MANAGE level */
	static final String[][] WRITE_MANAGE = {
		{ "action_plan", "notes" },
		{ "action_plan", "syncActions" },
		{ "action_plan", "sticky" },
		{ "action_plan", "ignoreAutoFail" },
		{ "action_plan", "active" },
		{ "action_plan", "defaultPhase" },
		{ "beacon", "message" },
		{ "beacon", "notes" },
		{ "beacon", "preset" },
		{ "camera", "notes" },
		{ "camera", "storePreset" },
		{ "comm_config", "timeoutMs" },
		{ "comm_config", "idleDisconnectSec" },
		{ "comm_config", "noResponseDisconnectSec" },
		{ "comm_link", "pollEnabled" },
		{ "controller", "condition" },
		{ "controller", "notes" },
		{ "detector", "abandoned" },
		{ "detector", "notes" },
		{ "dms", "deviceRequest" },
		{ "dms", "notes" },
		{ "dms", "preset" },
		{ "domain", "enabled" },
		{ "gate_arm", "notes" },
		{ "gate_arm_array", "notes" },
		{ "lane_marking", "notes" },
		{ "lcs_array", "notes" },
		{ "modem", "enabled" },
		{ "modem", "timeoutMs" },
		{ "msg_line", "restrictHashtag" },
		{ "msg_line", "rank" },
		{ "msg_pattern", "composeHashtag" },
		{ "msg_pattern", "flashBeacon" },
		{ "ramp_meter", "notes" },
		{ "ramp_meter", "storage" },
		{ "ramp_meter", "maxWait" },
		{ "ramp_meter", "algorithm" },
		{ "ramp_meter", "amTarget" },
		{ "ramp_meter", "pmTarget" },
		{ "role", "enabled" },
		{ "user", "enabled" },
		{ "user", "password" },
		{ "video_monitor", "notes" },
		{ "video_monitor", "restricted" },
		{ "video_monitor", "monitorStyle" },
		{ "weather_sensor", "deviceRequest" },
		{ "weather_sensor", "siteId" },
		{ "weather_sensor", "altId" },
		{ "weather_sensor", "notes" },
	};

	/** Get access level required to write object/attribute */
	public int accessWrite() {
		if (isAttribute()) {
			String typ = getTypePart();
			String att = getAttributePart();
			for (String[] acc: WRITE_OPERATE) {
				if (acc[0].equals(typ) && acc[1].equals(att))
					return AccessLevel.OPERATE.ordinal();
			}
			for (String[] acc: WRITE_MANAGE) {
				if (acc[0].equals(typ) && acc[1].equals(att))
					return AccessLevel.MANAGE.ordinal();
			}
		} else if (isObject()) {
			// Allow CREATE/DELETE of SignMessage w/OPERATE
			if ("sign_message".equals(getTypePart()))
				return AccessLevel.OPERATE.ordinal();
		}
		return AccessLevel.CONFIGURE.ordinal();
	}
}
