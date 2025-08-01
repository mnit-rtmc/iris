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
	static public final String REASON_INCIDENT = "incident";

	/** REASON: Situation */
	static public final String REASON_SITUATION = "situation";

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
		REASON_SITUATION,
		REASON_TESTING,
		REASON_MAINTENANCE,
		REASON_CONSTRUCTION,
	};

	/** SignMessage name */
	static private final String MESSAGE = "message";

	/** Incident name */
	static private final String INCIDENT = "incident";

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
			lock.remove(MESSAGE);
			lock.remove(INCIDENT);
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
			if (!isReasonOn(r)) {
				putMessage(null);
				putExpires(null);
			}
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

	/** Check if the reason is "ON" */
	private boolean isReasonOn(String reason) {
		return REASON_INCIDENT.equals(reason) ||
		       REASON_SITUATION.equals(reason) ||
		       REASON_TESTING.equals(reason);
	}

	/** Get the lock message, or null */
	public String optMessage() {
		return lock.optString(MESSAGE, null);
	}

	/** Set the lock message */
	public void setMessage(String msg) {
		if (msg != null && !isReasonOn(optReason()))
			putReason(REASON_TESTING);
		if (msg == null)
			putExpires(null);
		putMessage(msg);
	}

	/** Put the lock message */
	private void putMessage(String msg) {
		try {
			lock.put(MESSAGE, msg);
		}
		catch (JSONException e) {
			System.err.println("putMessage: " + e.getMessage());
		}
	}

	/** Get the lock incident, or null */
	public String optIncident() {
		return lock.optString(INCIDENT, null);
	}

	/** Set the lock incident */
	public void setIncident(String inc) {
		try {
			lock.put(INCIDENT, inc);
		}
		catch (JSONException e) {
			System.err.println("setIncident: " + e.getMessage());
		}
	}

	/** Get the lock expires, or null */
	public String optExpires() {
		return lock.optString(EXPIRES, null);
	}

	/** Set the lock duration */
	public void setDuration(Integer min) {
		putExpires(makeExpires(getOnMinutes(min)));
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
	private String makeExpires(Integer min) {
		if (min != null) {
			long now = TimeSteward.currentTimeMillis();
			long ms = min * 60 * 1000;
			return TimeSteward.format8601(now + ms);
		} else
			return null;
	}

	/** Filter ON duration depending on the lock reason */
	private Integer getOnMinutes(Integer min) {
		String reason = optReason();
		if (REASON_INCIDENT.equals(reason))
			return min;
		else if (REASON_SITUATION.equals(reason))
			return min;
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
