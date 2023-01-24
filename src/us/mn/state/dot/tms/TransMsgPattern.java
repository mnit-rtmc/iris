/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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

/**
 * Transient message pattern.
 *
 * @author Douglas Lau
 */
public class TransMsgPattern implements MsgPattern {

	/** Sign text MULTI string */
	private final String multi;

	/** Create a new transient msg pattern */
	public TransMsgPattern(String m) {
		multi = m;
	}

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return "trans_msg_pattern_" + multi;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		// do nothing
	}

	/** Get the hashtag for composing with the pattern.
	 * @return hashtag; null for no composing. */
	@Override
	public String getComposeHashtag() {
		assert(false);
		return null;
	}

	/** Set the hashtag for composing with the pattern.
	 * @param cht hashtag; null for no composing. */
	@Override
	public void setComposeHashtag(String cht) {
		// do nothing
	}

	/** Destroy the object */
	@Override
	public void destroy() {
		// do nothing
	}
}
