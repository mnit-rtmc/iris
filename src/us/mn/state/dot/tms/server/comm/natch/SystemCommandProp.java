/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * System command property
 *
 * @author Douglas Lau
 */
public class SystemCommandProp extends NatchProp {

	/** Create a new system command property */
	public SystemCommandProp(Counter c) {
		super(c);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "SC," + message_id + ",restart\n";
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Parse received message */
	@Override
	protected boolean parseMsg(String msg) throws IOException {
		return msg.equals("sc," + message_id + ",restart");
	}
}
