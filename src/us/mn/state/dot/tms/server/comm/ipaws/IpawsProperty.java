/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.server.comm.ipaws;

import java.io.IOException;
import java.io.InputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Container for IPAWS alert property.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class IpawsProperty extends ControllerProperty {
	
	/** Feed name */
	private final String alertFeed;

	/** Create a new IPAWS property */
	public IpawsProperty(String afd) {
		alertFeed = afd;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
			throws IOException {
		IpawsReader.readIpaws(is);
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "alertFeed " + alertFeed;
	}
}
