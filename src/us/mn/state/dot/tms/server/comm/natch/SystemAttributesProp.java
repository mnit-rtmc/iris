/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.RampMeterImpl.COMM_LOSS_THRESHOLD;
import static us.mn.state.dot.tms.server.comm.MeterPoller.*;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * System attributes property
 *
 * @author Douglas Lau
 */
public class SystemAttributesProp extends NatchProp {

	/** Create a new system attributes property */
	public SystemAttributesProp(Counter c) {
		super(c);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		long loss_ds = COMM_LOSS_THRESHOLD.round(DECISECONDS);
		String msg = "SA," + message_id + ',' + loss_ds + ',' +
			STARTUP_GREEN + ',' + STARTUP_YELLOW + ',' +
			getGreenTime() + ',' + getYellowTime() + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "SA," + message_id + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "sa";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 7;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		// not implemented
		return true;
	}
}
