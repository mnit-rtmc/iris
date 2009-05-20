/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * SXml test cases
 * @author Michael Darter
 * @created 05/20/09
 * @see SXml
 */
public class SXmlTest extends TestCase {

	/** constructor */
	public SXmlTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// extractUnderline
		assertTrue(SXml.extractUnderline(null) == null);
		assertTrue(SXml.extractUnderline("") == null);
		assertTrue(SXml.extractUnderline("<u></u>").equals(""));
		assertTrue(SXml.extractUnderline("abc<u></u>def").equals(""));
		assertTrue(SXml.extractUnderline("abc<u>X</u>def").equals("X"));
		assertTrue(SXml.extractUnderline("abc<u>XX</u>def").equals("XX"));
		assertTrue(SXml.extractUnderline("<u>XX</u>").equals("XX"));
		assertTrue(SXml.extractUnderline("abcd<u>XX</u>").equals("XX"));
	}
}
