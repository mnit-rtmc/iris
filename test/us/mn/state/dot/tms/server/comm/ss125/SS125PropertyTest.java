/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2021  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ss125;

import junit.framework.TestCase;
import java.io.IOException;
import java.util.Arrays;
import us.mn.state.dot.tms.utils.HexString;

/**
 * SS125 Property test cases
 * @author Michael Darter
 */
public class SS125PropertyTest extends TestCase {

	/** constructor */
	public SS125PropertyTest(String name) {
		super(name);
	}

	/** test 24 bit speed calculation */
	public void test24FixedExamples() {
		byte[] b;

		// Wavetronix docs example 1
		b = HexString.parse("FFED40");
		assertTrue(-19.25 == SS125Property.parse24Fixed(b, 0));

		// Wavetronix docs example 2 (doc has mistake)
		b = HexString.parse("80375E");
		assertTrue(55.3671875 == SS125Property.parse24Fixed(b, 0));
	}

	/** Test 24 bit speed calculation using bodies from controller */
	public void testParse24Fixed() {
		byte[] b;

		// Body returned from controller with invalid speed
		b = HexString.parse(
			"670100000FC192059182CE004D080000FE005123021850BC");
		// range
		assertTrue(77.03125 == SS125Property.parse16Fixed(b, 12));
		// speed (invalid)
		assertNull(SS125Property.parse24Fixed(b, 17));
		// length
		assertTrue(24.3125 == SS125Property.parse16Fixed(b, 21));

		// Body returned from controller
		b = HexString.parse(
			"670100000FC192058D0939015C0A0001198043C0021604FC");
		// range
		double diff = Math.abs(92.03906 - 
			SS125Property.parse16Fixed(b, 12));
		assertTrue(diff <= .001);
		// speed
		assertTrue(67.75 == SS125Property.parse24Fixed(b, 17));
		// length
		assertTrue(22.015625 == SS125Property.parse16Fixed(b, 21));
	}
}
