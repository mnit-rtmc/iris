/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
 * Copyright (C) 2016-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Simple text line reader.
 * This class should be used instead of BufferedReader.readLine whenever input
 * is untrusted, because if no line seperator is found, BufferedReader will
 * happily keep reading text until all memory is exhausted.
 *
 * @author Douglas Lau
 */

public class LineReader {

	/** Charset name for ASCII */
	static private final String ASCII = "US-ASCII";

	/** Test if a character is a line seperator */
	static protected boolean isLineSeperator(char c) {
		return c == '\r' || c == '\n';
	}

	/** Underlying reader */
	protected final Reader reader;

	/** Buffer to read text */
	protected final char[] buffer;

	/** Number of chars in buffer */
	protected int n_chars = 0;

	/** Seperator char for most recent line */
	protected char sep = 0;

	/** Create a new line reader.
	 * @param r Reader to read.
	 * @param max_chars Maximum number of characters on a line. */
	public LineReader(Reader r, int max_chars) {
		reader = r;
		buffer = new char[max_chars];
	}

	/** Create a new line reader.
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line. */
	public LineReader(InputStream is, int max_chars) throws IOException {
		this(new InputStreamReader(is, ASCII), max_chars);
	}

	/** Read a line of text */
	public String readLine() throws IOException {
		int eol = endOfLine();
		while(eol < 0 && n_chars < buffer.length) {
			int n = reader.read(buffer, n_chars, buffer.length -
				n_chars);
			if(n < 0) {
				if(n_chars > 0)
					return bufferedLine(n_chars);
				else
					return null;
			} else {
				n_chars += n;
				eol = endOfLine();
			}
		}
		if(eol >= 0)
			return bufferedLine(eol);
		else
			throw new IOException("LineReader buffer full");
	}

	/** Find the next end of line character */
	protected int endOfLine() {
		for(int i = crlf(0); i < n_chars; i++) {
			if(isLineSeperator(buffer[i]))
				return i;
		}
		return -1;
	}

	/** Get the next buffered line of text */
	protected String bufferedLine(int eol) {
		assert n_chars >= eol;
		int off = crlf(0);
		String line = new String(buffer, off, eol - off);
		eol = nextLine(eol);
		n_chars -= eol;
		if(n_chars > 0)
			System.arraycopy(buffer, eol, buffer, 0, n_chars);
		return line;
	}

	/** Get index to first character in next line */
	protected int nextLine(int pos) {
		if(n_chars > pos && isLineSeperator(buffer[pos])) {
			sep = buffer[pos];
			pos++;
			pos = crlf(pos);
			sep = buffer[pos - 1];
		}
		return pos;
	}

	/** Skip Windows-style line seperators */
	protected int crlf(int pos) {
		if(sep == '\r' && n_chars > pos && buffer[pos] == '\n')
			return pos + 1;
		else
			return pos;
	}
}
