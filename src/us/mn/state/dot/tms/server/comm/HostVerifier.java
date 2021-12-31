/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/*
 * This class performs host name verification.
 *
 * @author Michael Darter
 */
public class HostVerifier implements HostnameVerifier {
	@Override public boolean verify(String hostname, SSLSession sss) {
		// host is always valid regardless of cert state
		return true;
	}
}
