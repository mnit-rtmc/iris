/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import junit.framework.TestCase;
import static us.mn.state.dot.tms.utils.URIUtil.*;

/**
 * URIUtil test cases
 *
 * @author Douglas Lau
 */
public class URIUtilTest extends TestCase {

	public URIUtilTest(String name) {
		super(name);
	}

	public void testCreate() {
		assertTrue(create(UDP, "").toString().equals("udp:/"));
		assertTrue(create(TCP, "").toString().equals("tcp:/"));
		assertTrue(create(HTTP, "").toString().equals("http:/"));
	}
}
