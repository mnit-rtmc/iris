/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import junit.framework.TestCase;

/**
 * @author Douglas Lau
 */
public class RasterBuilderTest extends TestCase {

	final RasterBuilder rb = new RasterBuilder(200, 100, 0, 0, 3,
		ColorScheme.COLOR_24_BIT);

	public RasterBuilderTest(String name) {
		super(name);
	}

	public void testCombineFailed() {
		checkCombine("", "", null);
		checkCombine("AAA[cf]", "", null);
		checkCombine("[cf]", "123", null);
		// invalid text rectangle tag
		checkCombine("P[tr1,10,50]", "[tr1,10,50]A", null);
		// new page not allowed in first message for shared
		checkCombine("P[np]Q[tr1,10,50,20]", "[tr1,10,50,20]A", null);
		// additional text rectangle not allowed for shared
		checkCombine("P[tr1,10,50,20]",
			"[tr1,10,50,20]A[tr51,1,50,20]B", null);
	}

	public void testCombineSequenced() {
		checkCombine("AAA[cf]", "123", "AAA[cf][fo][jl][jp][np]123");
		checkCombine("AAA[cf]", "123[np]XYZ",
			"AAA[cf][fo][jl][jp][np]123[np]XYZ");
	}

	public void testCombineShared1() {
		checkCombine("P[tr1,10,50,20]", "[tr1,10,50,20]A",
			"P[cf][fo][jl][jp][tr1,10,50,20]A");
	}

	public void testCombineShared2() {
		String tr = "[tr1,10,50,20]";
		String df = "[cf][fo][jl][jp]";
		checkCombine("P" + tr,
			tr + "A[np]" + tr + "B",
			"P" + df + tr + "A[np]" + df +
			"P" + df + tr + "B");
	}

	public void testCombineShared3() {
		String tr = "[tr1,10,50,20]";
		String df = "[cf][fo][jl][jp]";
		checkCombine("P" + tr,
			tr + "A[np]" + tr + "B[np]" + tr + "C",
			"P" + df + tr + "A[np]" + df +
			"P" + df + tr + "B[np]" + df +
			"P" + df + tr + "C");
	}

	private void checkCombine(String ms1, String ms2, String rs) {
		String combined = rb.combineMulti(ms1, ms2);
		assertTrue((rs != null)
			? rs.equals(combined)
			: null == combined
		);
	}
}
