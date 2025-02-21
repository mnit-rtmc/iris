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
 * Ramp meter lock.
 *
 * @author Douglas Lau
 */
public class MeterLock {

	/** Lock reason */
	static private final String REASON = "reason";

	/** REASON: Incident */
	static private final String REASON_INCIDENT = "incident";

	/** REASON: Knocked down pole */
	static private final String REASON_KNOCKED_DOWN = "knocked down";

	/** REASON: Indication failure */
	static private final String REASON_INDICATION = "indication";

	/** REASON: Maintenance */
	static private final String REASON_MAINTENANCE = "maintenance";

	/** REASON: Construction */
	static private final String REASON_CONSTRUCTION = "construction";

	/** REASON: Testing */
	static public final String REASON_TESTING = "testing";

	/** Lock reasons */
	static public final String[] REASONS = {
		"",
		REASON_INCIDENT,
		REASON_KNOCKED_DOWN,
		REASON_INDICATION,
		REASON_MAINTENANCE,
		REASON_CONSTRUCTION,
		REASON_TESTING,
	};

	/** Release rate (vehicles per hour; Integer) */
	static private final String RATE = "rate";

	/** User ID */
	static private final String USER = "user_id";

	/** Expires (ISO 8601 date) */
	static private final String EXPIRES = "expires";

	/** Lock JSON object */
	private final JSONObject lock;

	/** Create a meter lock */
	public MeterLock(String lk) {
		JSONObject jo = new JSONObject();
		try {
			if (lk != null)
				jo = new JSONObject(lk);
		}
		catch (JSONException e) {
			System.err.println("LockBuilder: " + e.getMessage());
		}
		lock = jo;
	}

	/** Clear the lock */
	private void clear() {
		try {
			lock.remove(REASON);
			lock.remove(RATE);
			lock.remove(USER);
			lock.remove(EXPIRES);
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
		try {
			if (r != null)
				lock.put(REASON, r);
			else
				clear();
		}
		catch (JSONException e) {
			System.err.println("setReason: " + e.getMessage());
		}
	}

	/** Get the lock rate, or null */
	public Integer optRate() {
		return lock.has(RATE) ? lock.optInt(RATE) : null;
	}

	/** Set the lock metering rate */
	public void setRate(Integer r) {
		try {
			lock.put(RATE, r);
		}
		catch (JSONException e) {
			System.err.println("setRate: " + e.getMessage());
		}
	}

	/** Get the lock user, or null */
	public String optUser() {
		return lock.optString(USER, null);
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

	/** Get the lock expires, or null */
	public String optExpires() {
		return lock.optString(EXPIRES, null);
	}

	/** Set the lock expiration date/time */
	public void setExpires(boolean expires) {
		String ex = (expires) ? makeLockExpires() : null;
		try {
			lock.put(EXPIRES, ex);
		}
		catch (JSONException e) {
			System.err.println("setExpires: " + e.getMessage());
		}
	}

	/** Make lock expires date/time */
	private String makeLockExpires() {
		long now = TimeSteward.currentTimeMillis();
		// expires in 15 minutes
		return TimeSteward.format8601(now + 15 * 60 * 1000);
	}

	/** Get lock as a JSON string, or null */
	@Override
	public String toString() {
		return (!lock.isEmpty()) ? lock.toString() : null;
	}
}
