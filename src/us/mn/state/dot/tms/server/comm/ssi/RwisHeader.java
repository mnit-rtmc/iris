/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ssi;

import java.util.HashMap;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * SSI header record.
 *
 * @author Douglas Lau
 */
public class RwisHeader {

	/** Parse the fields of a line */
	static private String[] parseFields(String line) {
		String[] fs = line.split(",");
		for(int i = 0; i < fs.length; i++)
			fs[i] = fs[i].trim();
		return fs;
	}

	/** Raw header, as a line of CSV text */
	private final String raw_header;

	/** Field name to column mapping */
	private final HashMap<String, Integer> fields;

	/** Create a new RWIS header by parsing text that contains an ssi
	 * header record into fields.
	 * @param line A single text line (header).  The fields are: Siteid,
	 *             DtTm, AirTemp, Dewpoint, Rh, SpdAvg, SpdGust, DirMin,
	 *             DirAvg, DirMax, Pressure, PcIntens, PcType, PcRate,
	 *             PcAccum, Visibility.
	 * @throws ParsingException if header cannot be parsed.
	 */
	public RwisHeader(String line) throws ParsingException {
		raw_header = line;
		String[] fs = parseFields(line);
		if(fs.length < 1 || !fs[0].equals("Siteid"))
			throw new ParsingException("invalid header record");
		fields = new HashMap<String, Integer>();
		for(int i = 0; i < fs.length; i++)
			fields.put(fs[i], i);
	}

	/** Create an RWIS header containing only the Siteid */
	public RwisHeader() {
		raw_header = "Siteid";
		fields = new HashMap<String, Integer>();
		fields.put(raw_header, 0);
	}

	/** Get a named field from a record */
	public String getField(String[] rec, String name) {
		Integer col = fields.get(name);
		if(col != null && col < rec.length)
			return rec[col];
		else
			return null;
	}

	/** To string */
	public String toString() {
		return "(RwisHeader: " + raw_header + ")";
	}
}
