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
import java.text.SimpleDateFormat;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Date / time property.  This message sends the date / time to the keyboard.
 *
 * @author Douglas Lau
 */
public class DateTimeProp extends PelcoPProp {

	/** Date / time response code */
	static public final int REQ_CODE = 0xCD;

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf) {
		format8(tx_buf, REQ_CODE);
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		tx_buf.put(df.format(TimeSteward.getDateInstance()).getBytes());
	}
}
