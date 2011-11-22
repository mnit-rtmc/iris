/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.util.Arrays;
import junit.framework.TestCase;
import us.mn.state.dot.tms.utils.SString;

/** 
 * MultiString test cases
 * @author Michael Darter
 * @author Douglas Lau
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
		getLines();
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
		assertTrue(MultiString.normalize("01234567890").
			equals("01234567890"));
		assertTrue(MultiString.normalize("ABC").
			equals("ABC"));
		assertTrue(MultiString.normalize("abc").
			equals("abc"));
		assertTrue(MultiString.normalize("DON'T").
			equals("DON'T"));
		assertTrue(MultiString.normalize("SPACE SPACE").
			equals("SPACE SPACE"));
		assertTrue(MultiString.normalize("AB|C").
			equals("ABC"));
		assertTrue(MultiString.normalize("AB|{}{}C{}").
			equals("ABC"));
		assertTrue(MultiString.normalize("ABC DEF").
			equals("ABC DEF"));
		assertTrue(MultiString.normalize("ABC[bad]DEF").
			equals("ABCDEF"));
		assertTrue(MultiString.normalize("ABC[nl]DEF").
			equals("ABC[nl]DEF"));
		assertTrue(MultiString.normalize("ABC[nl3]DEF").
			equals("ABC[nl3]DEF"));
		assertTrue(MultiString.normalize("ABC[np]DEF").
			equals("ABC[np]DEF"));
		assertTrue(MultiString.normalize("ABC[jl4]DEF").
			equals("ABC[jl4]DEF"));
		assertTrue(MultiString.normalize("ABC[jl6]DEF").
			equals("ABCDEF"));
		assertTrue(MultiString.normalize("ABC[jp4]DEF").
			equals("ABC[jp4]DEF"));
		assertTrue(MultiString.normalize("[fo3]ABC DEF").
			equals("[fo3]ABC DEF"));
		assertTrue(MultiString.normalize("[fo3,beef]ABC DEF").
			equals("[fo3,beef]ABC DEF"));
		assertTrue(MultiString.normalize("[g1]").
			equals("[g1]"));
		assertTrue(MultiString.normalize("[g1,5,5]").
			equals("[g1,5,5]"));
		assertTrue(MultiString.normalize("[g1,5,5,beef]").
			equals("[g1,5,5,beef]"));
		assertTrue(MultiString.normalize("[cf255,255,255]").
			equals("[cf255,255,255]"));
		assertTrue(MultiString.normalize("[cf0,255,255]").
			equals("[cf0,255,255]"));
		assertTrue(MultiString.normalize("[cf0,255,0]").
			equals("[cf0,255,0]"));
		assertTrue(MultiString.normalize("[pto]").
			equals("[pto]"));
		assertTrue(MultiString.normalize("[pt10o]").
			equals("[pt10o]"));
		assertTrue(MultiString.normalize("[pt10o5]").
			equals("[pt10o5]"));
		assertTrue(MultiString.normalize("[pto5]").
			equals("[pto5]"));
		assertTrue(MultiString.normalize("[tr1,1,40,20]").
			equals("[tr1,1,40,20]"));
		assertTrue(MultiString.normalize("[tr1,1,0,0]").
			equals("[tr1,1,0,0]"));
		assertTrue(MultiString.normalize("[ttS100]").
			equals("[ttS100]"));
	}

	/** equals */
	private void equals() {
		MultiString t1 = new MultiString("x");
		MultiString t2 = new MultiString("x");
		MultiString t3 = new MultiString("x");
		// equals null contract
		assertFalse(t1.equals(null));
		// reflexive
		assertTrue(t1.equals(t1));
		// symmetric
		assertTrue(t1.equals(t2) && t2.equals(t1));
		// transitive
		assertTrue(t1.equals(t2) && t2.equals(t3) && t1.equals(t3));
		// simple cases
		assertTrue(new MultiString("").equals(new MultiString("")));
		assertTrue(new MultiString("").equals(""));
		assertTrue(new MultiString("XXX").equals("XXX"));
		assertTrue(new MultiString("XXX").equals(new MultiString("XXX")));
		assertFalse(new MultiString("XXX").equals("XXY"));
		assertFalse(new MultiString("XXX").equals(new MultiString("XXY")));
		// verify normalization used
		assertTrue(new MultiString("[fo1]abc").equals("[fo1]abc"));
		assertTrue(new MultiString("[fo1]abc").equals(new MultiString("[fo1]abc")));
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
		assertFalse(Arrays.equals(
			new MultiString("ABC[nl]DEF[np]GHI[nl][nl]123").
			getText(), new String[] {"ABC", "DEF", "GHI", "", "123"}));

		assertTrue(Arrays.equals(
			new MultiString("1[nl][nl]4[nl][nl]").getText(), 
			new String[] {"1", "", "4"}));
		assertTrue(Arrays.equals(
			new MultiString("1[nl][nl]4[nl][nl]").getText(), 
			new String[] {"1", "", "4"}));
		assertTrue(Arrays.equals(
			new MultiString("1[nl][nl]4[nl][nl]").getText(), 
			new String[] {"1", "", "4"}));
		assertTrue(Arrays.equals(
			new MultiString("1[nl][nl]4[nl][nl]").getText(), 
			new String[] {"1", "", "4"}));
		assertTrue(Arrays.equals(
			new MultiString("1[nl][nl]4[nl][nl]").getText(), 
			new String[] {"1", "", "4"}));
		assertTrue(Arrays.equals(
			new MultiString("1[nl]2[nl]3").getText(), 
			new String[] {"1", "2", "3"}));

		// tag in the middle of a span
		assertTrue(Arrays.equals(
			new MultiString("ABC[nl]D[j1x]E[j1x]FGH[nl]IJK[nl]").
			getText(), new String[] {"ABC", "D E FGH", "IJK"}));
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

	/** getLines */
	private void getLines() {
		assertTrue(Arrays.equals(new String[] { "" },
			new MultiString("").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
			new MultiString("ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
			new MultiString("ABC[nl][nl]").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
			new MultiString("ABC[nl][np]").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF" },
			new MultiString("ABC[nl][np]DEF").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF" },
			new MultiString("ABC[nl][np]DEF[np]").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "GHI" },
			new MultiString("ABC[nl]DEF[nl2]GHI").getLines()));
		assertTrue(Arrays.equals(new String[]
			{ "ABC", "DEF", "GHI", "JKL" },
		       new MultiString("ABC[nl]DEF[np]GHI[nl]JKL").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "GHI",""},
			new MultiString("ABC[nl]DEF[np]GHI").getLines()));
		assertTrue(Arrays.equals(new String[] { "[jl2]ABC", "DE[fo1]F"},
			new MultiString("[jl2]ABC[nl]DE[fo1]F").getLines()));
		assertTrue(Arrays.equals(new String[]
			{ "[cf0,0,0]ABC", "DE[sc5]F" },
		       new MultiString("[cf0,0,0]ABC[nl]DE[sc5]F").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[pb0,0,0]ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[cr255,0,0]ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[g1,0,0]ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[jp3]ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[pt50o0]ABC").getLines()));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[tr0,0,5,5]ABC").getLines()));
	}
}
