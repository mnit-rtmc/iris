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
package us.mn.state.dot.tms.server.comm.monstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * A property to configure a monitor.
 *
 * @author Douglas Lau
 */
public class ConfigProp extends ControllerProp {

	/** ASCII record separator */
	static private final char RECORD_SEP = 30;

	/** ASCII unit separator */
	static private final char UNIT_SEP = 31;

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		int pin = op.getController().getMaxPin();
		tx_buf.put(formatReq(pin).getBytes("UTF8"));
	}

	/** Format a config request */
	private String formatReq(int pin) {
		StringBuilder sb = new StringBuilder();
		sb.append("config");
		sb.append(UNIT_SEP);
		sb.append(pin);
		sb.append(RECORD_SEP);
		return sb.toString();
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "config";
	}
}
