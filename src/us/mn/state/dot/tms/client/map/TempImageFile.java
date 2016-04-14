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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A temporary image file.
 *
 * @author Douglas Lau
 */
public class TempImageFile {

	/** Temporary image input stream */
	protected final TempImageInputStream t_stream;

	/** Create a new temporary image file */
	public TempImageFile(InputStream is) throws IOException {
		t_stream = new TempImageInputStream(is);
	}

	/** Get image from the file */
	public BufferedImage getImage() throws IOException {
		t_stream.seek(0);
		return ImageIO.read(t_stream);
	}
}
