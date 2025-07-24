/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;

/**
 * DMS lock.
 *
 * @author Douglas Lau
 */
public class DmsLock {

	/** Lock reason */
	static private final String REASON = "reason";

	/** REASON: Incident */
	static private final String REASON_INCIDENT = "incident";

	/** REASON: Testing */
	static private final String REASON_TESTING = "testing";

	/** REASON: Maintenance */
	static private final String REASON_MAINTENANCE = "maintenance";

	/** REASON: Construction */
	static private final String REASON_CONSTRUCTION = "construction";

	/** Lock reasons */
	static public final String[] REASONS = {
		"",
		REASON_INCIDENT,
		REASON_TESTING,
		REASON_MAINTENANCE,
		REASON_CONSTRUCTION,
	};

	/** Expires (ISO 8601 date) */
	static private final String EXPIRES = "expires";

	/** User ID */
	static private final String USER = "user_id";

	/** Lock JSON object */
	private final JSONObject lock;

	/** Compare for equality */
	@Override
	public boolean equals(Object other) {
		if (other instanceof DmsLock) {
			DmsLock o = (DmsLock) other;
			return lock.equals(o.lock);
		} else
			return false;
	}

	/** Get hash code */
	@Override
	public int hashCode() {
		return lock.hashCode();
	}

	/** Create a DMS lock */
	public DmsLock(String lk) {
		JSONObject jo = new JSONObject();
		try {
			if (lk != null)
				jo = new JSONObject(lk);
		}
		catch (JSONException e) {
			System.err.println("DmsLock: " + e.getMessage());
		}
		lock = jo;
	}

	/** Clear the lock */
	private void clear() {
		try {
			lock.remove(REASON);
			lock.remove(EXPIRES);
			lock.remove(USER);
		}
		catch (JSONException e) {
			System.err.println("clear: " + e.getMessage());
		}
	}

	/** Get the lock reason, or null */
	public String optReason() {
		return lock.optString(REASON, null);
	}

	/** Set the lock reason */
	public void setReason(String r) {
		if (r != null) {
			putReason(r);
			// Only allow "incident" or "testing" for locked on
			boolean lock_on = (getOnMinutes() != null);
			putExpires((lock_on) ? makeExpires() : null);
		} else
			clear();
	}

	/** Put the lock reason */
	private void putReason(String r) {
		try {
			lock.put(REASON, r);
		}
		catch (JSONException e) {
			System.err.println("putReason: " + e.getMessage());
		}
	}

	/** Get the lock expires, or null */
	public String optExpires() {
		return lock.optString(EXPIRES, null);
	}

	/** Put the lock expiration date/time */
	private void putExpires(String ex) {
		try {
			lock.put(EXPIRES, ex);
		}
		catch (JSONException e) {
			System.err.println("putExpires: " + e.getMessage());
		}
	}

	/** Make lock expires date/time */
	private String makeExpires() {
		Integer min = getOnMinutes();
		if (min != null) {
			long now = TimeSteward.currentTimeMillis();
			long ms = min * 60 * 1000;
			return TimeSteward.format8601(now + ms);
		} else
			return null;
	}

	/** Get ON duration depending on the lock reason */
	private Integer getOnMinutes() {
		String reason = optReason();
		if (REASON_INCIDENT.equals(reason))
			return 30;
		else if (REASON_TESTING.equals(reason))
			return 5;
		else
			return null;
	}

	/** Get the lock user, or null */
	public String optUser() {
		return lock.optString(USER, null);
	}

	/** Get the lock user */
	public String getUser() {
		String user = optUser();
		return (user != null) ? user : "UNKNOWN";
	}

	/** Set the lock user */
	public void setUser(String u) {
		try {
			lock.put(USER, u);
		}
		catch (JSONException e) {
			System.err.println("setUser: " + e.getMessage());
		}
	}

	/** Get lock as a JSON string, or null */
	@Override
	public String toString() {
		return (!lock.isEmpty()) ? lock.toString() : null;
	}
}
