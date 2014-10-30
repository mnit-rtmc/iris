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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Synchronize Property
 *
 * @author Douglas Lau
 */
public class SynchronizeProperty extends MndotProperty {

	/** Format a buffer with a time stamp.
	 * @param stamp Time stamp.
	 * @return Buffer of 6 BCD-encoded bytes.
	 * @throws IOException on error encoding to BCD. */
	static private byte[] formatStamp(Calendar stamp) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCDOutputStream bcd = new BCDOutputStream(os);
		bcd.write2(stamp.get(Calendar.MONTH) + 1);
		bcd.write2(stamp.get(Calendar.DAY_OF_MONTH));
		bcd.write2(stamp.get(Calendar.YEAR) % 100);
		bcd.write2(stamp.get(Calendar.HOUR_OF_DAY));
		bcd.write2(stamp.get(Calendar.MINUTE));
		bcd.write2(stamp.get(Calendar.SECOND));
		return os.toByteArray();
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] sync = formatStamp(TimeSteward.getCalendarInstance());
		byte[] pkt = createRequest(c, CatCode.SYNCHRONIZE_CLOCK,
			sync.length);
		System.arraycopy(sync, 0, pkt, OFF_PAYLOAD, sync.length);
		calculateChecksum(pkt);
		os.write(pkt);
	}

	/** Parse a store response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	@Override
	protected void parseStore(byte[] pkt) throws IOException {
		validateResponseLength(pkt, 3);
	}
}
