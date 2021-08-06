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
import static us.mn.state.dot.tms.server.comm.MeterPoller.*;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * System attributes property
 *
 * @author Douglas Lau
 */
public class SystemAttributesProp extends NatchProp {

	/** Get SA / sa message */
	private String getMessage(String code) {
		return code + ',' + message_id + ',' + COMM_FAIL_THRESHOLD_MS +
			',' + STARTUP_GREEN + ',' + STARTUP_YELLOW + ',' +
			getGreenTime() + ',' + getYellowTime() + '\n';
	}

	/** Create a new system attributes property */
	public SystemAttributesProp(Counter c) {
		super(c);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		tx_buf.put(getMessage("SA").getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "SA," + message_id + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Parse received message */
	@Override
	protected boolean parseMsg(String msg) throws IOException {
		return msg.equals(getMessage("sa"));
	}
}
