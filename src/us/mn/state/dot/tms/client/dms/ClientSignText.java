/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;

/**
 * Client-side sign text for message selector combo boxes.
 *
 * @author Douglas Lau
 */
public class ClientSignText implements SignText {

	/** Sign text MULTI string */
	private final String multi;

	/** Sign text line number */
	private final short line;

	/** Sign text rank */
	private final short rank;

	/** Create a new client sign text.
	 * @param m Multi string of sign text.
	 * @param ln Line number.
	 * @param r Message rank. */
	public ClientSignText(String m, short ln, short r) {
		multi = m;
		line = ln;
		rank = r;
	}

	/** Create a new client sign text.
	 * @param m Multi string of sign text. */
	public ClientSignText(String m) {
		this(m, (short) 0, (short) 0);
	}

	/** Get the string representation */
	@Override
	public String toString() {
		return multi;
	}

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return "client_sign_text_" + multi;
	}

	/** Check group membership */
	@Override
	public boolean isInGroup(String g) {
		return false;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		return null;
	}

	/** Set the line */
	@Override
	public void setLine(short l) {
		// do nothing
	}

	/** Get the line */
	@Override
	public short getLine() {
		return line;
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

	/** Set the rank */
	@Override
	public void setRank(short r) {
		// do nothing
	}

	/** Get the rank */
	@Override
	public short getRank() {
		return rank;
	}

	/** Destroy the object */
	@Override
	public void destroy() {
		// do nothing
	}
}
