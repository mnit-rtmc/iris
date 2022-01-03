/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
 * Firmware version property
 *
 * @author Douglas Lau
 */
public class FirmwareVersionProp extends NatchProp {

	/** Firmware version */
	private String version;

	/** Get the firmware version */
	public String getVersion() {
		return version;
	}

	/** Build time stamp */
	private long stamp;

	/** Create a new firmware version property */
	public FirmwareVersionProp(Counter c) {
		super(c);
		version = "";
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "V.," + message_id + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "v.";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 4;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		version = param[2];
		stamp = parseStamp(param[3]);
		return true;
	}
}
