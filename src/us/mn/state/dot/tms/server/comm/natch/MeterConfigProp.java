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
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Meter configuration property
 *
 * @author Douglas Lau
 */
public class MeterConfigProp extends MeterProp {

	/** Get the meter heads */
	private String getHeads() {
		RampMeterType ty = RampMeterType.fromOrdinal(
			meter.getMeterType());
		return meter.isActive() ? Integer.toString(ty.lanes) : "0";
	}

	/** Get the release code */
	private String getRelease() {
		switch (RampMeterType.fromOrdinal(meter.getMeterType())) {
		case DUAL_SIMULTANEOUS: return "1";
		default: return "0";
		}
	}

	/** Get the pin number */
	private String getPin() {
		return Integer.toString(meter.getPin());
	}

	/** Get the meter head output pins */
	private String getOutputPins() {
		switch (meter.getPin()) {
		// 334 cabinets use these outputs for first meter
		case 2: return "4,5,6,7,8,9";
		// 334-Z style cabinets can have two meters
		case 3: return "12,13,15,16,17,18";
		// Pin must be 2 or 3
		default: return "0,0,0,0,0,0";
		}
	}

	/** Create a new meter configuration property */
	public MeterConfigProp(Counter c, RampMeterImpl m) {
		super(c, m);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MC," + message_id + ',' + getMeterNumber() + ',' +
			getHeads() + ',' + getRelease() + ',' + getPin() + ',' +
			getOutputPins() + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "MC," + message_id + ',' + getMeterNumber() + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "mc";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 12;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		return param[2].equals(Integer.toString(getMeterNumber()));
	}
}
