/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Meter timing table property
 *
 * @author Douglas Lau
 */
public class MeterTimingProp extends MeterProp {

	/** Table entry number (0-15) */
	private int entry;

	/** Set the meter entry number */
	public void setEntry(int ent) {
		if (ent >= 0 && ent <= 4) {
			int mn = getMeterNumber();
			if (mn >= 0 && mn <= 4)
				entry = (mn * 4) + ent;
		}
	}

	/** Get the table entry number */
	private String getEntryNumber() {
		return Integer.toString(entry);
	}

	/** Start time (minute of day) */
	private int start;

	/** Set the start time */
	public void setStart(int st) {
		start = st;
	}

	/** Stop time (minute of day) */
	private int stop;

	/** Set the stop time */
	public void setStop(int st) {
		stop = st;
	}

	/** Red time (tenths of a sec) */
	private int red;

	/** Set the red time */
	public void setRed(int rd) {
		red = rd;
	}

	/** Create a new meter timing table property */
	public MeterTimingProp(Counter c, RampMeterImpl m) {
		super(c, m);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MT," + message_id + ',' + getEntryNumber() + ',' +
			getMeterNumber() + ',' + start + ',' + stop + ',' +
			red + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MT," + message_id + ',' + getEntryNumber() + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "mt";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 7;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		if (param[2].equals(getEntryNumber()) &&
		    param[3].equals(Integer.toString(getMeterNumber())))
		{
			start = parseInt(param[4]);
			stop = parseInt(param[5]);
			red = parseInt(param[6]);
			return true;
		} else
			return false;
	}
}
