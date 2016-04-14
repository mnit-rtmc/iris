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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * An image fetcher is a simple class to fetch images remotely.
 *
 * @author Douglas Lau
 */
public class ImageFetcher {

	/** Base URL to fetch images */
	protected final URL base_url;

	/** Create a new image fetcher for the specified URL */
	public ImageFetcher(String url) throws IOException {
		base_url = new URL(url);
	}

	/** Fetch the named image */
	public InputStream fetchImage(String n) throws IOException {
		URL url = new URL(base_url.toExternalForm() + n + ".png");
		return url.openStream();
	}
}
