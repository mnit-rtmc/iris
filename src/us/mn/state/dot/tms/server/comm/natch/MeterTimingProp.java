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
		StringBuilder sb = new StringBuilder();
		sb.append("MT,");
		sb.append(message_id);
		sb.append(',');
		sb.append(getEntryNumber());
		sb.append(',');
		sb.append(getMeterNumber());
		sb.append(',');
		sb.append(Integer.toString(start));
		sb.append(',');
		sb.append(Integer.toString(stop));
		sb.append(',');
		sb.append(Integer.toString(red));
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("MT,");
		sb.append(message_id);
		sb.append(',');
		sb.append(getEntryNumber());
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Parse received message */
	@Override
	protected boolean parseMsg(String msg) throws IOException {
		String[] param = msg.split(",");
		if (param.length == 7 &&
		    param[0].equals("mt") &&
		    param[1].equals(message_id) &&
		    param[2].equals(getEntryNumber()) &&
		    param[3].equals(Integer.toString(getMeterNumber())))
		{
			try {
				start = Integer.parseInt(param[4]);
				stop = Integer.parseInt(param[5]);
				red = Integer.parseInt(param[6]);
				return true;
			}
			catch (NumberFormatException e) { }
		}
		return false;
	}
}
