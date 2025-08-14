/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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

	/** Write/create/delete access exceptions for OPERATE level */
	static final String[] TYPE_OPERATE = {
		"incident"
	};

	/** Write/create/delete access exceptions for MANAGE level */
	static final String[] TYPE_MANAGE = {
		"action_plan", "device_action", "msg_pattern", "msg_line",
		"time_action"
	};

	/** Write access exceptions for OPERATE level */
	static final String[][] WRITE_OPERATE = {
		{ "action_plan", "phase" },
		{ "beacon", "state" },
		{ "camera", "deviceRequest" },
		{ "camera", "ptz" },
		{ "camera", "recallPreset" },
		{ "comm_link", "pollEnabled" },
		{ "controller", "condition" },
		{ "controller", "deviceRequest" },
		{ "detector", "fieldLength" },
		{ "detector", "forceFail" },
		{ "dms", "deviceRequest" },
		{ "dms", "lock" },
		{ "gate_arm_array", "armStateNext" },
		{ "gate_arm_array", "ownerNext" },
		{ "lcs", "deviceRequest" },
		{ "lcs", "lock" },
		{ "modem", "enabled" },
		{ "ramp_meter", "deviceRequest" },
		{ "ramp_meter", "lock" },
		{ "video_monitor", "camera" },
		{ "video_monitor", "deviceRequest" },
		{ "video_monitor", "playList" },
		{ "weather_sensor", "deviceRequest" },
	};

	/** Write access exceptions for MANAGE level */
	static final String[][] WRITE_MANAGE = {
		{ "alarm", "description" },
		{ "beacon", "message" },
		{ "beacon", "preset" },
		{ "camera", "publish" },
		{ "camera", "storePreset" },
		{ "comm_config", "timeoutMs" },
		{ "comm_config", "retryThreshold" },
		{ "comm_config", "idleDisconnectSec" },
		{ "comm_config", "noResponseDisconnectSec" },
		{ "comm_link", "description" },
		{ "detector", "abandoned" },
		{ "dms", "preset" },
		{ "domain", "enabled" },
		{ "modem", "timeoutMs" },
		{ "play_list", "entries" },
		{ "ramp_meter", "storage" },
		{ "ramp_meter", "maxWait" },
		{ "ramp_meter", "algorithm" },
		{ "ramp_meter", "amTarget" },
		{ "ramp_meter", "pmTarget" },
		{ "role", "enabled" },
		{ "user", "enabled" },
		{ "user", "password" },
		{ "video_monitor", "restricted" },
		{ "video_monitor", "monitorStyle" },
		{ "weather_sensor", "siteId" },
		{ "weather_sensor", "altId" },
	};

	/** Get access level required to write object/attribute */
	public int accessWrite() {
		if (canWriteOperate())
			return AccessLevel.OPERATE.ordinal();
		else if (canWriteManage())
			return AccessLevel.MANAGE.ordinal();
		else
			return AccessLevel.CONFIGURE.ordinal();
	}

	/** Check for write access at OPERATE level */
	private boolean canWriteOperate() {
		String typ = getTypePart();
		for (String acc: TYPE_OPERATE) {
			if (acc.equals(typ))
				return true;
		}
		if (isAttribute()) {
			String att = getAttributePart();
			for (String[] acc: WRITE_OPERATE) {
				if (acc[0].equals(typ) && acc[1].equals(att))
					return true;
			}
		}
		return false;
	}

	/** Check for write access at MANAGE level */
	private boolean canWriteManage() {
		String typ = getTypePart();
		for (String acc: TYPE_MANAGE) {
			if (acc.equals(typ))
				return true;
		}
		if (isAttribute()) {
			String att = getAttributePart();
			for (String[] acc: WRITE_MANAGE) {
				if (acc[0].equals(typ) && acc[1].equals(att))
					return true;
			}
		}
		return false;
	}
}
