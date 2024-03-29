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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Detector configuration property
 *
 * @author Douglas Lau
 */
public class DetectorConfigProp extends DetectorProp {

	/** Input pin */
	private int pin;

	/** Create a new detector configuration property */
	public DetectorConfigProp(Counter c, int dn) {
		super(c, dn);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		pin = lookupPin(op.getController());
		String msg = "DC," + message_id + ',' + detector_num + ',' +
			pin + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "DC," + message_id + ',' + detector_num + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "dc";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 4;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		if (detector_num == parseInt(param[2])) {
			pin = parseInt(param[3]);
			return true;
		} else
			return false;
	}

	/** Check if pin is correct for an operation */
	public boolean isPinCorrect(Operation op) {
		return pin == lookupPin(op.getController());
	}
}
