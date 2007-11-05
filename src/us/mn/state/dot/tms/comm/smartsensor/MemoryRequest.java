/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.smartsensor;

import java.io.IOException;
import java.io.PrintStream;

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

	/** Get the SmartSensor memory buffer address */
	abstract protected int memoryAddress();

	/** Get the SmartSensor memory buffer length */
	abstract protected short memoryLength();

	/** Format the buffer to write to SmartSensor memory */
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
			// NOTE: The SmartSensor needs 4 extra seconds to
			// respond (probably to update FLASH memory).
			try { Thread.sleep(4000); }
			catch(InterruptedException e) {}
		}
	}

	/** Set the response to the request */
	protected void setResponse(String r) throws IOException {
		if(is_set) {
			if(r.equals("Success"))
				return;
			else
				throw new SmartSensorError(
					"Error writing SmartSensor memory");
		}
	}
}
