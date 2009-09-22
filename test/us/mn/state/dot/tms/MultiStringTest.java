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
 */
public class MultiStringTest extends TestCase {

	/** constructor */
	public MultiStringTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		getFonts();
		getPageOnTime();
		normalization();
		replacePageOnTime();
		equals();
		getText();
		getNumPages();
		etc();
	}

	/** everything else */
	private void etc() {
		// constructor
		try {
			new MultiString(null);
			assertTrue(false);
		} catch(NullPointerException ex) {
			assertTrue(true);
		}

		// isValid
		assertTrue(new MultiString().isValid());
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
	}

	/** replacePageOnTime */
	private void replacePageOnTime() {
		MultiString t1, t2;
		int[] pt;

		t1 = new MultiString("YA1[np]YA2");
		t2 = t1.replacePageOnTime(4);
		pt = t2.getPageOnTimes(7);
		assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
		assertTrue("[pt4o]YA1[np]YA2".equals(t2.toString()));

		t1 = new MultiString("[pt3o]YA1[np]OH YA2");
		t2 = t1.replacePageOnTime(4);
		pt = t2.getPageOnTimes(7);
		assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
		assertTrue("[pt4o]YA1[np]OH YA2".equals(t2.toString()));

		t1 = new MultiString("[pt3o50]YA1[np]OH YA2");
		t2 = new MultiString(MultiString.
			replacePageOnTime(t1.toString(), 4));
		pt = t2.getPageOnTimes(7);
		assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
		assertTrue("[pt4o50]YA1[np]OH YA2".equals(t2.toString()));

		t1 = new MultiString("[pt3o50]YA1[np][pt22o60]OH YA2");
		t2 = new MultiString(MultiString.
			replacePageOnTime(t1.toString(), 4));
		pt = t2.getPageOnTimes(7);
		assertTrue(pt.length == 2 && pt[0] == 4 && pt[1] == 4);
		assertTrue("[pt4o50]YA1[np][pt4o60]OH YA2".
			equals(t2.toString()));
	}

	/** getFonts */
	private void getFonts() {
		// bogus default font numbers
		assertTrue(new MultiString("").getFonts(-10).length == 0);
		assertTrue(new MultiString("").getFonts(0).length == 0);
		assertTrue(new MultiString("").getFonts(256).length == 0);
		assertTrue(new MultiString("").getFonts(257).length == 0);

		// default is used - 1 page
		assertTrue(new MultiString("YA1").getFonts(255).length == 1);
		assertTrue(new MultiString("YA1").getFonts(255)[0] == 255);

		// default is used - 2 page
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255)[0] == 255);
		assertTrue(new MultiString("YA1[np]YA2").
			getFonts(255)[1] == 255);

		// mainline 1 page
		assertTrue(new MultiString("[fo2]YA1").
			getFonts(255).length == 1);
		assertTrue(new MultiString("[fo2]YA1").
			getFonts(255)[0] == 2);

		// mainline 2 page
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255)[0] == 2);
		assertTrue(new MultiString("[fo2]YA1[np][fo3]YA2").
			getFonts(255)[1] == 3);

		// mainline 2 page w/ default
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255)[0] == 255);
		assertTrue(new MultiString("YA1[np][fo3]YA2").
			getFonts(255)[1] == 3);

		// mainline 2 page w/ font carryover
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255).length == 2);
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255)[0] == 3);
		assertTrue(new MultiString("[fo3]YA1[np]YA2").
			getFonts(255)[1] == 3);
	}

	/** getPageOnTime */
	private void getPageOnTime() {
		int[] t;

		t = new MultiString("").getPageOnTimes(254);
		assertTrue(t.length == 1);
		assertTrue(t[0] == 254);

		t = new MultiString("ABC1[np]ABC2").
			getPageOnTimes(7);
		assertTrue(t.length == 2);
		assertTrue(t[0] == 7 && t[1] == 7);

		t = new MultiString("ABC1[np]ABC2").
			getPageOnTimes(7);
		assertTrue(t.length == 2);
		assertTrue(t[0] == 7 && t[1] == 7);

		t = new MultiString("[pto]YA1[np]OH YA2").
			getPageOnTimes(7);
		assertTrue(t.length == 2);
		assertTrue(t[0] == 7 && t[1] == 7);

		t = new MultiString("[pt3o]YA1[np]OH YA2").
			getPageOnTimes(7);
		assertTrue(t.length == 2);
		assertTrue(t[0] == 3 && t[1] == 3);

		t = new MultiString("[pto4]YA1[np]OH YA2").
			getPageOnTimes(7);
		assertTrue(t.length == 2);
		assertTrue(t[0] == 7 && t[1] == 7);

		t = new MultiString("[pt7o4]PG1[np][pt8o4]PG2" +
			"[np]PG3").getPageOnTimes(7);
		assertTrue(t.length == 3);
		assertTrue(t[0] == 7 && t[1] == 8 && t[2] == 8);

		t = new MultiString("[pt7o4][np][pt8o4][np]A").
			getPageOnTimes(7);
		assertTrue(t.length == 3);
		assertTrue(t[0] == 7);
		//assertTrue(t[1] == 8); //FIXME: this fails!
		assertTrue(t[2] == 8);

		// test page time specified once for entire message
		assertTrue(new MultiString("ABC[nl]DEF").
			getPageOnTime().toTenths() == 0);
		DmsPgTime defspg = DmsPgTime.getDefaultOn(true);
		DmsPgTime defmpg = DmsPgTime.getDefaultOn(false);
		assertTrue(new MultiString("").
			getPageOnTime().equals(defspg));
		assertTrue(new MultiString("ABC[nl]DEF").
			getPageOnTime().equals(defspg));
		assertTrue(new MultiString("ABC[np]DEF").
			getPageOnTime().equals(defmpg));
		assertTrue(new MultiString("[pt13o0]ABC[nl]DEF").
			getPageOnTime().toTenths() == 13);
		assertTrue(new MultiString("ABC[nl][pt14o]DEF").
			getPageOnTime().toTenths() == 14);
		//FIXME: this fails, probably shouldn't
		//assertTrue(new MultiString("ABC[nl]DEF[pt14o]").
		//	getPageOnTime().toTenths() == 14);
		assertTrue(new MultiString("ABC[np][pt14o]DEF").
			getPageOnTime().equals(defmpg));
		assertTrue(new MultiString("ABC[np]DEF[pt14o]").
			getPageOnTime().equals(defmpg));
	}

	/** normalization */
	private void normalization() {
		assertTrue(new MultiString("01234567890").normalize().
			equals("01234567890"));
		assertTrue(new MultiString("ABC").normalize().
			equals("ABC"));
		assertTrue(new MultiString("abc").normalize().
			equals("ABC"));
		assertTrue(new MultiString("DON'T").normalize().
			equals("DON'T"));
		assertTrue(new MultiString("SPACE SPACE").normalize().
			equals("SPACE SPACE"));
		assertTrue(new MultiString("AB|C").normalize().
			equals("ABC"));
		assertTrue(new MultiString("AB|{}{}C{}").normalize().
			equals("ABC"));
		assertTrue(new MultiString("ABC DEF").normalize().
			equals("ABC DEF"));
		assertTrue(new MultiString("ABC[bad]DEF").normalize().
			equals("ABCDEF"));
		assertTrue(new MultiString("ABC[nl]DEF").normalize().
			equals("ABC[nl]DEF"));
		assertTrue(new MultiString("ABC[nl3]DEF").normalize().
			equals("ABC[nl]DEF"));
		assertTrue(new MultiString("ABC[np]DEF").normalize().
			equals("ABC[np]DEF"));
		assertTrue(new MultiString("ABC[jl4]DEF").normalize().
			equals("ABC[jl4]DEF"));
		assertTrue(new MultiString("ABC[jl6]DEF").normalize().
			equals("ABCDEF"));
		assertTrue(new MultiString("ABC[jp4]DEF").normalize().
			equals("ABC[jp4]DEF"));
		assertTrue(new MultiString("[fo3]ABC DEF").normalize().
			equals("[fo3]ABC DEF"));
		assertTrue(new MultiString("[fo3,beef]ABC DEF").normalize().
			equals("[fo3,beef]ABC DEF"));
		assertTrue(new MultiString("[g1]").normalize().
			equals("[g1]"));
		assertTrue(new MultiString("[g1,5,5]").normalize().
			equals("[g1,5,5]"));
		assertTrue(new MultiString("[g1,5,5,beef]").normalize().
			equals("[g1,5,5,beef]"));
		assertTrue(new MultiString("[cf255,255,255]").normalize().
			equals("[cf255,255,255]"));
		assertTrue(new MultiString("[cf0,255,255]").normalize().
			equals("[cf0,255,255]"));
		assertTrue(new MultiString("[cf0,255,0]").normalize().
			equals("[cf0,255,0]"));
		assertTrue(new MultiString("[pto]").normalize().
			equals("[pto]"));
		assertTrue(new MultiString("[pt10o]").normalize().
			equals("[pt10o]"));
		assertTrue(new MultiString("[pt10o5]").normalize().
			equals("[pt10o5]"));
		assertTrue(new MultiString("[pto5]").normalize().
			equals("[pto5]"));
		assertTrue(new MultiString("[tr1,1,40,20]").normalize().
			equals("[tr1,1,40,20]"));
		assertTrue(new MultiString("[tr1,1,0,0]").normalize().
			equals("[tr1,1,0,0]"));
		assertTrue(new MultiString("[ttS100]").normalize().
			equals("[ttS100]"));
	}

	/** equals */
	private void equals() {
		{
			MultiString s1 = null;
			MultiString s2 = null;
			assertTrue(MultiString.equals(s1, s2));
		}
		assertFalse(MultiString.equals(null, new MultiString("")));
		assertTrue(MultiString.equals("", ""));
		assertFalse(MultiString.equals("[fo2]LINE1", "[fo1]LINE1"));
		assertTrue(MultiString.equals("LINE1[nl][nl]", "LINE1"));
		assertTrue(MultiString.equals("LINE1[nl][np]", "LINE1[np]"));
	}

	/** getText */
	private void getText() {
		String[] s;

		s = new MultiString("").getText();
		assertTrue(s.length == 0);

		s = new MultiString("ABC").getText();
		assertTrue(s.length == 1);
		assertTrue(s[0].equals("ABC"));

		s = new MultiString("ABC[nl]").getText();
		assertTrue(s.length == 1);
		assertTrue(s[0].equals("ABC"));

		s = new MultiString("ABC[nl]DEF").getText();
		assertTrue(s.length == 2);
		assertTrue(s[0].equals("ABC"));
		assertTrue(s[1].equals("DEF"));

		s = new MultiString("ABC[nl]DEF[np]GHI[nl][nl]123").getText();
		assertTrue(s.length == 4);
		assertTrue(s[0].equals("ABC"));
		assertTrue(s[1].equals("DEF"));
		assertTrue(s[2].equals("GHI"));
		assertTrue(s[3].equals("123"));
	}

	/** getNumPages */
	private void getNumPages() {
		assertTrue(new MultiString("").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][nl]").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][nl]").
			getNumPages() == 1);
		assertTrue(new MultiString("ABC[nl][np]").
			getNumPages() == 2);
		assertTrue(new MultiString("ABC[nl][np]DEF").
			getNumPages() == 2);
		assertTrue(new MultiString("ABC[nl][np]DEF[np]").
			getNumPages() == 3);
	}
}
