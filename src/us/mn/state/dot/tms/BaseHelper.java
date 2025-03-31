/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;

/**
 * Base helper class for client/server interfaces.
 *
 * @author Douglas Lau
 */
abstract public class BaseHelper {

	/** User name for automatic actions */
	static public final String USER_AUTO = "AUTO";

	/** Compare two (possibly-null) objects for equality */
	static public boolean objectEquals(Object o0, Object o1) {
		return (o0 != null) ? o0.equals(o1) : o1 == null;
	}

	/** SONAR namespace. For server code this is set in TMSImpl and
	 * for client code this is set in SonarState. */
	static public Namespace namespace;

	/** SONAR user.  For server code this is null. */
	static public User user;

	/** Prevent object creation */
	protected BaseHelper() {
		assert false;
	}

	/** Check if a type can be read */
	static protected boolean canRead(String tname) {
		int lvl = namespace.accessLevel(new Name(tname), user);
		return lvl >= AccessLevel.VIEW.ordinal();
	}

	/** Get optional JSON attribute, or null */
	static public Object optJson(String json, String key) {
		if (json == null)
			return null;
		try {
			JSONObject jo = new JSONObject(json);
			return jo.opt(key);
		}
		catch (JSONException e) {
			System.err.println("optJson: " + json +
				"\nmsg: " + e.getMessage());
			return null;
		}
	}

	/** Make a JSON object */
	static private JSONObject makeJson(String json) {
		if (json != null) {
			try {
				return new JSONObject(json);
			}
			catch (JSONException e) {
				System.err.println("makeJson " + json +
					"\nmsg: " + e.getMessage());
			}
		}
		return new JSONObject();
	}

	/** Put a key/value pair into a JSON object */
	static public String putJson(String json, String key, Object val) {
		JSONObject jo = makeJson(json);
		try {
			jo.put(key, val);
		}
		catch (JSONException e) {
			System.err.println("putJson " + key + ':' + val +
				"\nmsg: " + e.getMessage());
		}
		return (!jo.isEmpty()) ? jo.toString() : null;
	}
}
