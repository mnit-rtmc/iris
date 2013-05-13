/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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
 * Incident impact for one mainline lane (or shoulder).
 *
 * @author Douglas Lau
 */
public enum IncidentImpact {
	FREE_FLOWING('.'),
	PARTIALLY_BLOCKED('?'),
	BLOCKED('!');

	/** Character for encoding impact */
	public final char _char;

	/** Create a new incident impact */
	private IncidentImpact(char c) {
		_char = c;
	}

	/** Lookup an incident impact from a character code */
	static public IncidentImpact fromChar(char c) {
		for(IncidentImpact v: IncidentImpact.values()) {
			if(v._char == c)
				return v;
		}
		return null;
	}

	/** Create an array of incident impacts from a coded string.
	 * @param im Coded string of incident impact by lane.
	 * @return Array of incident impact values, one per lane. */
	static public IncidentImpact[] fromString(String im) {
		IncidentImpact[] imp = new IncidentImpact[im.length()];
		for(int i = 0; i < imp.length; i++) {
			IncidentImpact v = fromChar(im.charAt(i));
			imp[i] = v != null ? v : FREE_FLOWING;
		}
		return imp;
	}

	/** Create a coded impact string from an array.
	 * @param Array of incident impact values, one per lane.
	 * @return Coded string of incident impact by lane. */
	static public String fromArray(IncidentImpact[] imp) {
		StringBuilder sb = new StringBuilder();
		for(IncidentImpact v: imp)
			sb.append(v != null ? v._char : FREE_FLOWING._char);
		return sb.toString();
	}
}
