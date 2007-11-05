/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2006  Minnesota Department of Transportation
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

import java.io.Serializable;

/**
 * DmsMessage is a class which contains the properties of a single line message
 * for display on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public class DmsMessage implements Comparable, Serializable, Cloneable {

	/** Max id assigned to a message */
	static protected int max_id;

	/** Get the max id */
	static public int nextId() { return max_id + 1; }

	/** Message ID */
	public final Integer id;

	/** DMS id (null for global messages) */
	public final String dms;

	/** Line number on sign (usually 1-3) */
	public final short line;

	/** Message text */
	public String message;

	/** Width of message (in pixels) */
	public int m_width;

	/** Abbreviated message text */
	public String abbrev;

	/** Width of abbreviation (in pixels) */
	public int a_width;

	/** Message ordering priority */
	public short priority;

	/** Create a new DMS message */
	public DmsMessage(int id, String dms, short line, String message,
		String abbrev, short priority)
	{
		max_id = Math.max(id, max_id);
		this.id = new Integer(id);
		this.dms = dms;
		this.line = line;
		this.message = message;
		this.abbrev = abbrev;
		this.priority = priority;
	}

	/** Create clone of the DMS message */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/** Compare with another DmsMessage */
	public int compareTo(Object o) {
		DmsMessage other = (DmsMessage)o;
		int c = line - other.line;
		if(c == 0)
			c = priority - other.priority;
		if(c == 0)
			c = message.compareTo(other.message);
		if(c == 0)
			c = id.compareTo(other.id);
		return c;
	}
}
