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
import java.text.SimpleDateFormat;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Clock status property
 *
 * @author Douglas Lau
 */
public class ClockStatusProp extends NatchProp {

	/** Get CS / cs message */
	private String getMessage(String code) {
		return code + ',' + message_id + ',' +
			new SimpleDateFormat(RFC3339).format(new Date(stamp));
	}

	/** Time stamp */
	private long stamp;

	/** Create a new clock status property */
	public ClockStatusProp(Counter c) {
		super(c);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		stamp = TimeSteward.currentTimeMillis();
		String msg = getMessage("CS") + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "CS," + message_id + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "cs";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 3;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		stamp = parseStamp(param[2]);
		return true;
	}
}
