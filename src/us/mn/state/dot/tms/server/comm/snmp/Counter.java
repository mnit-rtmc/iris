/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Counter from RFC1155-SMI.
 *
 * @author Douglas Lau
 */
public class Counter extends ASN1Integer {

	/** Create a new counter */
	public Counter(MIBNode n) {
		super(n);
	}

	/** Encode a counter */
	@Override
	public void encode(BER er) throws IOException {
		er.encodeCounter(getInteger());
	}

	/** Decode a counter */
	@Override
	public void decode(InputStream is, BER er) throws IOException {
		setInteger(er.decodeCounter(is));
	}
}
