/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
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
 * Test cases
 * @author Michael Darter
 * @created 04/23/10
 */
public class SFileTest extends TestCase {

	/** constructor */
	public SFileTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		String temp_fname = "temp_file";

		// writeStringToFile
		assertTrue(SFile.writeStringToFile("temp_file", null, true));
		assertTrue(SFile.fileExists(temp_fname));
		assertFalse(SFile.writeStringToFile(null, null, true));
 
		// delete
		assertTrue(SFile.delete(null));
		assertTrue(SFile.delete(temp_fname)); // created above
		assertTrue(SFile.delete("sf324dgdf"));

		// readUrl(String)
		String nullstring = null;
		assertTrue(SFile.readUrl(nullstring) == null);
		assertTrue(SFile.readUrl("") == null);

		// getAbsolutePath
		assertTrue(SFile.getAbsolutePath(null).equals(""));
		assertTrue(SFile.getAbsolutePath("").equals(""));

		// fileExists
		assertFalse(SFile.fileExists(null));
		assertFalse(SFile.fileExists(""));
		assertTrue(SFile.fileExists("/home"));
		assertTrue(SFile.fileExists("/home/"));
		assertFalse(SFile.fileExists("/asdfksdfkljgdfg"));
	}
}
