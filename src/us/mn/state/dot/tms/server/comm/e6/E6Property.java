/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * E6 property.
 *
 * @author Douglas Lau
 */
abstract public class E6Property extends ControllerProperty {

	/** Get the query command */
	public Command queryCmd() throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Get the store command */
	public Command storeCmd() throws IOException {
		throw new ProtocolException("STORE not supported");
	}

	/** Get the query packet data */
	abstract public byte[] queryData();

	/** Parse a received packet */
	abstract public void parse(byte[] data) throws IOException;
}
