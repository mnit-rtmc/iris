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
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Detector configuration property
 *
 * @author Douglas Lau
 */
public class DetectorConfigProp extends NatchProp {

	/** Detector number (0-31) */
	private int detector_num;

	/** Advance to the next detector */
	public void nextDetector() {
		detector_num++;
	}

	/** Check if done */
	public boolean isDone() {
		return detector_num > 31;
	}

	/** Lookup the input pin for a detector */
	private int lookupDetectorPin() {
		if (detector_num >= 0 && detector_num < 32)
			return detector_num + 39;
		else
			return 0;
	}

	/** Create a new detector configuration property */
	public DetectorConfigProp(Counter c) {
		super(c);
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
		sb.append(lookupDetectorPin());
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
	protected boolean parseMsg(String msg) throws IOException {
		return msg.equals("dc," + message_id + ',' + detector_num +
			',' + lookupDetectorPin());
	}
}
