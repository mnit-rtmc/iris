/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * A video stream which reads an MJPEG source.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class MJPEGStream implements VideoStream {

	/** Input stream to read */
	private final InputStream stream;

	/** Count of rendered frames */
	private int n_frames = 0;

	/** Create a new MJPEG stream */
	public MJPEGStream(InputStream is) {
		stream = is;
	}

	/** Get the next image in the mjpeg stream */
	public byte[] getImage() throws IOException {
		int n_size = getImageSize();
		byte[] image = new byte[n_size];
		int n_bytes = 0;
		while(n_bytes < n_size) {
			int r = stream.read(image, n_bytes, n_size - n_bytes);
			if(r >= 0)
				n_bytes += r;
			else
				throw new IOException("End of stream");
		}
		n_frames++;
		return image;
	}

	/** Get the length of the next image */
	private int getImageSize() throws IOException {
		for(int i = 0; i < 100; i++) {
			String s = readLine();
			if(s.toLowerCase().indexOf("content-length") > -1) {
				// throw away an empty line after the
				// content-length header
				readLine();
				return parseContentLength(s);
			}
		}
		throw new IOException("Too many headers");
	}

	/** Parse the content-length header */
	private int parseContentLength(String s) throws IOException {
		s = s.substring(s.indexOf(":") + 1);
		s = s.trim();
		try {
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			throw new IOException("Invalid content-length");
		}
	}

	/** Read the next line of text */
	private String readLine() throws IOException {
		StringBuilder b = new StringBuilder();
		while(true) {
			int ch = stream.read();
			if(ch < 0) {
				if(b.length() == 0)
					throw new IOException("End of stream");
				else
					break;
			}
			b.append((char)ch);
			if(ch == '\n')
				break;
		}
		return b.toString();
	}

	/** Close the video stream */
	public void close() {
		try {
			stream.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Get the number of frames rendered */
	public int getFrameCount() {
		return n_frames;
	}
}
