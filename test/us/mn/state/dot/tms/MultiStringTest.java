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
		// isValid
		assertTrue(new MultiString(null).isValid());
		assertTrue(new MultiString("").isValid());
		assertTrue(new MultiString("ABC").isValid());
		// nl tag
		assertTrue(new MultiString("ABC[nl]DEF").isValid());
		assertTrue(new MultiString("ABC[nl1]DEF").isValid());
		//assertFalse(new MultiString("ABC[nl12]DEF").isValid());
		// fo tag
		assertTrue(new MultiString("ABC[fo]DEF").isValid());
		assertTrue(new MultiString("ABC[fo1]DEF").isValid());
		assertTrue(new MultiString("ABC[fo12]DEF").isValid());
		assertTrue(new MultiString("ABC[fo123]DEF").isValid());
		//assertFalse(new MultiString("ABC[fo1234]DEF").isValid());
		// np tag
		assertTrue(new MultiString("ABC[np]DEF").isValid());
		assertTrue(new MultiString("ABC[nl1]DEF").isValid());
		// jp tag
		//assertFalse(new MultiString("ABC[jp]DEF").isValid());
		assertTrue(new MultiString("ABC[jp1]DEF").isValid());
		assertTrue(new MultiString("ABC[jp2]DEF").isValid());
		assertTrue(new MultiString("ABC[jp3]DEF").isValid());
		assertTrue(new MultiString("ABC[jp4]DEF").isValid());
		//assertFalse(new MultiString("ABC[jp5]DEF").isValid());
		// pt tag
		assertTrue(new MultiString("ABC[pto]").isValid());
		assertTrue(new MultiString("ABC[pt1o]").isValid());
		assertTrue(new MultiString("ABC[pt12o]").isValid());
		assertTrue(new MultiString("ABC[pt123o]").isValid());
		//assertFalse(new MultiString("ABC[pt1234o]").isValid());
		assertTrue(new MultiString("ABC[pto1]").isValid());
		assertTrue(new MultiString("ABC[pto12]").isValid());
		assertTrue(new MultiString("ABC[pto123]").isValid());
		//assertFalse(new MultiString("ABC[pto1234]").isValid());

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

		// getFont
		// FIXME: need to login to an IRIS server to run this test case
		{
			//int[] pgs = (new MultiString("[fo1]PAGE ONE")).getFont(5);
			//assertTrue(pgs.length == 1 && pgs[0] == 1);
		}

		// getPageOnTime
		{
			int[] t;

			t = new MultiString("").getPageOnTime(6);
			assertTrue(t.length == 0);

			t = new MultiString("ABC1[np]ABC2").
				getPageOnTime(7);
			assertTrue(t.length == 2);
			assertTrue(t[0] == 7 && t[1] == 7);

			t = new MultiString("[pto]YA1[np]OH YA2").
				getPageOnTime(7);
			assertTrue(t.length == 2);
			assertTrue(t[0] == 7 && t[1] == 7);

			t = new MultiString("[pt3o]YA1[np]OH YA2").
				getPageOnTime(7);
			assertTrue(t.length == 2);
			assertTrue(t[0] == 3 && t[1] == 3);

			t = new MultiString("[pto4]YA1[np]OH YA2").
				getPageOnTime(7);
			assertTrue(t.length == 2);
			assertTrue(t[0] == 7 && t[1] == 7);

			t = new MultiString("[pt7o4]PG1[np][pt8o4]PG2" +
				"[np]PG3").getPageOnTime(7);
			assertTrue(t.length == 3);
			assertTrue(t[0] == 7 && t[1] == 8 && t[2] == 8);

			/*
			FIXME: this test case fails: with no text on each
			       page, the parse method isn't behaving as
			       expected--is this a bug?
			t = new MultiString("[pt7o4][np][pt8o4][np]A").
				getPageOnTime(7);
			assertTrue(t.length == 3);
			assertTrue(t[0] == 7 && t[1] == 8 && t[2] == 8);
			*/
		}

		// replacePageOnTime
		{
			MultiString t1, t2;
			int[] pt;

			t1 = new MultiString("YA1[np]YA2");
			t2 = new MultiString(t1.replacePageOnTime(4));
			pt = t2.getPageOnTime(7);
			assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
			assertTrue("[pt4o]YA1[np]YA2".equals(t2.toString()));

			t1 = new MultiString("[pt3o]YA1[np]OH YA2");
			t2 = new MultiString(t1.replacePageOnTime(4));
			pt = t2.getPageOnTime(7);
			assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
			assertTrue("[pt4o]YA1[np]OH YA2".equals(t2.toString()));

			t1 = new MultiString("[pt3o50]YA1[np]OH YA2");
			t2 = new MultiString(t1.replacePageOnTime(4));
			pt = t2.getPageOnTime(7);
			assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
			assertTrue("[pt4o50]YA1[np]OH YA2".equals(t2.toString()));

			t1 = new MultiString("[pt3o50]YA1[np][pt22o60]OH YA2");
			t2 = new MultiString(t1.replacePageOnTime(4));
			pt = t2.getPageOnTime(7);
			assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
			assertTrue("[pt4o50]YA1[np][pt4o60]OH YA2".equals(t2.toString()));
		}

		// getFont
		{
			MultiString t1, t2;
			int[] fn;

			t1 = new MultiString(null);
			fn = t1.getFont(255);
			assertTrue(fn.length == 0);

			t1 = new MultiString("");
			fn = t1.getFont(255);
			assertTrue(fn.length == 0);

			t1 = new MultiString("YA1");
			fn = t1.getFont(255);
			assertTrue(fn.length == 1 && fn[0] == 255);

			t1 = new MultiString("[fo2]YA1");
			fn = t1.getFont(255);
			assertTrue(fn.length == 1 && fn[0] == 2);

			t1 = new MultiString("YA1[np]YA2");
			fn = t1.getFont(255);
			assertTrue(fn.length == 2 && fn[0] == 255 && fn[1] == 255);

			t1 = new MultiString("[fo2]YA1[np][fo3]YA2");
			fn = t1.getFont(255);
			assertTrue(fn.length == 2 && fn[0] == 2 && fn[1] == 3);

			t1 = new MultiString("YA1[np][fo3]YA2");
			fn = t1.getFont(255);
			assertTrue(fn.length == 2 && fn[0] == 255 && fn[1] == 3);

			t1 = new MultiString("[fo3]YA1[np]YA2");
			fn = t1.getFont(255);
			assertTrue(fn.length == 2 && fn[0] == 3 && fn[1] == 3);
		}
	}
}
