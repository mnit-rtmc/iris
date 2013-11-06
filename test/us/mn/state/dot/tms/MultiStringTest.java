/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.utils.SString;

/**
 * MultiString test cases
 * @author Michael Darter
 * @author Douglas Lau
 */
public class MultiStringTest extends TestCase {

	public MultiStringTest(String name) {
		super(name);
	}

	public void testAsText() {
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC[fo1]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC [fo1]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC [sc4]DEF").asText()));
		assertTrue("ABC DEF".equals(new MultiString(
			"ABC[jl4]DEF").asText()));
	}

	public void testGetLines() {
		assertTrue(Arrays.equals(new String[] { "", "", "" },
			new MultiString("").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "", "" },
			new MultiString("ABC").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "", "" },
			new MultiString("ABC[nl][nl]").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "", "" },
			new MultiString("ABC[nl][np]").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "", "", "DEF",
			"", "" }, new MultiString("ABC[nl][np]DEF")
			.getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "", "", "DEF",
			"", "" }, new MultiString("ABC[nl][np]DEF[np]")
			.getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "GHI" },
			new MultiString("ABC[nl]DEF[nl2]GHI").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "GHI",
			"JKL" }, new MultiString("ABC[nl]DEF[np]GHI[nl]JKL")
			.getLines(2)));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "",
			"GHI", "JKL", "" }, new MultiString(
			"ABC[nl]DEF[np]GHI[nl]JKL").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "GHI",""},
			new MultiString("ABC[nl]DEF[np]GHI").getLines(2)));
		assertTrue(Arrays.equals(new String[] { "ABC", "DEF", "",
			"GHI", "", ""}, new MultiString("ABC[nl]DEF[np]GHI")
			.getLines(3)));
		assertTrue(Arrays.equals(new String[]{"[jl2]ABC","DEF",""},
			new MultiString("[jl2]ABC[nl]DEF[g1,1,1]").getLines(3)));
		assertTrue(Arrays.equals(new String[] { "[cf0,0,0]ABC",
			"DE[sc5]F"},new MultiString("[cf0,0,0]ABC[nl]DE[sc5]F")
			.getLines(2)));
		// Test for non-line tags being stripped
		assertTrue(Arrays.equals(new String[] { "ABC", "" },
		       new MultiString("[pb0,0,0]ABC").getLines(2)));
		assertTrue(Arrays.equals(new String[] { "ABC", "" },
		       new MultiString("[cr255,0,0]ABC").getLines(2)));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[g1,0,0]ABC").getLines(1)));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[jp3]ABC").getLines(1)));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[pt50o0]ABC").getLines(1)));
		assertTrue(Arrays.equals(new String[] { "ABC" },
		       new MultiString("[tr0,0,5,5]ABC").getLines(1)));
		assertTrue(Arrays.equals(new String[] { "ABC [jl4]DEF" },
		       new MultiString("[jp3]ABC [jl4]DEF").getLines(1)));
	}

	public void testGetNumPages() {
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

	public void testGetText() {
		// Single page tests
		checkGetText("", 3, new String[0]);
		checkGetText("ABC", 3, new String[] { "ABC" });
		checkGetText("ABC[nl]", 3, new String[] { "ABC" });
		checkGetText("ABC[nl][nl]", 3, new String[] { "ABC" });
		checkGetText("ABC[nl]DEF", 3, new String[] { "ABC", "DEF" });
		checkGetText("ABC[nl]DEF", 1, new String[] { "ABC" });
		checkGetText("ABC[nl]DEF[nl]GHI", 3,
			new String[] {"ABC", "DEF", "GHI"});
		checkGetText("ABC[nl]DEF[nl]GHI[nl]JKL", 3,
			new String[] {"ABC", "DEF", "GHI"});
		// Multi-page tests
		checkGetText("ABC[np]", 3, new String[] { "ABC" });
		checkGetText("ABC[np][nl]", 3,
			new String[] { "ABC" });
		checkGetText("ABC[nl][nl]DEF[np][nl]", 3,
			new String[] {"ABC", "", "DEF"});
		checkGetText("ABC[np]DEF", 3,
			new String[] { "ABC", "", "", "DEF" });
		checkGetText("ABC[nl][np]DEF", 3,
			new String[] { "ABC", "", "", "DEF" });
		checkGetText("ABC[nl][np]DEF[np]", 3,
			new String[] { "ABC", "", "", "DEF" });
		checkGetText("ABC[nl]DEF[np]GHI[nl][nl]123", 3,
			new String[] { "ABC", "DEF", "", "GHI", "", "123" });
		checkGetText("ABC[nl]DEF[np]GHI[nl][nl]123", 2,
			new String[] { "ABC", "DEF", "GHI" });
		checkGetText("ABC[nl]DEF[np]GHI[nl]JKL[nl]123", 2,
			new String[] { "ABC", "DEF", "GHI", "JKL" });
		// tags contained in a span
		checkGetText("ABC[nl]D[j1x]E[j1x]F[nl]GHI", 3,
			new String[] {"ABC", "D E F", "GHI"});
		checkGetText("ABC[nl][jl2]D[jl3]E[jl4]F[nl]GHI", 3,
			new String[] {"ABC", "D E F", "GHI"});
		checkGetText("[fo1]ABC", 3, new String[] {"ABC" });
	}

	private void checkGetText(String multi, int n_lines, String[] text) {
		assertTrue(Arrays.equals(new MultiString(multi).getText(
			n_lines), text));
	}

	public void testEquals() {
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

	public void testNormalize() {
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
			equals("AB|C"));
		assertTrue(MultiString.normalize("AB|{}{}C{}").
			equals("AB|{}{}C{}"));
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
		assertTrue(MultiString.normalize("[pb0,128,255]").
			equals("[pb0,128,255]"));
		assertTrue(MultiString.normalize("[ttS100]").
			equals("[ttS100]"));
	}

	public void testPageOnTime() {

		// test page time specified once for entire message
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(new Interval(0)));
		Interval defspg = PageTimeHelper.defaultPageOnInterval(true);
		Interval defmpg = PageTimeHelper.defaultPageOnInterval(false);
		assertTrue(new MultiString("").
			pageOnInterval().equals(defspg));
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(defspg));
		assertTrue(new MultiString("ABC[np]DEF").
			pageOnInterval().equals(defmpg));
		assertTrue(new MultiString("[pt13o0]ABC[nl]DEF").
			pageOnInterval().round(DECISECONDS) == 13);
		assertTrue(new MultiString("ABC[nl][pt14o]DEF").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[nl]DEF[pt14o]").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[np][pt14o]DEF").
			pageOnInterval().equals(defmpg));
		assertTrue(new MultiString("ABC[np]DEF[pt14o]").
			pageOnInterval().equals(defmpg));
	}

	public void testPageOnIntervals() {
		// Single page tests
		checkPageOn("", 0, new int[] { 0 });
		checkPageOn("", 999, new int[] { 999 });
		checkPageOn("[pto]", 10, new int[] { 10 });
		checkPageOn("[pt5o]", 10, new int[] { 5 });
		checkPageOn("[pto5]", 10, new int[] { 10 });
		checkPageOn("ABC", 10, new int[] { 10 });
		checkPageOn("[pto]ABC", 10, new int[] { 10 });
		checkPageOn("[pt5o]ABC", 10, new int[] { 5 });
		checkPageOn("[pto5]ABC", 10, new int[] { 10 });
		checkPageOn("ABC[pto]", 10, new int[] { 10 });
		checkPageOn("ABC[pt5o]", 10, new int[] { 5 });
		checkPageOn("ABC[pto5]", 10, new int[] { 10 });
		checkPageOn("ABC[nl][pto]123", 10, new int[] { 10 });
		checkPageOn("ABC[nl]123[pto]", 10, new int[] { 10 });
		checkPageOn("ABC[nl][pt5o]123", 10, new int[] { 5 });
		checkPageOn("ABC[nl]123[pt5o]", 10, new int[] { 5 });
		checkPageOn("ABC[nl][pto5]123", 10, new int[] { 10 });
		checkPageOn("ABC[nl]123[pto5]", 10, new int[] { 10 });
		// Two page tests
		checkPageOn("[np]", 8, new int[] { 8, 8 });
		checkPageOn("[pto][np]", 8, new int[] { 8, 8 });
		checkPageOn("[np][pto]", 8, new int[] { 8, 8 });
		checkPageOn("[pt7o][np]", 8, new int[] { 7, 7 });
		checkPageOn("[pt4o][np]", 8, new int[] { 4, 4 });
		checkPageOn("[np][pt7o]", 8, new int[] { 8, 7 });
		checkPageOn("[pt7o][np][pto]", 8, new int[] { 7, 8 });
		checkPageOn("ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOn("[pto]ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOn("ABC[np][pto]123", 8, new int[] { 8, 8 });
		checkPageOn("[pt7o]ABC[np]123", 8, new int[] { 7, 7 });
		checkPageOn("[pt4o]ABC[np]123", 8, new int[] { 4, 4 });
		checkPageOn("ABC[np][pt7o]123", 8, new int[] { 8, 7 });
		checkPageOn("[pto]ABC[np]123[pt7o]", 8, new int[] { 8, 7 });
		checkPageOn("[pt7o]ABC[np][pto]123", 8, new int[] { 7, 8 });
		checkPageOn("[pt7o]ABC[np]123[pto]", 8, new int[] { 7, 8 });
		// Three page tests
		checkPageOn("PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOn("[pto]PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOn("[pt7o4]PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 7, 8, 8 });
		checkPageOn("PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 10, 8, 8 });
		checkPageOn("PG1[np][pt8o4]PG2[np][pto]PG3", 10,
			new int[] { 10, 8, 10 });
	}

	private void checkPageOn(String ms, int dflt_ds, int[] intvls) {
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		Interval[] t = new MultiString(ms).pageOnIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for(int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
	}

	public void testPageOffIntervals() {
		// Single page tests
		checkPageOff("", 0, new int[] { 0 });
		checkPageOff("", 999, new int[] { 999 });
		checkPageOff("[pto]", 10, new int[] { 10 });
		checkPageOff("[pt5o]", 10, new int[] { 10 });
		checkPageOff("[pto5]", 10, new int[] { 5 });
		checkPageOff("ABC", 10, new int[] { 10 });
		checkPageOff("[pto]ABC", 10, new int[] { 10 });
		checkPageOff("[pt5o]ABC", 10, new int[] { 10 });
		checkPageOff("[pto5]ABC", 10, new int[] { 5 });
		checkPageOff("ABC[pto]", 10, new int[] { 10 });
		checkPageOff("ABC[pt5o]", 10, new int[] { 10 });
		checkPageOff("ABC[pto5]", 10, new int[] { 5 });
		checkPageOff("ABC[nl][pto]123", 10, new int[] { 10 });
		checkPageOff("ABC[nl]123[pto]", 10, new int[] { 10 });
		checkPageOff("ABC[nl][pt5o]123", 10, new int[] { 10 });
		checkPageOff("ABC[nl]123[pt5o]", 10, new int[] { 10 });
		checkPageOff("ABC[nl][pto5]123", 10, new int[] { 5 });
		checkPageOff("ABC[nl]123[pto5]", 10, new int[] { 5 });
		// Two page tests
		checkPageOff("[np]", 8, new int[] { 8, 8 });
		checkPageOff("[pto][np]", 8, new int[] { 8, 8 });
		checkPageOff("[np][pto]", 8, new int[] { 8, 8 });
		checkPageOff("[pto7][np]", 8, new int[] { 7, 7 });
		checkPageOff("[pto4][np]", 8, new int[] { 4, 4 });
		checkPageOff("[np][pto7]", 8, new int[] { 8, 7 });
		checkPageOff("[pto7][np][pto]", 8, new int[] { 7, 8 });
		checkPageOff("ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOff("[pto]ABC[np]123", 8, new int[] { 8, 8 });
		checkPageOff("ABC[np][pto]123", 8, new int[] { 8, 8 });
		checkPageOff("[pto7]ABC[np]123", 8, new int[] { 7, 7 });
		checkPageOff("[pto4]ABC[np]123", 8, new int[] { 4, 4 });
		checkPageOff("ABC[np][pto7]123", 8, new int[] { 8, 7 });
		checkPageOff("[pto]ABC[np]123[pto7]", 8, new int[] { 8, 7 });
		checkPageOff("[pto7]ABC[np][pto]123", 8, new int[] { 7, 8 });
		checkPageOff("[pto7]ABC[np]123[pto]", 8, new int[] { 7, 8 });
		// Three page tests
		checkPageOff("PG1[np]PG2[np]PG3", 6, new int[] { 6, 6, 6 });
		checkPageOff("[pto]PG1[np]PG2[np]PG3", 6,
			new int[] { 6, 6, 6 });
		checkPageOff("[pt7o4]PG1[np][pt8o6]PG2[np]PG3", 10,
			new int[] { 4, 6, 6 });
		checkPageOff("PG1[np][pt8o4]PG2[np]PG3", 10,
			new int[] { 10, 4, 4 });
		checkPageOff("PG1[np][pt8o4]PG2[np][pto]PG3", 10,
			new int[] { 10, 4, 10 });
	}

	private void checkPageOff(String ms, int dflt_ds, int[] intvls) {
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		Interval[] t = new MultiString(ms).pageOffIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for(int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
	}

	public void testReplacePageOnTime() {
		checkReplacePageTimes("YA1[np]YA2",
		                      "[pt4o]YA1[np]YA2",
		                      7, 4, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o]YA1[np]OH YA2",
		                      "[pt4o]YA1[np]OH YA2",
		                      7, 4, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np]OH YA2",
		                      "[pt4o50]YA1[np]OH YA2",
		                      7, 4, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np][pt22o60]OH YA2",
		                      "[pt4o50]YA1[np][pt4o60]OH YA2",
		                      7, 4, new int[] { 4, 4 });
	}

	private void checkReplacePageTimes(String ms, String cms, int dflt_ds,
		int pot, int[] intvls)
	{
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		MultiString ms1 = new MultiString(ms);
		MultiString ms2 = ms1.replacePageOnTime(pot);
		Interval[] t = ms2.pageOnIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for(int i = 0; i < t.length; i++) {
			Interval val = new Interval(intvls[i], DECISECONDS);
			assertTrue(t[i].equals(val));
		}
		assertTrue(cms.equals(ms2.toString()));
	}

	public void testGetFonts() {
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

	public void testEtc() {
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
}
