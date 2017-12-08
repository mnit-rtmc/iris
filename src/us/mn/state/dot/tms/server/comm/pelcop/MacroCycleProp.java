/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Macro cycle property.  This message cycles the selected macro.
 *
 * @author Douglas Lau
 */
public class MacroCycleProp extends MonStatusProp {

	/** Macro cycle request code */
	static public final int REQ_CODE = 0xEB;

	/** Cycle dir codes */
	static private final int DIR_NEXT = 1;
	static private final int DIR_PREV = 2;

	/** Create a new macro cycle property */
	public MacroCycleProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int dir = parse8(rx_buf);
		switch (dir) {
		case DIR_NEXT:
			if (nextPlayList())
				return;
			break;
		case DIR_PREV:
			if (prevPlayList())
				return;
			break;
		default:
			throw new ParsingException("CYCLE DIR");
		}
		setErrMsg(ErrorMsg.MacNotPresent);
	}

	/** Go to the next item in play list */
	private boolean nextPlayList() {
		VideoMonitorImpl vm = findVideoMonitor();
		if (vm != null)
			vm.nextPlayList();
		return vm != null;
	}

	/** Go to the previous item in play list */
	private boolean prevPlayList() {
		VideoMonitorImpl vm = findVideoMonitor();
		if (vm != null)
			vm.prevPlayList();
		return vm != null;
	}
}
