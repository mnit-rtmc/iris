/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
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
import java.io.LineNumberReader;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * SSI property.
 *
 * @author Michael Darter
 */
public class SsiProperty extends ControllerProperty {

	/** Perform a get request, read and parse reccords from file */
	public void doGetRequest(InputStream is) throws IOException {
		SsiPoller.log("called, input=" + is);
		if(is == null) {
			SsiPoller.log("no input stream to read");
			throw new EOFException();
		}
		InputStreamReader isr = new InputStreamReader(is, 
			"ISO-8859-1");
		LineNumberReader lnr = new LineNumberReader(isr);
		while(true) {
			String line = lnr.readLine();
			if(line == null)
				break;
			new RwisRec(line).store();
		}
		SsiPoller.log("done");
	}
}
