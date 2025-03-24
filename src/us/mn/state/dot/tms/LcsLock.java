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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.TimeSteward;

/**
 * Ramp meter lock.
 *
 * @author Douglas Lau
 */
public class LcsLock {

	/** Lock reason */
	static private final String REASON = "reason";

	/** REASON: Incident */
	static private final String REASON_INCIDENT = "incident";

	/** REASON: Testing */
	static private final String REASON_TESTING = "testing";

	/** REASON: Indication failure */
	static private final String REASON_INDICATION = "indication";

	/** REASON: Maintenance */
	static private final String REASON_MAINTENANCE = "maintenance";

	/** REASON: Construction */
	static private final String REASON_CONSTRUCTION = "construction";

	/** Lock reasons */
	static public final String[] REASONS = {
		"",
		REASON_INCIDENT,
		REASON_TESTING,
		REASON_INDICATION,
		REASON_MAINTENANCE,
		REASON_CONSTRUCTION,
	};

	/** Array of lane indications (ordinal of LcsIndication) */
	static private final String INDICATIONS = "indications";

	/** Expires (ISO 8601 date) */
	static private final String EXPIRES = "expires";

	/** User ID */
	static private final String USER = "user_id";

	/** Lock JSON object */
	private final JSONObject lock;

	/** Compare for equality */
	@Override
	public boolean equals(Object other) {
		if (other instanceof LcsLock) {
			LcsLock o = (LcsLock) other;
			return lock.equals(o.lock);
		} else
			return false;
	}

	/** Get hash code */
	@Override
	public int hashCode() {
		return lock.hashCode();
	}

	/** Create a LCS lock */
	public LcsLock(String lk) {
		JSONObject jo = new JSONObject();
		try {
			if (lk != null)
				jo = new JSONObject(lk);
		}
		catch (JSONException e) {
			System.err.println("LcsLock: " + e.getMessage());
		}
		lock = jo;
	}

	/** Clear the lock */
	private void clear() {
		try {
			lock.remove(REASON);
			lock.remove(INDICATIONS);
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
			if (getOnMinutes() == null) {
				putIndications(null);
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

	/** Get the indications, or null */
	public int[] optIndications() {
		if (lock.has(INDICATIONS)) {
			JSONArray arr = lock.optJSONArray(INDICATIONS);
			if (arr != null) {
				try {
					int[] ind = new int[arr.length()];
					for (int i = 0; i < arr.length(); i++)
						ind[i] = arr.getInt(i);
					return ind;
				}
				catch (JSONException e) {
					System.err.println("optIndications: " +
						e.getMessage());
				}
			}
		}
		return null;
	}

	/** Set the lock indications */
	public void setIndications(int[] ind) {
		putIndications(ind);
		// Only allow "incident" or "testing" for locked on
		if (ind != null && getOnMinutes() == null)
			putReason(REASON_INCIDENT);
		putExpires((ind != null) ? makeExpires() : null);
	}

	/** Put the lock indications */
	private void putIndications(int[] ind) {
		try {
			lock.put(INDICATIONS, ind);
		}
		catch (JSONException e) {
			System.err.println("putIndications: " + e.getMessage());
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
