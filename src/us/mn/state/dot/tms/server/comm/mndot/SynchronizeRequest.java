/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Synchronize Request
 *
 * @author Douglas Lau
 */
public class SynchronizeRequest extends Request {

	/** Format a basic "GET" request */
	protected byte[] formatPayloadGet(Message m) throws IOException {
		throw new ProtocolException("SYNCHRONIZE IS SET ONLY");
	}

	/** Get the expected number of octets in response to a GET request */
	protected int expectedGetOctets() {
		return 0;
	}

	/** Format a basic "SET" request */
	protected byte[] formatPayloadSet(Message m) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCD.OutputStream bcd = new BCD.OutputStream(os);
		Calendar now = Calendar.getInstance();
		bcd.write8Bit(now.get(Calendar.MONTH) + 1);
		bcd.write8Bit(now.get(Calendar.DAY_OF_MONTH));
		bcd.write8Bit(now.get(Calendar.YEAR) % 100);
		bcd.write8Bit(now.get(Calendar.HOUR_OF_DAY));
		bcd.write8Bit(now.get(Calendar.MINUTE));
		bcd.write8Bit(now.get(Calendar.SECOND));
		byte[] sync = os.toByteArray();
		byte[] req = new byte[9];
		req[OFF_DROP_CAT] = m.dropCat(SYNCHRONIZE_CLOCK);
		req[OFF_LENGTH] = (byte)sync.length;	// Always 6 octets
		System.arraycopy(sync, 0, req, OFF_PAYLOAD, sync.length);
		req[req.length - 1] = checksum(req);
		return req;
	}

	/** Get the expected number of octets in response to a SET request */
	protected int expectedSetOctets() {
		return 3;
	}
}
