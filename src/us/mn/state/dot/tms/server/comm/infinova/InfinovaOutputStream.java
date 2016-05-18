/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.infinova;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import us.mn.state.dot.sched.DebugLog;
import static us.mn.state.dot.tms.server.comm.infinova.InfinovaPoller.INF_LOG;

/**
 * An OutputStream which prepends an Infinova header to messages.
 *
 * @author Douglas Lau
 */
public class InfinovaOutputStream extends OutputStream {

	/** Maximum message size */
	static private final int MAX_MESSAGE = 256;

	/** Message ID */
	private enum MsgId {
		auth(0x01), ptz(0x13);
		private MsgId(int i) {
			id = (byte)i;
		}
		public final byte id;
	}

	/** Size of authentication message */
	static private final int AUTH_SZ = 64;

	/** Authentication message */
	static private final byte[] AUTH = new byte[AUTH_SZ + 2];

	/** Size of PTZ header */
	static private final int PTZ_SZ = 12;

	/** Log a message */
	static private void log(String m) {
		if (INF_LOG.isOpen())
			INF_LOG.log(m);
	}

	/** Buffered output stream */
	private final BufferedOutputStream out;

	/** Flag for needs authentication */
	private boolean needs_auth = true;

	/** Create an Infinova output stream */
	public InfinovaOutputStream(OutputStream os) {
		out = new BufferedOutputStream(os, MAX_MESSAGE);
	}

	/** Write one byte to the output stream.  This method bypasses the
	 * Infinova header stuff. */
	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	/** Write the specified buffer to the output stream */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (needs_auth)
			writeAuthentication();
		writeHeader(MsgId.ptz, PTZ_SZ + len);
		writePtzHeader(len);
		log("write: " + len);
		out.write(b, off, len);
	}

	/** Write the specified buffer to the output stream */
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	/** Write an authentication message */
	private void writeAuthentication() throws IOException {
		writeHeader(MsgId.auth, AUTH_SZ);
		out.write(AUTH);
		needs_auth = false;
	}

	/** Write an infinova header */
	private void writeHeader(MsgId msg, int len) throws IOException {
		log("writeHeader: " + msg + ", len: " + len);
		byte[] header = new byte[] {
			'I', 'N', 'F', 0, 0, 0, 0, 0, 0, 0, 0, 0,
		};
		header[3] = msg.id;
		if (msg == MsgId.auth) {
			header[5] = 1;
			header[7] = 1;
		}
		header[11] = (byte) len;
		out.write(header);
	}

	/** Write a PTZ header */
	private void writePtzHeader(int len) throws IOException {
		byte[] header = new byte[PTZ_SZ];
		header[0] = 1;
		header[7] = (byte) len;
		out.write(header);
	}

	/** Flush pending data to the output stream */
	@Override
	public void flush() throws IOException {
		log("flush");
		out.flush();
	}

	/** Close the output stream */
	@Override
	public void close() throws IOException {
		try {
			flush();
		}
		finally {
			log("close");
			out.close();
		}
	}
}
