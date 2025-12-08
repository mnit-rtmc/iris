/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * ClearGuide authentication token container and related methods.
 *
 * @author Michael Darter
 */
public class Tokens {

	/** Get null-safe string */
	static private String safe(String str) {
		return (str != null ? str : "");
	}

	/** String is null or empty? */
	static private boolean empty(String str) {
		return safe(str).isEmpty();
	}

	/** Tokens valid? */
	static protected boolean valid(String ta, String tr) {
		return !empty(ta) && !empty(tr);
	}

	/** Get the ClearGuide customer key, never null */
	static private String getCustomerKey() {
		return safe(SystemAttrEnum.API_KEY_CLEARGUIDE.getString());
	}

	/** Get DMS API URI */
	static protected String getDmsApiUri() {
		StringBuilder sb = new StringBuilder();
		sb.append("https://api.iteris-clearguide.com/v1/");
		sb.append("dms/dms/?customer_key=").append(getCustomerKey());
		return sb.toString();
	}

	/** Access token, valid for 5 mins */
	private String tok_access = "";

	/** Refresh token, valid for 24 hours */
	private String tok_refresh = "";

	/** Authentication time */
	private long auth_time = 0;

	/** Get token */
	protected String getAccess() {
		return tok_access;
	}

	/** Get token */
	protected String getRefresh() {
		return tok_refresh;
	}

	/** Store tokens to associated comm link */
	protected void store(String ta, String tr) {
		if (valid(ta, tr)) {
			tok_access = safe(ta);
			tok_refresh = safe(tr);
			auth_time = System.currentTimeMillis();
		}
	}

	/** Tokens valid? */
	protected boolean valid() {
		return valid(tok_access, tok_refresh);
	}

	/** Clear tokens */
	protected void clear() {
		tok_refresh = "";
		tok_access = "";
	}

	/** Get tokens age in seconds */
	protected int getAge() {
		final long now_ms = System.currentTimeMillis();
		return Long.valueOf(now_ms - auth_time).intValue() / 1000;
	}

	/** To string */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:");
		sb.append(" len_tok_access=");
		sb.append(safe(tok_access).length());
		sb.append(" hash_tok_access=");
		sb.append(safe(tok_access).hashCode());
		sb.append(" len_tok_refresh=");
		sb.append(safe(tok_refresh).length());
		sb.append(" hash_tok_refresh=");
		sb.append(safe(tok_refresh).hashCode());
		sb.append(" age_s=").append(getAge());
		sb.append(" tokens_valid=").append(valid());
		return sb.toString();
	}
}
