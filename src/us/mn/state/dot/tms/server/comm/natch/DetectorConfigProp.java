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

	/** Create a new detector configuration property */
	public DetectorConfigProp(Counter c, int dn) {
		super(c, dn);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DC,");
		sb.append(message_id);
		sb.append(',');
		sb.append(detector_num);
		sb.append(',');
		sb.append(lookupPin(op.getController()));
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DC,");
		sb.append(message_id);
		sb.append(',');
		sb.append(detector_num);
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Parse received message */
	@Override
	protected boolean parseMsg(Operation op, String msg)
		throws IOException
	{
		return msg.equals("dc," + message_id + ',' + detector_num +
			',' + lookupPin(op.getController()));
	}
}
