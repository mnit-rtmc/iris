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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Synchronize Property
 *
 * @author Douglas Lau
 */
public class SynchronizeProperty extends MndotProperty {

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		Calendar stamp = TimeSteward.getCalendarInstance();
		byte[] pkt = createRequest(c, CatCode.SYNCHRONIZE_CLOCK, 6);
		formatBCD2(pkt, 2, stamp.get(Calendar.MONTH) + 1);
		formatBCD2(pkt, 3, stamp.get(Calendar.DAY_OF_MONTH));
		formatBCD2(pkt, 4, stamp.get(Calendar.YEAR) % 100);
		formatBCD2(pkt, 5, stamp.get(Calendar.HOUR_OF_DAY));
		formatBCD2(pkt, 6, stamp.get(Calendar.MINUTE));
		formatBCD2(pkt, 7, stamp.get(Calendar.SECOND));
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

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return CatCode.SYNCHRONIZE_CLOCK.toString();
	}
}
