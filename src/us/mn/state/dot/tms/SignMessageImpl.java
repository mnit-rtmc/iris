/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.User;

/**
 * A sign message represents a message which can be displayed on a dynamic
 * message sign (DMS). It contains the text associated with the message and a
 * bitmap for each page of the message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageImpl extends BaseObjectImpl implements SignMessage {

	/** Last allocated system message ID */
	static protected int last_id = 0;

	/** Create a unique sign message name */
	static protected synchronized String createUniqueName() {
		String n = createNextName();
		while(namespace.lookupObject(SONAR_TYPE, n) != null)
			n = createNextName();
		return n;
	}

	/** Create the next system message name */
	static protected String createNextName() {
		last_id++;
		// Check if the ID has rolled over to negative numbers
		if(last_id < 0)
			last_id = 0;
		return "system_" + last_id;
	}

	/** Load all the sign text */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading sign messages...");
		namespace.registerType(SONAR_TYPE, SignMessageImpl.class);
		store.query("SELECT name, multi, bitmaps, priority, " +
			"duration FROM iris.sign_message;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignMessageImpl(
					row.getString(1),	// name
					row.getString(2),	// multi
					row.getString(3),	// bitmaps
					row.getInt(4),		// priority
					row.getInt(5)		// duration
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("multi", multi);
		map.put("bitmaps", bitmaps);
		map.put("priority", priority);
		if(duration != null)
			map.put("duration", duration);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new sign message (by SONAR clients) */
	public SignMessageImpl(String n) {
		super(n);
	}

	/** Create a sign message */
	protected SignMessageImpl(String n, String m, String b, int p,
		Integer d)
	{
		super(n);
		multi = m;
		bitmaps = b;
		priority = p;
		// FIXME: the ancient postgresql driver has a bug which makes
		// a NULL column return 0 for numeric datatypes. This workaround
		// can be removed after upgrading to newer JDBC driver. These
		// fields cannot be 0 anyway, so this trick works in this case.
		if(d == 0)
			d = null;
		duration = d;		
	}

	/** Create a new sign message (by IRIS) */
	public SignMessageImpl(String m, String b, DMSMessagePriority p,
		Integer d)
	{
		super(createUniqueName());
		multi = m;
		bitmaps = b;
		priority = p.ordinal();
		if(d != null && d.equals(0))
			d = null;
		duration = d;
	}

	/** Check if the sign message is blank */
	public boolean isBlank() {
		return isMultiBlank() && isBitmapBlank();
	}

	/** Check if the MULTI string is blank */
	protected boolean isMultiBlank() {
		return new MultiString(multi).isBlank();
	}

	/** Check if the bitmap is blank */
	protected boolean isBitmapBlank() {
		try {
			for(byte b: Base64.decode(bitmaps)) {
				if(b != 0)
					return false;
			}
			return true;
		}
		catch(IOException e) {
			return false;
		}
	}

	/** Message MULTI string, contains message text for all pages */
	protected String multi;

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.MultiString */
	public String getMulti() {
		return multi;
	}

	/** Bitmap data for each page (Base64-encoded) */
	protected String bitmaps;

	/** Get the bitmaps for all pages of the message.
	 * @return Base64-encoded bitmap data.
	 * @see us.mn.state.dot.tms.Base64 */
	public String getBitmaps() {
		return bitmaps;
	}

	/** Message activation priority */
	protected int priority;

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getPriority() {
		return priority;
	}

	/** Get the message run-time priority.
	 * @return Run-time priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getRunTimePriority() {
		if(isBlank())
			return DMSMessagePriority.BLANK.ordinal();
		else
			return getPriority();
	}

	/** Duration of this message (minutes) */
	protected Integer duration;

	/** Get the message duration.
	 * @return Duration in minutes; null means indefinite. */
	public Integer getDuration() {
		return duration;
	}
}
