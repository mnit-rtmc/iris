/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Binned data property to query or clear oldest binned data record.
 *
 * @author Douglas Lau
 */
public class BinnedDataProperty extends MndotProperty {

	/** Number of octets for timestamp */
	static private final int STAMP_LENGTH = 5;

	/** Number of octets for binned data record */
	static private final int RECORD_LENGTH = 75;

	/** Number of octets for complete binned data buffer */
	static private final int BINNED_DATA_LENGTH =
		STAMP_LENGTH + RECORD_LENGTH;

	/** Binned data buffer payload */
	private final byte[] payload = new byte[BINNED_DATA_LENGTH];

	/** Get the binned data record */
	public byte[] getRecord() {
		byte[] rec = new byte[RECORD_LENGTH];
		System.arraycopy(payload, STAMP_LENGTH, rec, 0, RECORD_LENGTH);
		return rec;
	}

	/** Count of binned data records */
	private int n_records;

	/** Get the remaining record count */
	public int getRecordCount() {
		return n_records;
	}

	/** Stamp at end of sample interval */
	private long stamp;

	/** Get timestamp at the end of sample interval */
	public long getStamp() {
		return stamp;
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] req = createRequest(c, CatCode.SEND_NEXT_RECORD, 0);
		calculateChecksum(req);
		os.write(req);
	}

	/** Parse a query response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	@Override
	protected void parseQuery(byte[] pkt) throws IOException {
		validateResponseLength(pkt, payload.length + 3);
		System.arraycopy(pkt, OFF_PAYLOAD, payload, 0, payload.length);
		parseStamp();
	}

	/** Parse timestamp at the end of sample interval */
	private void parseStamp() throws IOException {
		int year = 1900 + parseBCD2(payload, 0);
		if (year < 1989)
			year += 100;
		int month = parseBCD2(payload, 1) - 1;
		int day = parseBCD2(payload, 2);
		int hour = parseBCD2(payload, 3);
		int minute = parseBCD2(payload, 4);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute, 0);
		stamp = cal.getTimeInMillis();
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] req = createRequest(c, CatCode.DELETE_OLDEST_RECORD, 0);
		calculateChecksum(req);
		os.write(req);
	}

	/** Parse a store response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	@Override
	protected void parseStore(byte[] pkt) throws IOException {
		validateResponseLength(pkt, 4);
		n_records = pkt[OFF_PAYLOAD] & 0xFF;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("bin: ");
		sb.append(HexString.format(payload, ' '));
		return sb.toString();
	}
}
