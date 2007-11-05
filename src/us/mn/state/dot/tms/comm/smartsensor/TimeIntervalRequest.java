/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.comm.ParsingException;

/**
 * Time Interval Request
 *
 * @author Douglas Lau
 */
public class TimeIntervalRequest extends MemoryRequest {

	/** Create a new time interval request */
	public TimeIntervalRequest() {
		this(0);
	}

	/** Create a new time interval request */
	public TimeIntervalRequest(int i) {
		value = i;
	}

	/** Current value of time interval */
	int value;

	/** Get the SmartSensor memory buffer address */
	protected int memoryAddress() { return 0x00008E; }

	/** Get the SmartSensor memory buffer length */
	protected short memoryLength() { return 8; }

	/** Format the buffer to write to SmartSensor memory */
	protected String formatBuffer() { return hex(value, 8); }

	/** Set the response to the request */
	protected void setResponse(String r) throws IOException {
		super.setResponse(r);
		try { value = Integer.parseInt(r, 16); }
		catch(NumberFormatException e) {
			throw new ParsingException(
				"Invalid time interval: " + r);
		}
	}
}
