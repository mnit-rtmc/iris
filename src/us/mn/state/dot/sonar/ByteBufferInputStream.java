/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This is an input stream backed by a byte buffer.
 *
 * @author Douglas Lau
 */
public class ByteBufferInputStream extends InputStream {

	/** Byte buffer where data is read */
	protected ByteBuffer buffer;

	/** Create a new byte buffer input stream */
	public ByteBufferInputStream(ByteBuffer buf) {
		buffer = buf;
	}

	/** Get the byte buffer */
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/** Read a single byte from the input stream */
	public int read() {
		if(available() > 0)
			return buffer.get() & 0xFF;
		else
			return -1;
	}

	/** Read an array of bytes from the input stream */
	public int read(byte[] b, int off, int len) {
		if(available() < 1)
			return -1;
		len = Math.min(len, available());
		if(len > 0)
			buffer.get(b, off, len);
		return len;
	}

	/** Skip ahead in the input stream */
	public long skip(long n) {
		n = Math.min(n, buffer.remaining());
		buffer.position((int)(buffer.position() + n));
		return n;
	}

	/** Get the number of bytes available for reading */
	public int available() {
		return buffer.remaining();
	}
}
