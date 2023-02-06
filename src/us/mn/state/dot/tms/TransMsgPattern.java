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

	/** Sign configuration */
	private final SignConfig config;

	/** Sign text MULTI string */
	private final String multi;

	/** Create a new transient msg pattern */
	public TransMsgPattern(SignConfig sc, String m) {
		config = sc;
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

	/** Set the sign config */
	@Override
	public void setSignConfig(SignConfig sc) {
		// do nothing
	}

	/** Get the sign config */
	@Override
	public SignConfig getSignConfig() {
		return config;
	}

	/** Set the sign group */
	@Override
	public void setSignGroup(SignGroup sg) {
		// do nothing
	}

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		assert(false);
		return null;
	}

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		// do nothing
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Destroy the object */
	@Override
	public void destroy() {
		// do nothing
	}
}
