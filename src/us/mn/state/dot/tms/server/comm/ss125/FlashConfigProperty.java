/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

	/** Message ID for flash config request */
	protected int msgId() {
		return MSG_ID_FLASH_CONFIG;
	}

	/** Format the body of a GET request */
	byte[] formatBodyGet() throws IOException {
		assert false;
		return null;
	}

	/** Format the body of a SET request */
	byte[] formatBodySet() throws IOException {
		byte[] body = new byte[3];
		format8(body, OFF_MSG_ID, msgId());
		format8(body, OFF_MSG_SUB_ID, msgSubId());
		format8(body, OFF_READ_WRITE, REQ_WRITE);
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
