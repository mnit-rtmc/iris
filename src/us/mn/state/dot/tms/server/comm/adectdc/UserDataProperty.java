/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Property to get/set user data.
 *
 * @author Douglas Lau
 */
public class UserDataProperty extends TdcProperty {

	/** Cmd byte values */
	static public final byte CMD_MODE = 0x00;
	static public final byte CMD_STATUS = 0x01;
	static public final byte CMD_RESET_WRONG_WAY = 0x0E;
	static public final byte CMD_EXTRA = 0x0F;
	static public final byte CMD_GET_STATUS = 0x10; // JPEG
	static public final byte CMD_GET_DATA = 0x20; // JPEG
	static public final byte CMD_EXEC_COMMAND = 0x50; // JPEG
	static public final byte CMD_GET_LENGTH = 0x60; // JPEG

	/** Data byte values */
	static public final byte DATA_MODE_NORMAL = 0x00;
	static public final byte DATA_MODE_REVERSE = 0x01; // non-zero
	static public final byte DATA_EXTRA_STATIC = 0x01;
	static public final byte DATA_EXTRA_DYNAMIC = 0x02;
	static public final byte DATA_EXTRA_RESET_SYNC = 0x03;

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		// NOTE: the example in the protocol document uses FCB/FCV,
		//       but it also says they are only used
		//       "in conjunctin with requests for traffic data"
		byte ctrl = CTRL_FCB | CTRL_FCV | CTRL_USER;
		byte[] buf = new byte[2];
		buf[0] = CMD_MODE;
		buf[1] = DATA_MODE_NORMAL;
		os.write(formatLong(ctrl, c, buf));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseSingle(is);
	}

	/** Get user data as a string */
	@Override
	public String toString() {
		return "user data";
	}
}
