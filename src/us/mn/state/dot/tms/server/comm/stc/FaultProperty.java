/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Fault property reads the controller faults, errors and alerts.
 *
 * @author Douglas Lau
 */
public class FaultProperty extends STCProperty {

	/** Byte offsets from beginning of fault response */
	static private final int OFF_COUNT = 1;
	static private final int OFF_FAULT = 3;

	/** Fault codes */
	private FaultCode[] faults;

	/** Create a new fault property */
	public FaultProperty(String pw) {
		super(pw);
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 'F';
		os.write(formatRequest(c.getDrop(), data));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse a received message */
	@Override
	protected void parseMessage(byte[] msg, int len)
		throws IOException
	{
		if (msg[0] != 'F') {
			super.parseMessage(msg, len);
			return;
		}
		if (len < 3)
			throw new ParsingException("TOO FEW BYTES:" + len);
		int n_faults = parseAsciiHex2(msg, OFF_COUNT);
		FaultCode[] fc = new FaultCode[n_faults];
		int j = OFF_FAULT;
		// NOTE: there is no reliable method to parse these codes,
		//       since they may be 2 or 3 digits
		for (int i = 0; i < n_faults; i++) {
			// check for a 2-digit fault code
			if (j + 2 <= msg.length) {
				int v = parseAsciiHex2(msg, j);
				FaultCode c = FaultCode.fromValue(v);
				if (c != null) {
					fc[i] = c;
					j += 2;
					continue;
				}
			}
			// check for a 3-digit fault code
			if (j + 3 <= msg.length) {
				int v = parseAsciiHex3(msg, j);
				FaultCode c = FaultCode.fromValue(v);
				if (c != null) {
					fc[i] = c;
					j += 3;
					continue;
				}
			}
			fc[i] = FaultCode.FAL_UNKNOWN;
		}
		faults = fc;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "Faults: " + getFaults();
	}

	/** Get the faults */
	public String getFaults() {
		if (faults != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < faults.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(faults[i].toString());
			}
			return sb.toString();
		} else
			return null;
	}
}
