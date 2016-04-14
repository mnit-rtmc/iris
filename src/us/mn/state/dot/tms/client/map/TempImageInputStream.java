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
package us.mn.state.dot.tms.client.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * A temporary image input stream creates a temporary file to cache an image
 * input stream so that it can be used multiple times.  This is similar to
 * javax.imageio.stream.FileCacheImageInputStream, but more useful, since it
 * can be reused without reopening the original stream.
 *
 * @author Douglas Lau
 */
public class TempImageInputStream extends ImageInputStreamImpl {

	/** Size of block buffer */
	static protected final int BLOCK_SZ = 4096;

	/** Maximum file size is 64 blocks (256 KB) */
	static protected final int MAX_FILE_SZ = BLOCK_SZ * 64;

	/** Byte array input stream to cache data */
	protected final ByteArrayInputStream temp;

	/** Create a temporary image input stream */
	public TempImageInputStream(InputStream is) throws IOException {
		temp = new ByteArrayInputStream(readInputStream(is));
	}

	/** Read the entire contents of an input stream */
	private byte[] readInputStream(InputStream is) throws IOException {
		byte[] buf = new byte[BLOCK_SZ];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while(baos.size() < MAX_FILE_SZ) {
			int n_bytes = is.read(buf, 0, BLOCK_SZ);
			if(n_bytes == -1)
				break;
			baos.write(buf, 0, n_bytes);
		}
		return baos.toByteArray();
	}

	/** Check if the stream is cached (yes, it is) */
	public boolean isCached() {
		return true;
	}

	/** Check if the stream is a cached file (no) */
	public boolean isCachedFile() {
		return false;
	}

	/** Check if the stream is cached in memeory (yes) */
	public boolean isCachedMemory() {
		return true;
	}

	/** Read one byte from the stream */
	public int read() throws IOException {
		checkClosed();
		if(temp.available() > 0) {
			streamPos++;
			return temp.read();
		} else
			return -1;
	}

	/** Read data from the stream into a buffer */
	public int read(byte[] b, int off, int len) throws IOException {
		checkClosed();
		if(off < 0)
			throw new IndexOutOfBoundsException("off<0");
		if(len < 0)
			throw new IndexOutOfBoundsException("len<0");
		if(off + len > b.length)
			throw new IndexOutOfBoundsException("off+len>b.length");
		if(off + len < 0)
			throw new IndexOutOfBoundsException("off+len<0");
		if(len == 0)
			return 0;
		int n_bytes = temp.read(b, off, len);
		if(n_bytes > 0)
			streamPos += n_bytes;
		return n_bytes;
	}

	/** Seek to the specified position */
	public void seek(long pos) throws IOException {
		temp.reset();
		if(pos > 0)
			temp.skip(pos);
		super.seek(pos);
	}

	/** Flush data before the given position */
	public void flushBefore(long pos) {
		// Lease flushPos at 0 To allow reusing the cache file
	}

	/** Close the stream */
	public void close() {
		// ImageIO.read calls close, but we want to reuse the cache
		// file, so don't actually close the file (sneaky!)
	}
}
