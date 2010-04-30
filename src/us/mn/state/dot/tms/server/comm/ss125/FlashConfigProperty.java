/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;

/**
 * Flash Configuration Property.
 *
 * @author Douglas Lau
 */
public class FlashConfigProperty extends SS125Property {

	/** Flash config request ID */
	static protected final byte MSG_ID = 0x08;

	/** Format the body of a GET request */
	byte[] formatBodyGet() throws IOException {
		assert false;
		return null;
	}

	/** Format the body of a SET request */
	byte[] formatBodySet() throws IOException {
		byte[] body = new byte[3];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_WRITE;
		return body;
	}

	/** Parse the payload of a GET response */
	void parsePayload(byte[] body) throws IOException {
		assert false;
	}

	/** Delay before checking for response */
	void delayResponse() {
		// NOTE: wait 4 extra seconds for flash memory update
		try {
			Thread.sleep(4000);
		}
		catch(InterruptedException e) {
			// not sleepy?
		}
	}
}
