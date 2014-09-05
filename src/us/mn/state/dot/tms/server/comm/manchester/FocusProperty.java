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
 * A property to focus a camera.
 *
 * @author Douglas Lau
 */
public class FocusProperty extends ManchesterProperty {

	/** Requested focus value [-1, 1] :: [near, far] */
	private final int focus;

	/** Create a new focus property */
	public FocusProperty(int f) {
		focus = f;
	}

	/** Encode a focus command packet */
	private byte[] encodeFocusPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (focus < 0)
			pkt[1] |= EX_FOCUS_NEAR;
		else
			pkt[1] |= EX_FOCUS_FAR;
		return pkt;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		drop--;		// receiver address is zero-relative
		os.write(encodeFocusPacket(drop));
	}
}
