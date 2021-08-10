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
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Meter status property
 *
 * @author Douglas Lau
 */
public class MeterStatusProp extends MeterProp {

	/** Red dwell time */
	private int red;

	/** Set the red dwell time */
	public void setRed(int r) {
		red = r;
	}

	/** Get the red dwell time */
	public int getRed() {
		return red;
	}

	/** Create a new meter status property */
	public MeterStatusProp(Counter c, RampMeterImpl m) {
		super(c, m);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MS," + message_id + ',' + getMeterNumber() + ',' +
			red + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MS," + message_id + ',' + getMeterNumber() + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "ms";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 4;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		if (param[2].equals(Integer.toString(getMeterNumber()))) {
			red = parseInt(param[3]);
			return red >= 0;
		} else
			return false;
	}
}
