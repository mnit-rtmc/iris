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
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * General Configuration Property.
 *
 * @author Douglas Lau
 */
public class GeneralConfigProperty extends SS125Property {

	/** Message ID for general config request */
	protected MessageID msgId() {
		return MessageID.GENERAL_CONFIG;
	}

	/** Format a QUERY request */
	protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.READ);
		return body;
	}

	/** Format a STORE request */
	protected byte[] formatStore() throws IOException {
		byte[] body = new byte[87];
		formatBody(body, MessageType.WRITE);
		formatString(body, 3, 2, orientation);
		formatString(body, 5, 32, location);
		formatString(body, 37, 32, description);
		formatString(body, 69, 16, serialNumber);
		formatBool(body, 85, metric);
		return body;
	}

	/** Parse a QUERY response */
	protected void parseQuery(byte[] body) throws IOException {
		if(body.length != 87)
			throw new ParsingException("BODY LENGTH");
		orientation = parseString(body, 3, 2);
		location = parseString(body, 5, 32);
		description = parseString(body, 37, 32);
		serialNumber = parseString(body, 69, 16);
		metric = parseBool(body, 85);
	}

	/** Sensor orientation */
	protected String orientation = "";

	/** Get the sensor orientation */
	public String getOrientation()  {
		return orientation;
	}

	/** Sensor location */
	protected String location = "";

	/** Get the sensor location */
	public String getLocation() {
		return location;
	}

	/** Set the sensor location */
	public void setLocation(String loc) {
		location = loc;
	}

	/** Sensor description */
	protected String description = "";

	/** Get the sensor description */
	public String getDescription() {
		return description;
	}

	/** Sensor serial number */
	protected String serialNumber = "";

	/** Get the sensor serial number */
	public String getSerialNumber() {
		return serialNumber;
	}

	/** Metric flag */
	protected boolean metric = false;

	/** Get the metric flag */
	public boolean isMetric() {
		return metric;
	}

	/** Set the metric flag */
	public void setMetric(boolean m) {
		metric = m;
	}

	/** Get a string representation of the property */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("orientation:");
		sb.append(getOrientation());
		sb.append(",location:");
		sb.append(getLocation());
		sb.append(",description:");
		sb.append(getDescription());
		sb.append(",serial#:");
		sb.append(getSerialNumber());
		sb.append(",metric:");
		sb.append(isMetric());
		return sb.toString();
	}
}
