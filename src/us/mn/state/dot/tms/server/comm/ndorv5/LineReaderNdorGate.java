/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2013	Minnesota Department of Transportation
 * Copyright (C) 2015-2021  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndorv5;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import us.mn.state.dot.tms.utils.LineReader;

/**
 *  This class is here to deal with the odd "<CR><LF>"
 *  (Literally those EIGHT characters) end-of-line
 *  marker that the NDOR v5 gate-controller sends.
 *  Derived from Doug Lau's LineReader class.
 *  
 * @author John L. Stanley - SRF Consulting
 */
public class LineReaderNdorGate extends LineReader {

	/** Create a new line reader.
	 * @param r Reader to read.
	 * @param max_chars Maximum number of characters on a line. */
	public LineReaderNdorGate(Reader r, int max_chars) {
		super(r, max_chars);
	}

	/** Create a new line reader.
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line. */
	public LineReaderNdorGate(InputStream is, int max_chars)
			throws IOException {
		super(is, max_chars);
	}

	/** Read a line of text */
	@Override
	public String readLine() throws IOException {
		int eol = endOfLine();
		while ((eol < 0) && (n_chars < buffer.length)) {
			int n = reader.read(buffer, n_chars,
			                    buffer.length - n_chars);
			if (n < 0) {
				if (n_chars > 0)
					return bufferedLine(n_chars);
				else
					return null;
			} else {
				n_chars += n;

				// process odd EOL marker 
				// from NDOR gate controller
				String s1 = new String(buffer, 0, n_chars);
				String s2 = s1.replace("<CR><LF>", "\n\r");
				if (!s1.equals(s2)) {
					n_chars = s2.length();
					System.arraycopy(s2.toCharArray(), 0,
					                 buffer, 0,
					                 n_chars);
				}
					
				eol = endOfLine();
			}
		}

		if (eol < 0)
			throw new IOException("LineReader buffer full");

		return bufferedLine(eol);
	}
}
