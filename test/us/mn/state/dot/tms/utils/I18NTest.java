/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.utils.I18N.extractUnderline;

/** 
 * @author Michael Darter
 * @author Douglas Lau
 */
public class I18NTest extends TestCase {

	public I18NTest(String name) {
		super(name);
	}

	public void test() {
		assertTrue(extractUnderline(null) == null);
		assertTrue(extractUnderline("") == null);
		assertTrue(extractUnderline("<u></u>").equals(""));
		assertTrue(extractUnderline("abc<u></u>def").equals(""));
		assertTrue(extractUnderline("abc<u>X</u>def").equals("X"));
		assertTrue(extractUnderline("abc<u>XX</u>def").equals("XX"));
		assertTrue(extractUnderline("<u>XX</u>").equals("XX"));
		assertTrue(extractUnderline("abcd<u>XX</u>").equals("XX"));
	}
}
