/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.IOException;
import java.io.PrintStream;
import us.mn.state.dot.tms.server.comm.ControllerException;

/**
 * Memory Request
 *
 * @author Douglas Lau
 */
abstract public class MemoryRequest extends Request {

	/** Is this a SET request */
	protected boolean is_set = false;

	/** Check if the request has a checksum */
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
		return "S" + hex(memoryAddress(), 6) + hex(memoryLength(), 4);
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

	/** Poll the sensor */
	protected void doPoll(PrintStream ps, String h, String r)
		throws IOException
	{
		super.doPoll(ps, h, r);
		if(is_set) {
			// NOTE: The SS105 needs 4 extra seconds to
			// respond (probably to update FLASH memory).
			try {
				Thread.sleep(4000);
			}
			catch(InterruptedException e) {
				// not sleepy?
			}
		}
	}

	/** Set the response to the request */
	protected void setResponse(String r) throws IOException {
		if(is_set) {
			if(r.equals("Success"))
				return;
			else
				throw new ControllerException(
					"Error writing SS105 memory");
		}
	}
}
