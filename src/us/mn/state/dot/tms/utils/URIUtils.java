/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.lang.IllegalArgumentException;
import java.net.URI;


/**
 * Misc. URI/URL utilities
 *
 * @author Travis Swanston
 */
public class URIUtils {

	/**
	 * Check that the scheme of a given URI matches the given String.
	 * @param uri a String representation of the URI
	 * @param scheme the scheme to match against
	 * @return Returns false if either argument is null, false if uri
	 *         violates RFC 2396 or has an undefined scheme,
	 *         otherwise returns whether uri string-matches scheme.
	 */
	static public boolean checkScheme(String uri, String scheme) {
		if ( (uri == null) || (scheme == null) )
			return false;
		URI uriObj;
		try {
			uriObj = URI.create(uri);
		}
		catch (IllegalArgumentException e) {
			/* RFC 2396 violation */
			return false;
		}
		String uriScheme = uriObj.getScheme();
		if (uriScheme == null)
			/* scheme undefined */
			return false;
		return (uriScheme.equals(scheme));
	}

}

