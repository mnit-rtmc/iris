/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005  Minnesota Department of Transportation
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

/**
 * Firmware Version Request
 *
 * @author Douglas Lau
 */
public class VersionRequest extends Request {

	/** Firmware version */
	protected String version;

	/** Check if the request has a checksum */
	protected boolean hasChecksum() {
		return false;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		return "S5";
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() throws IOException {
		throw new SmartSensorError("Firmware version is read-only");
	}

	/** Set the response to the request */
	protected void setResponse(String r) {
		version = r;
	}

	/** Get the sensor firmware version */
	public String getVersion() {
		return version;
	}
}
