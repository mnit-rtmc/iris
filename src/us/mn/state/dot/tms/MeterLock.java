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

	/** REASON: Testing */
	static private final String REASON_TESTING = "testing";

	/** REASON: Knocked down pole */
	static private final String REASON_KNOCKED_DOWN = "knocked down";

	/** REASON: Indication failure */
	static private final String REASON_INDICATION = "indication";

	/** REASON: Maintenance */
	static private final String REASON_MAINTENANCE = "maintenance";

	/** REASON: Construction */
	static private final String REASON_CONSTRUCTION = "construction";

	/** REASON: Reserve */
	static private final String REASON_RESERVE = "reserve";

	/** Lock reasons */
	static public final String[] REASONS = {
		"",
		REASON_INCIDENT,
		REASON_TESTING,
		REASON_KNOCKED_DOWN,
		REASON_INDICATION,
		REASON_MAINTENANCE,
		REASON_CONSTRUCTION,
		REASON_RESERVE,
	};

	/** Release rate (vehicles per hour; Integer) */
	static private final String RATE = "rate";

	/** Expires (ISO 8601 date) */
	static private final String EXPIRES = "expires";

	/** User ID */
	static private final String USER = "user_id";

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
			System.err.println("MeterLock: " + e.getMessage());
		}
		lock = jo;
	}

	/** Clear the lock */
	private void clear() {
		try {
			lock.remove(REASON);
			lock.remove(RATE);
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
			if (!lock_on)
				putRate(null);
			putExpires(makeExpires(optRate()));
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

	/** Get the lock rate, or null */
	public Integer optRate() {
		return lock.has(RATE) ? lock.optInt(RATE) : null;
	}

	/** Set the lock metering rate */
	public void setRate(Integer rt) {
		putRate(rt);
		// Only allow "incident" or "testing" for locked on
		if (rt != null && getOnMinutes() == null)
			putReason(REASON_TESTING);
		putExpires(makeExpires(rt));
	}

	/** Put the lock metering rate */
	private void putRate(Integer rt) {
		try {
			lock.put(RATE, rt);
		}
		catch (JSONException e) {
			System.err.println("putRate: " + e.getMessage());
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
	private String makeExpires(Integer rt) {
		if (rt != null) {
			Integer min = getOnMinutes();
			if (min != null) {
				long now = TimeSteward.currentTimeMillis();
				long ms = min * 60 * 1000;
				return TimeSteward.format8601(now + ms);
			}
		}
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
