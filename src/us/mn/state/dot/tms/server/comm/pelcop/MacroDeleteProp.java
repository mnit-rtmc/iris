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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Macro delete property.  This message deletes the selected macro.
 *
 * @author Douglas Lau
 */
public class MacroDeleteProp extends MonStatusProp {

	/** Macro delete request code */
	static public final int REQ_CODE = 0xE8;

	/** Create a new macro delete property */
	public MacroDeleteProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		setMonNumber((100 * mhi) + mlo);
		VideoMonitorImpl vm = findVideoMonitor();
		if (vm != null && vm.getPlayList() != null)
			vm.setPlayList(null);
		else
			setErrMsg(ErrorMsg.MacNotPresent);
	}
}
