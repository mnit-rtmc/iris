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

import junit.framework.TestCase;

/**
 * MultiBuilder test cases
 *
 * @author Douglas Lau
 */
public class MultiBuilderTest extends TestCase {

	public MultiBuilderTest(String name) {
		super(name);
	}

	public void testNPE() {
		MultiBuilder mb;
		try {
			mb = new MultiBuilder();
			mb = new MultiBuilder("");
			assertTrue(true);
		}
		catch (NullPointerException ex) {
			assertTrue(false);
		}
		try {
			mb = new MultiBuilder(null);
			assertTrue(false);
		}
		catch (NullPointerException ex) {
			assertTrue(true);
		}
	}

	public void testSpan() {
		checkSpan("ABC");
		checkSpan("DEF");
		checkSpan("123 XYZ");
		checkSpan(new String[] { "ABC", "DEF"}, "ABCDEF");
		checkSpan(new String[] { "123", "XYZ"}, "123XYZ");
	}

	private void checkSpan(String txt) {
		MultiBuilder mb = new MultiBuilder();
		mb.addSpan(txt);
		assertTrue(mb.toString().equals(txt));
	}

	private void checkSpan(String[] txt, String txt2) {
		MultiBuilder mb = new MultiBuilder();
		for (String t: txt)
			mb.addSpan(t);
		assertTrue(mb.toString().equals(txt2));
	}

	public void testLine() {
		checkLine(null, "[nl]");
		checkLine(1, "[nl1]");
		checkLine(2, "[nl2]");
		checkLine(3, "[nl3]");
		checkLine(4, "[nl4]");
		checkLine(5, "[nl5]");
	}

	private void checkLine(Integer spacing, String txt) {
		MultiBuilder mb = new MultiBuilder();
		mb.addLine(spacing);
		assertTrue(mb.toString().equals(txt));
	}

	public void testPage() {
		MultiBuilder mb = new MultiBuilder();
		mb.addPage();
		assertTrue(mb.toString().equals("[np]"));
	}
}
