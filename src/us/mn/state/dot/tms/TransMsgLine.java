/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2026  Minnesota Department of Transportation
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
 * Transient message line implementation.
 *
 * @author Douglas Lau
 */
public class TransMsgLine implements MsgLine {

	/** Msg line MULTI string */
	private final String multi;

	/** Msg line number */
	private final short line;

	/** Msg line rank */
	private final short rank;

	/** Create a new transient message line.
	 * @param m Multi string.
	 * @param ln Line number.
	 * @param r Message rank. */
	public TransMsgLine(String m, short ln, short r) {
		multi = m;
		line = ln;
		rank = r;
	}

	/** Create a new transient message line.
	 * @param m Multi string of message line. */
	public TransMsgLine(String m) {
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
		return "ml_" + multi;
	}

	/** Get notes (including hashtags) */
	@Override
	public String getNotes() {
		return null;
	}

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return null;
	}

	/** Set the line number */
	@Override
	public void setLine(short l) {
		// do nothing
	}

	/** Get the line number*/
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
