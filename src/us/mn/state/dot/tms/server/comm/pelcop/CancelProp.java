/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Cancel control property.  This message is sent when switching away from a
 * monitor or logging out after PTZ on a camera.  Its purpose remains somewhat
 * of a mystery.
 *
 * @author Douglas Lau
 */
public class CancelProp extends PelcoPProp {

	/** Cancel request code */
	static public final int REQ_CODE = 0xB9;

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int cam = parseBCD4(rx_buf);
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 0)
			throw new ParsingException("CANCEL");
	}

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf) {
		format8(tx_buf, ACK);
	}

	/** Format the head of a packet */
	@Override
	public void formatHead(ByteBuffer tx_buf) {
		// no head for ACK
	}

	/** Format the tail of a packet */
	@Override
	public void formatTail(ByteBuffer tx_buf) {
		// no tail for ACK
	}
}
