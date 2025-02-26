/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import java.io.InputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Container for a CAP property.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class CapProperty extends ControllerProperty {

	/** Feed name */
	private final String alertFeed;

	/** Create a new CAP property */
	public CapProperty(String afd) {
		alertFeed = afd;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		CapXmlReader reader = new CapXmlReader(is);
		reader.parse();
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "alertFeed " + alertFeed;
	}
}
