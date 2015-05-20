/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerException;

/**
 * Memory Property
 *
 * @author Douglas Lau
 */
abstract public class MemoryProperty extends SS105Property {

	/** Delay time to wait for FLASH memory to be written */
	static private final int FLASH_WRITE_MS = 4000;

	/** Is this a SET request */
	protected boolean is_set = false;

	/** Check if the property has a checksum */
	protected boolean hasChecksum() {
		return !is_set;
	}

	/** Get the SS105 memory buffer address */
	abstract protected int memoryAddress();

	/** Get the SS105 memory buffer length */
	abstract protected short memoryLength();

	/** Format the buffer to write to SS105 memory */
	abstract protected String formatBuffer();

	/** Format a basic memory request */
	protected String formatRequest() {
		return "S" + HexString.format(memoryAddress(), 6) +
		       HexString.format(memoryLength(), 4);
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		is_set = false;
		return "SJ" + formatRequest();
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() {
		is_set = true;
		String payload = formatRequest() + formatBuffer();
		String hexsum = checksum(payload);
		return "SK" + payload + hexsum;
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		TimeSteward.sleep_well(FLASH_WRITE_MS);
		super.decodeStore(c, is);
	}

	/** Parse the response to a STORE */
	protected void parseStore(String res) throws IOException {
		if(!res.equals("Success")) {
			throw new ControllerException(
				"Error writing SS105 memory");
		}
	}
}
