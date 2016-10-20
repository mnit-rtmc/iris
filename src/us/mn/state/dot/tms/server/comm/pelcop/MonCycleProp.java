/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.nio.ByteBuffer;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Monitor cycle property.
 *
 * @author Douglas Lau
 */
public class MonCycleProp extends MonStatusProp {

	/** Monitor cycle request code */
	static public final int REQ_CODE = 0xC9;

	/** Create a new monitor cycle property */
	public MonCycleProp(boolean l) {
		super(l);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		if (parse8(rx_buf) != 3)
			throw new ParsingException("CYCLE");
		int dir = parse8(rx_buf);
		if (dir != 1 && dir != 2)
			throw new ParsingException("DIR");
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		int mon = (100 * mhi) + mlo;
		if (dir == 1)
			selectNextMonitor(mon);
		else
			selectPrevMonitor(mon);
	}

	/** Select next video monitor */
	private void selectNextMonitor(int mon) {
		setMonitor(VideoMonitorHelper.findNext(mon));
	}

	/** Select previous video monitor */
	private void selectPrevMonitor(int mon) {
		setMonitor(VideoMonitorHelper.findPrev(mon));
	}
}
