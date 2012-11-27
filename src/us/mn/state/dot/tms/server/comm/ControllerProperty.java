/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A controller property is one element of a CommMessage.  It represents a
 * property which can be queried from or stored to a controller.
 *
 * @author Douglas Lau
 */
abstract public class ControllerProperty {

	/** Get the path for a property */
	public String getPath() {
		return "";
	}

	/** Encode a QUERY request */
	public void encodeQuery(OutputStream os, int drop) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Decode a QUERY response */
	public void decodeQuery(InputStream is, int drop) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Encode a STORE request */
	public void encodeStore(OutputStream os, int drop) throws IOException {
		throw new ProtocolException("STORE not supported");
	}

	/** Decode a STORE response */
	public void decodeStore(InputStream is, int drop) throws IOException {
		throw new ProtocolException("STORE not supported");
	}

	/** Receive a response.
	 * @param is Input stream to read.
	 * @param n_bytes Number of bytes to receive.
	 * @return Array of bytes received.
	 * @throws IOException On any errors receiving response. */
	protected final byte[] recvResponse(InputStream is, int n_bytes)
		throws IOException
	{
		byte[] buf = new byte[n_bytes];
		int n_rcv = 0;
		while(n_rcv < n_bytes) {
			int r = is.read(buf, n_rcv, n_bytes - n_rcv);
			if(r <= 0)
				throw new EOFException("END OF STREAM");
			n_rcv += r;
		}
		return buf;
	}
}
