/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * SSI property.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SsiProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 1024;

	/** Maximum number of records in RWIS file */
	static private final int MAX_RECORDS = 500;

	/** Mapping of site_id to most recent RWIS records */
	private final HashMap<String, RwisRec> records;

	/** Create a new SSI property */
	public SsiProperty(HashMap<String, RwisRec> recs) {
		records = recs;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		if (is == null)
			throw new EOFException();
		LineReader lr = new LineReader(is, MAX_RESP);
		RwisHeader header = readHeader(lr);
		String line = lr.readLine();
		for (int i = 0; line != null && i < MAX_RECORDS; i++) {
			SsiPoller.slog("parsing " + line);
			RwisRec rec = new RwisRec(line, header);
			SsiPoller.slog("parsed rec=" + rec);
			String site_id = rec.getSiteId();
			// Only save record if site_id is in mapping
			if (site_id != null && records.containsKey(site_id))
				records.put(site_id, rec);
			line = lr.readLine();
		}
	}

	/** Read the header */
	private RwisHeader readHeader(LineReader lr) throws IOException {
		String line = lr.readLine();
		if (line != null)
			return new RwisHeader(line);
		else
			return null;
	}
}
