/*
 * SONAR -- Simple Object Notification And Replication
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

package us.mn.state.dot.tms;

import junit.framework.TestCase;

/** 
 * MultiString test cases
 * @author Michael Darter
 * @created 05/05/09
 */
public class MultiStringTest extends TestCase {

	/** constructor */
	public MultiStringTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// normalization
		assertTrue(new MultiString("01234567890").normalize().
			equals("01234567890"));
		assertTrue(new MultiString("ABC").normalize().
			equals("ABC"));
		assertTrue(new MultiString("abc").normalize().
			equals("ABC"));
		assertTrue(new MultiString("AB|C").normalize().
			equals("ABC"));
		assertTrue(new MultiString("AB|{}{}C{}").normalize().
			equals("ABC"));
		assertTrue(new MultiString("ABC[nl]").normalize().
			equals("ABC[nl]"));
		assertTrue(new MultiString("ABC[nl3]").normalize().
			equals("ABC[nl3]"));
		assertTrue(new MultiString("ABC[np]").normalize().
			equals("ABC[np]"));
		assertTrue(new MultiString("ABC[jl]").normalize().
			equals("ABC[jl]"));
		assertTrue(new MultiString("ABC[nl][nl]").normalize().
			equals("ABC[nl]"));
		assertTrue(new MultiString("ABC DEF").normalize().
			equals("ABC DEF"));

		// FIXME: note, this case fails, should it? This causes
		//        mismatches in the quick message library between
		//        what the user typed and lib messages.
		//assertTrue(new MultiString("ABC[nl]").normalize().
		//	equals("ABC"));
	}
}
