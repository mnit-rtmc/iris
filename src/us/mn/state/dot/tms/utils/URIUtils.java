/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Miscellaneous URI functions.
 *
 * @author Douglas Lau
 */
public class URIUtils {

	/** Create a URI from a string.
	 * @param uri String specifier.
	 * @return Specified URI, or null if invalid. */
	static public URI create(String uri) throws URISyntaxException {
		// If the URI contains a colon, parse
		// assuming a scheme is defined
		if (uri.indexOf(':') >= 0) {
			try {
				return new URI(uri);
			}
			catch (URISyntaxException e) {
				// the colon was proabaly for
				// a tcp or udp port number
			}
		}
		// Force the scheme to be null
		return new URI("//" + uri);
	}
}
