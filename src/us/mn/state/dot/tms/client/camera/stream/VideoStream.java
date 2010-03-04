/*
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

/**
 * A video stream produces a stream of image data.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public interface VideoStream {

	/** Get the next image in the stream */
	byte[] getImage() throws IOException;

	/** Get the number of frames rendered */
	int getFrameCount();

	/** Close the video stream */
	void close();
}
