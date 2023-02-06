/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
 * Copyright (C) 2023  SRF Consulting Group
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
 * Temporary copy of a MsgPattern with extra
 * processing to assure the SignConfig and
 * message string will be non-null.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class TransMsgPattern {

	/** Sign configuration */
	private final SignConfig config;

	/** Sign text MULTI string */
	private final String multi;

	/** Create a new TransMsgPattern */
	private TransMsgPattern(SignConfig sc, String ms) {
		config = sc;
		multi = ms;
	}
	
	/** Generate a new TransMsgPattern.
	 * If either parameter is null, returns null.
	 * Otherwise, returns a new TransMsgPattern.
	 */
	static public TransMsgPattern generate(SignConfig sc, String ms) {
		if ((sc == null) || (ms == null))
			return null;
		return new TransMsgPattern(sc, ms);
	}

	/** Get the sign config */
	public SignConfig getSignConfig() {
		return config;
	}

	/** Get the MULTI string */
	public String getMulti() {
		return multi;
	}
}
