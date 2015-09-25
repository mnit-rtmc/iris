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
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Buffered tag transaction property.
 *
 * @author Douglas Lau
 */
public class BufferedTransactionProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Query command code */
	static private final int QUERY = 0x0007;

	/** Buffered transaction number */
	private final int n_trans;

	/** Tag Transaction */
	private TagTransaction transaction;

	/** Create a new buffered transaction property */
	public BufferedTransactionProp(int n) {
		n_trans = n;
	}

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Get the query packet data */
	@Override
	public byte[] queryData() {
		byte[] d = new byte[6];
		format16(d, 0, QUERY);
		format32(d, 2, n_trans);
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length < 8 || d.length > 64)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (parse32(d, 4) != n_trans)
			throw new ParsingException("TRANSACTION NUMBER");
		transaction = new TagTransaction(d, 8, d.length - 8);
	}

	/** Get the tag transaction */
	public TagTransaction getTransaction() {
		return transaction;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "buffered: " + n_trans + ' ' + transaction;
	}
}
