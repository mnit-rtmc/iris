/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A message decoder provides a Java API for decoding messages from the SONAR
 * wire protocol.
 *
 * @author Douglas Lau
 */
public class MessageDecoder {

	/** Everything on the wire is encoded to UTF-8 */
	static private final Charset UTF8 = Charset.forName("UTF-8");

	/** Byte buffer to store incoming SONAR data */
	private final ByteBuffer app_in;

	/** Byte buffer input stream */
	private final ByteBufferInputStream in_buf;

	/** Char reader input stream */
	private InputStreamReader reader;

	/** String builder to build decoded parameters */
	private final StringBuilder m_buf = new StringBuilder();

	/** List of decoded parameters */
	private ArrayList<String> params = new ArrayList<String>();

	/** Create a new SONAR message decoder */
	public MessageDecoder(ByteBuffer in) throws IOException {
		app_in = in;
		in_buf = new ByteBufferInputStream(in);
		reader = new InputStreamReader(in_buf, UTF8);
	}

	/** Complete the current parameter */
	private void completeParameter() {
		params.add(m_buf.toString());
		m_buf.setLength(0);
	}

	/** Decode messages */
	public List<String> decode() throws IOException {
		try {
			app_in.flip();
			return _decode();
		}
		finally {
			app_in.compact();
		}
	}

	/** Decode messages */
	private List<String> _decode() throws IOException {
		while (reader.ready()) {
			int ch = reader.read();
			if (ch < 0)
				break;
			char c = (char) ch;
			if (Message.RECORD_SEP.code == c) {
				completeParameter();
				List<String> p = params;
				params = new ArrayList<String>();
				return p;
			} else if (Message.UNIT_SEP.code == c)
				completeParameter();
			else
				m_buf.append(c);
		}
		return null;
	}

	/** Debug the SONAR parameters */
	public void debugParameters() {
		StringBuilder b = new StringBuilder();
		for (String s: params) {
			b.append(s);
			b.append(' ');
		}
		System.err.println(b.toString());
	}
}
