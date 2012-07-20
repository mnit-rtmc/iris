/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * SSI property.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SsiProperty extends ControllerProperty {

	/** Size of buffer for line reader */
	static private final int BUFFER_SZ = 1024;

	/** Maximum number of records in RWIS file */
	static private final int MAX_RECORDS = 500;

	/** Mapping of site_id to most recent RWIS records */
	private final HashMap<String, RwisRec> records;

	/** Create a new SSI property */
	public SsiProperty(HashMap<String, RwisRec> recs) {
		records = recs;
	}

	/** Perform a get request, read and parse reccords from file */
	public void doGetRequest(InputStream is) throws IOException {
		if(is == null) {
			SsiPoller.log("no input stream to read");
			throw new EOFException();
		}
		InputStreamReader isr = new InputStreamReader(is, "US-ASCII");
		LineReader lr = new LineReader(isr, BUFFER_SZ);
		RwisHeader header = readHeader(lr);
		String line = lr.readLine();
		for(int i = 0; line != null && i < MAX_RECORDS; i++) {
			SsiPoller.log("parsing " + line);
			RwisRec rec = new RwisRec(line, header);
			SsiPoller.log("parsed rec=" + rec);
			String site_id = rec.getSiteId();
			// Only save record if site_id is in mapping
			if(site_id != null && records.containsKey(site_id))
				records.put(site_id, rec);
			line = lr.readLine();
		}
	}

	/** Read the header */
	private RwisHeader readHeader(LineReader lr) throws IOException {
		String line = lr.readLine();
		if(line != null)
			return new RwisHeader(line);
		else
			return null;
	}
}
