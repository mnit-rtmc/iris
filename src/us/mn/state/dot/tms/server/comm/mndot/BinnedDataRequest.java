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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Binned data request to query or clear oldest binned data record.
 *
 * @author Douglas Lau
 */
public class BinnedDataRequest extends Request {

	/** Number of octets for timestamp */
	static protected final int STAMP_LENGTH = 5;

	/** Number of octets for binned data record */
	static protected final int RECORD_LENGTH = 75;

	/** Number of octets for complete binned data buffer */
	static protected final int BINNED_DATA_LENGTH =
		STAMP_LENGTH + RECORD_LENGTH;

	/** Binned data buffer payload */
	protected final byte[] payload = new byte[BINNED_DATA_LENGTH];

	/** Get the binned record timestamp */
	public Calendar getStamp() throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(payload);
		BCD.InputStream bcd = new BCD.InputStream(bis);
		int year = 1900 + bcd.read8Bit();
		if(year < 1989)
			year += 100;
		int month = bcd.read8Bit() - 1;
		int day = bcd.read8Bit();
		int hour = bcd.read8Bit();
		int minute = bcd.read8Bit();
		Calendar stamp = Calendar.getInstance();
		stamp.set(year, month, day, hour, minute, 0);
		return stamp;
	}

	/** Get the binned data record */
	public byte[] getRecord() {
		byte[] rec = new byte[RECORD_LENGTH];
		System.arraycopy(payload, STAMP_LENGTH, rec, 0, RECORD_LENGTH);
		return rec;
	}

	/** Count of binned data records */
	protected int n_records;

	/** Get the remaining record count */
	public int getRecordCount() {
		return n_records;
	}

	/** Format a basic "GET" request */
	protected byte[] formatPayloadGet(Message m) throws IOException {
		byte[] req = new byte[3];
		req[OFF_DROP_CAT] = m.dropCat(SEND_NEXT_RECORD);
		req[OFF_LENGTH] = 0;
		req[req.length - 1] = checksum(req);
		return req;
	}

	/** Get the expected number of octets in response to a GET request */
	protected int expectedGetOctets() {
		return payload.length + 3;
	}

	/** Parse the response to a GET request */
	protected void parseGetResponse(byte[] buf) throws IOException {
		if(buf.length != payload.length)
			throw new ParsingException("Bad resp len:"+ buf.length);
		System.arraycopy(buf, OFF_PAYLOAD, payload, 0, payload.length);
	}

	/** Format a basic "SET" request */
	protected byte[] formatPayloadSet(Message m) throws IOException {
		byte[] req = new byte[3];
		req[OFF_DROP_CAT] = m.dropCat(DELETE_OLDEST_RECORD);
		req[OFF_LENGTH] = 0;
		req[req.length - 1] = checksum(req);
		return req;
	}

	/** Get the expected number of octets in response to a SET request */
	protected int expectedSetOctets() {
		return 4;
	}

	/** Parse the response to a SET request */
	protected void parseSetResponse(byte[] buf) {
		n_records = buf[OFF_PAYLOAD] & 0xFF;
	}
}
