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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Detector status property
 *
 * @author Douglas Lau
 */
public class StatusProperty extends TdcProperty {

	/** NOTE: Status flags are different for model TDC1-PIR */
	private final boolean tdc1_pir = false;

	/** Status value */
	protected int status = 0;

	/** Check if a bit flag is set */
	private boolean checkFlag(int bit) {
		return (status & (1 << bit)) != 0;
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(formatShort(CTRL_STATUS, c));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		byte[] buf = parseLong(is, c);
		if (buf == null)
			throw new ParsingException("Expected long frame");
		if (buf[0] != (CTRL_STATUS_RESP))
			throw new ParsingException("Wrong CTRL: " + buf[0]);
		if (buf.length != 3)
			throw new ParsingException("Wrong len: " + buf.length);
		parseStatus(buf);
	}

	/** Parse the status byte */
	protected void parseStatus(byte[] buf) {
		status = buf[2];
	}

	/** Get status faults */
	public final String getFaults() {
		ArrayList<String> faults = new ArrayList<String>();
		if (checkFlag(7))
			faults.add("HW");
		if (checkFlag(6))
			faults.add("sync");
		if (checkFlag(5))
			faults.add("queue");
		if (checkFlag(4))
			faults.add("wrong-way");
		if (checkFlag(3))
			faults.add("ultrasonic");
		if (tdc1_pir) {
			if (checkFlag(2))
				faults.add("low-voltage");
			if (checkFlag(1))
				faults.add("thermo");
			if (checkFlag(0))
				faults.add("IR");
		} else {
			if (checkFlag(2))
				faults.add("IR-2");
			if (checkFlag(1))
				faults.add("IR-1");
			if (checkFlag(0))
				faults.add("radar");
		}
		String f = String.join(";", faults);
		return (!f.isEmpty()) ? f : null;
	}

	/** Get status as a string */
	@Override
	public String toString() {
		String f = getFaults();
		return "status: " + ((f != null) ? f : "ok");
	}
}
