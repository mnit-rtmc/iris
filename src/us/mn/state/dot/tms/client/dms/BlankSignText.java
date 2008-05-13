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
package us.mn.state.dot.tms.client.dms;

import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;

/**
 * Blank sign text for message selector combo boxes.
 *
 * @author Douglas Lau
 */
public class BlankSignText implements SignText {

	/** Get the SONAR object name */
	public String getName() {
		return "BLANK";
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the sign group */
	public SignGroup getSignGroup() {
		return null;
	}

	/** Set the line */
	public void setLine(short l) {
		// do nothing
	}

	/** Get the line */
	public short getLine() {
		return 0;
	}

	/** Set the message */
	public void setMessage(String m) {
		// do nothing
	}

	/** Get the message */
	public String getMessage() {
		return "";
	}

	/** Set the priority */
	public void setPriority(short p) {
		// do nothing
	}

	/** Get the priority */
	public short getPriority() {
		return 1;
	}

	/** Destroy the object */
	public void destroy() {
		// do nothing
	}
}
