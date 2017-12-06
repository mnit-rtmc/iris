/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Monitor cycle property.  This message is sent to cycle forward / reverse
 * through the monitor list.
 *
 * @author Douglas Lau
 */
public class MonCycleProp extends MonStatusProp {

	/** Monitor cycle request code */
	static public final int REQ_CODE = 0xC9;

	/** Sub-request code for alternate camera */
	static private final int SUB_ALT_CAM = 2;

	/** Sub-request code for cycle monitor */
	static private final int SUB_CYCLE_MON = 3;

	/** Cycle dir codes */
	static private final int DIR_NEXT = 1;
	static private final int DIR_PREV = 2;

	/** Create a new monitor cycle property */
	public MonCycleProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int sub = parse8(rx_buf);
		if (sub == SUB_ALT_CAM) {
			setErrMsg(ErrorMsg.NoAltCam);
			return;
		}
		if (sub != SUB_CYCLE_MON)
			throw new ParsingException("SUB");
		int dir = parse8(rx_buf);
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		int mon = (100 * mhi) + mlo;
		switch (dir) {
		case DIR_NEXT:
			selectNextMonitor(mon);
			break;
		case DIR_PREV:
			selectPrevMonitor(mon);
			break;
		default:
			throw new ParsingException("CYCLE DIR");
		}
	}

	/** Select next video monitor */
	private void selectNextMonitor(int mon) {
		setMonNumber(VideoMonitorHelper.findNext(mon));
	}

	/** Select previous video monitor */
	private void selectPrevMonitor(int mon) {
		setMonNumber(VideoMonitorHelper.findPrev(mon));
	}
}
