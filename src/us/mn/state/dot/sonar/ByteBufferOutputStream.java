/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2010-2017  Minnesota Department of Transportation
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

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This is an output stream backed by a byte buffer.  It automatically expands
 * as necessary.
 *
 * @author Douglas Lau
 */
public class ByteBufferOutputStream extends OutputStream {

	/** Allocate a byte buffer with at least n_bytes capacity */
	static private ByteBuffer allocate(int n_bytes) {
		// Minimum buffer size is 2**10 (1024)
		// Maximum buffer size is 2**31 (2 GB)
		for (int i = 10; i < 32; i++) {
			int cap = 1 << i;
			if (cap >= n_bytes)
				return ByteBuffer.allocate(cap);
		}
		throw new IllegalArgumentException();
	}

	/** Default byte buffer (before expansion) */
	private final ByteBuffer o_buffer;

	/** Byte buffer where data is written */
	private ByteBuffer buffer;

	/** Create a new byte buffer output stream */
	public ByteBufferOutputStream() {
		this(0);
	}

	/** Create a new byte buffer output stream */
	public ByteBufferOutputStream(int n_bytes) {
		o_buffer = allocate(n_bytes);
		buffer = o_buffer;
	}

	/** Get the current byte buffer */
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/** Write a single byte to the output stream */
	@Override
	public void write(int b) {
		if (buffer.remaining() < 1)
			expand(1);
		buffer.put((byte) b);
	}

	/** Write an array of bytes to the output stream */
	@Override
	public void write(byte[] b, int off, int len) {
		if (off < 0 || len < 0 || off + len > b.length)
			throw new IndexOutOfBoundsException();
		if (buffer.remaining() < len)
			expand(len);
		buffer.put(b, off, len);
	}

	/** Expand the buffer by the specified number of bytes */
	private void expand(int n_bytes) {
		ByteBuffer buf = allocate(buffer.position() + n_bytes);
		buffer.flip();
		buf.put(buffer);
		buffer = buf;
	}

	/** Compact the buffer */
	public void compact() {
		if (buffer.hasRemaining())
			buffer.compact();
		else {
			buffer = o_buffer;
			o_buffer.clear();
		}
	}
}
