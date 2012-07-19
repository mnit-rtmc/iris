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

	/** Perform a get request, read and parse reccords from file */
	public void doGetRequest(InputStream is) throws IOException {
		if(is == null) {
			SsiPoller.log("no input stream to read");
			throw new EOFException();
		}
		InputStreamReader isr = new InputStreamReader(is, "US-ASCII");
		LineReader lr = new LineReader(isr, BUFFER_SZ);
		String line = lr.readLine();
		while(line != null) {
			SsiPoller.log("parsing " + line);
			new RwisRec(line).store();
			line = lr.readLine();
		}
	}
}
