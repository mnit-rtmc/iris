/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A property to command the iris of a camera.
 *
 * @author Douglas Lau
 */
public class IrisProperty extends ManchesterProperty {

	/** Requested iris value [-1, 1] :: [close, open] */
	private final int iris;

	/** Create a new iris property */
	public IrisProperty(int i) {
		iris = i;
	}

	/** Encode an iris command packet */
	private byte[] encodeIrisPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (iris < 0)
			pkt[1] |= EX_IRIS_CLOSE;
		else
			pkt[1] |= EX_IRIS_OPEN;
		return pkt;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		drop--;		// receiver address is zero-relative
		os.write(encodeIrisPacket(drop));
	}
}
