/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Flash Configuration Property.
 *
 * @author Douglas Lau
 */
public class FlashConfigProperty extends SS125Property {

	/** Delay time to wait for FLASH memory to be written */
	static private final int FLASH_WRITE_MS = 4000;

	/** Message ID for flash config request */
	protected MessageID msgId() {
		return MessageID.FLASH_CONFIG;
	}

	/** Format a STORE request */
	protected byte[] formatStore() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.WRITE);
		return body;
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		TimeSteward.sleep_well(FLASH_WRITE_MS);
		super.decodeStore(c, is);
	}

	/** Get a string representation of the property */
	public String toString() {
		return "flash_config";
	}
}
