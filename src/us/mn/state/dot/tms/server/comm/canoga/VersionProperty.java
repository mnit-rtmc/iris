/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Firmware Version Property
 *
 * @author Douglas Lau
 */
public class VersionProperty extends CanogaProperty {

	/** Message payload for a GET request */
	static protected final byte[] PAYLOAD_GET = { 'B' };

	/** Get the expected number of octets in response */
	protected int expectedResponseOctets() {
		return 18;
	}

	/** Format a basic "GET" request */
	protected byte[] formatPayloadGet() {
		return PAYLOAD_GET;
	}

	/** Format a basic "SET" request */
	protected byte[] formatPayloadSet() throws IOException {
		throw new ProtocolException("Firmware version is read-only");
	}

	/** Get the property name */
	@Override
	protected String getName() {
		return "Firmware Version";
	}

	/** Firmware version */
	protected String version;

	/** Set the requested value */
	protected void setValue(byte[] v) {
		version = getPayload(v);
	}

	/** Get the requested value */
	@Override
	public String getValue() {
		return version;
	}
}
