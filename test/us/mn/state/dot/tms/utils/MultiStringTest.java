/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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

import java.util.Arrays;
import junit.framework.TestCase;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;

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
		checkAsText("ABC[fo1]DEF", "ABC DEF");
		checkAsText("ABC [fo1]DEF", "ABC DEF");
		checkAsText("ABC [sc4]DEF", "ABC DEF");
		checkAsText("ABC [sc4]DEF[/sc]", "ABC DEF");
		checkAsText("ABC[jl4]DEF", "ABC DEF");
		checkAsText("ABC[nl]DEF", "ABC DEF");
		checkAsText("ABC[nl][nl]DEF", "ABC DEF");
		checkAsText("ABC[np]DEF", "ABC DEF");
	}

	private void checkAsText(String m0, String m1) {
		assertTrue(new MultiString(m0).asText().equals(m1));
	}

	public void testGetNumPages() {
		checkGetNumPages("", 1);
		checkGetNumPages("ABC", 1);
		checkGetNumPages("ABC[nl][nl]", 1);
		checkGetNumPages("ABC[nl][nl]", 1);
		checkGetNumPages("ABC[nl][np]", 2);
		checkGetNumPages("ABC[nl][np]DEF", 2);
		checkGetNumPages("ABC[nl][np]DEF[np]", 3);
	}

	private void checkGetNumPages(String m, int p) {
		assertTrue(new MultiString(m).getNumPages() == p);
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
		assertTrue(new MultiString("[fo1]abc").equals(new MultiString(
			"[fo1]abc")));
	}

	public void testNormalize() {
		checkNormalize("01234567890", "01234567890");
		checkNormalize("ABC", "ABC");
		checkNormalize("abc", "abc");
		checkNormalize("DON'T", "DON'T");
		checkNormalize("SPACE SPACE", "SPACE SPACE");
		checkNormalize("AB|C", "AB|C");
		checkNormalize("AB|{}{}C{}", "AB|{}{}C{}");
		checkNormalize("!\"#$%&\'()*+,-./", "!\"#$%&\'()*+,-./");
		checkNormalize(":;<=>?@\\^_`{|}~", ":;<=>?@\\^_`{|}~");
		checkNormalize("\t\b\n\r\f", "");
		checkNormalize("[[", "[[");
		checkNormalize("]]", "]]");
		checkNormalize("[[NOT TAG]]", "[[NOT TAG]]");
		checkNormalize("[", "");
		checkNormalize("]", "");
		checkNormalize("[bad tag", "bad tag");
		checkNormalize("bad tag]", "bad tag");
		checkNormalize("bad[tag", "badtag");
		checkNormalize("bad]tag", "badtag");
		checkNormalize("bad[ tag[nl]", "bad");
		checkNormalize("bad ]tag[nl]", "bad tag[nl]");
		checkNormalize("ABC_DEF", "ABC_DEF");
		checkNormalize("ABC[bad]DEF", "ABCDEF");
		checkNormalize("ABC[nl]DEF", "ABC[nl]DEF");
		checkNormalize("ABC[nl3]DEF", "ABC[nl3]DEF");
		checkNormalize("ABC[np]DEF", "ABC[np]DEF");
		checkNormalize("ABC[jl4]DEF", "ABC[jl4]DEF");
		checkNormalize("ABC[jl6]DEF", "ABC[jl]DEF");
		checkNormalize("ABC[jp4]DEF", "ABC[jp4]DEF");
		checkNormalize("[fo3]ABC DEF", "[fo3]ABC DEF");
		checkNormalize("[fo3,beef]ABC DEF", "[fo3,beef]ABC DEF");
		checkNormalize("[g1]", "[g1]");
		checkNormalize("[g1_]", "");
		checkNormalize("[g1,5,5]", "[g1,5,5]");
		checkNormalize("[g1,5,5,beef]", "[g1,5,5,beef]");
		checkNormalize("[cf255,255,255]", "[cf255,255,255]");
		checkNormalize("[cf0,255,255]", "[cf0,255,255]");
		checkNormalize("[cf0,255,0]", "[cf0,255,0]");
		checkNormalize("[pto]", "[pto]");
		checkNormalize("[pt10o]", "[pt10o]");
		checkNormalize("[pt10o5]", "[pt10o5]");
		checkNormalize("[pto5]", "[pto5]");
		checkNormalize("ABC[sc3]DEF", "ABC[sc3]DEF");
		checkNormalize("ABC[sc3]DEF[/sc]GHI", "ABC[sc3]DEF[/sc]GHI");
		checkNormalize("[tr1,1,40,20]", "[tr1,1,40,20]");
		checkNormalize("[tr1,1,0,0]", "[tr1,1,0,0]");
		checkNormalize("[pb0,128,255]", "[pb0,128,255]");
		checkNormalize("[ttS100]", "[ttS100,prepend,OVER ]");
		checkNormalize("[feedL1]", "[feedL1]");
		checkNormalize("[feedL1_2]", "[feedL1_2]");
	}

	private void checkNormalize(String m0, String m1) {
		assertTrue(new MultiString(m0).normalize().equals(m1));
	}

	public void testNormalizeLine() {
		checkNormalizeLine("ABC", "ABC");
		checkNormalizeLine("[cb1]ABC", "ABC");
		checkNormalizeLine("[pb2]ABC", "ABC");
		checkNormalizeLine("[pb255,255,0]ABC", "ABC");
		checkNormalizeLine("[cf4]ABC", "[cf4]ABC");
		checkNormalizeLine("[cf128,128,128]ABC", "[cf128,128,128]ABC");
		checkNormalizeLine("[cr1,1,5,5,3]ABC", "ABC");
		checkNormalizeLine("[cr1,1,5,5,255,255,0]ABC", "ABC");
		checkNormalizeLine("[fo1]ABC", "ABC");
		checkNormalizeLine("[g1]ABC", "ABC");
		checkNormalizeLine("[jl2]ABC", "[jl2]ABC");
		checkNormalizeLine("[jp1]ABC", "ABC");
		checkNormalizeLine("[nl]ABC", "ABC");
		checkNormalizeLine("[nl2]ABC", "ABC");
		checkNormalizeLine("[np]ABC", "ABC");
		checkNormalizeLine("[pt5o2]ABC", "ABC");
		checkNormalizeLine("[sc4]ABC[/sc]", "[sc4]ABC[/sc]");
		checkNormalizeLine("[tr1,1,20,20]ABC", "ABC");
		checkNormalizeLine("[feedF0]ABC", "ABC");
	}

	private void checkNormalizeLine(String m0, String m1) {
		assertTrue(new MultiString(m0).normalizeLine().equals(m1));
	}

	public void testPageOnTime() {
		Interval df_pgon = PageTimeHelper.defaultPageOnInterval();
		// test page time specified once for entire message
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[nl]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[np]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("[pt13o0]ABC[nl]DEF").
			pageOnInterval().round(DECISECONDS) == 13);
		assertTrue(new MultiString("ABC[nl][pt14o]DEF").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[nl]DEF[pt14o]").
			pageOnInterval().round(DECISECONDS) == 14);
		assertTrue(new MultiString("ABC[np][pt14o]DEF").
			pageOnInterval().equals(df_pgon));
		assertTrue(new MultiString("ABC[np]DEF[pt14o]").
			pageOnInterval().equals(df_pgon));
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
		                      7, 4, null, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o]YA1[np]OH YA2",
		                      "[pt4o]YA1[np]OH YA2",
		                      7, 4, null, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np]OH YA2",
		                      "[pt4o50]YA1[np]OH YA2",
		                      7, 4, 50, new int[] { 4, 4 });
		checkReplacePageTimes("[pt3o50]YA1[np][pt22o60]OH YA2",
		                      "[pt4o50]YA1[np][pt4o50]OH YA2",
		                      7, 4, 50, new int[] { 4, 4 });
	}

	private void checkReplacePageTimes(String ms, String cms, int dflt_ds,
		int pot, Integer pof, int[] intvls)
	{
		Interval dflt = new Interval(dflt_ds, DECISECONDS);
		MultiString ms2 = new MultiString(new MultiString(ms)
			.replacePageTime(pot, pof));
		Interval[] t = ms2.pageOnIntervals(dflt);
		assertTrue(t.length == intvls.length);
		for (int i = 0; i < t.length; i++) {
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
		assertTrue(new MultiString("[fo10]ABC").
			getFonts(255).length == 1);
		assertTrue(new MultiString("[fo10]ABC").
			getFonts(255)[0] == 10);

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
		try {
			new MultiString(null);
			assertTrue(false);
		} catch (NullPointerException ex) {
			assertTrue(true);
		}

		assertTrue(new MultiString("").isValid());
		assertTrue(new MultiString("ABC").isValid());
		assertTrue(new MultiString("abc").isValid());
		assertTrue(new MultiString("0123456789").isValid());

		assertTrue(new MultiString("ABC[nl]DEF").isValid());
		assertTrue(new MultiString("ABC[nl1]DEF").isValid());

		assertTrue(new MultiString("ABC[fo]DEF").isValid());
		assertTrue(new MultiString("ABC[fo1]DEF").isValid());
		assertTrue(new MultiString("ABC[fo12]DEF").isValid());
		assertTrue(new MultiString("ABC[fo123]DEF").isValid());

		assertTrue(new MultiString("ABC[np]DEF").isValid());

		assertTrue(new MultiString("ABC[jp1]DEF").isValid());
		assertTrue(new MultiString("ABC[jp2]DEF").isValid());
		assertTrue(new MultiString("ABC[jp3]DEF").isValid());
		assertTrue(new MultiString("ABC[jp4]DEF").isValid());

		assertTrue(new MultiString("ABC[pto]").isValid());
		assertTrue(new MultiString("ABC[pt1o]").isValid());
		assertTrue(new MultiString("ABC[pt12o]").isValid());
		assertTrue(new MultiString("ABC[pt123o]").isValid());
		assertTrue(new MultiString("ABC[pto1]").isValid());
		assertTrue(new MultiString("ABC[pto12]").isValid());
		assertTrue(new MultiString("ABC[pto123]").isValid());

		assertTrue(new MultiString("[[").isValid());
		assertTrue(new MultiString("]]").isValid());
		assertTrue(new MultiString("!\"#$%&\'()*+,-./").isValid());
		assertTrue(new MultiString(":;<=>?@\\^_`{|}~").isValid());

		assertFalse(new MultiString("ABC[zzz]DEF").isValid());
		assertFalse(new MultiString("[").isValid());
		assertFalse(new MultiString("]").isValid());
		assertFalse(new MultiString("\t\b\n\r\f").isValid());
	}

	public void testBlank() {
		assertTrue(new MultiString("").isBlank());
		assertTrue(new MultiString(" ").isBlank());
		assertTrue(new MultiString("\t").isBlank());
		assertTrue(new MultiString("[nl]").isBlank());
		assertTrue(new MultiString("[np]").isBlank());
		assertTrue(new MultiString("[pt1o1]").isBlank());
		assertTrue(new MultiString("[jp2]").isBlank());
		assertTrue(new MultiString("[jl3]").isBlank());
		assertTrue(new MultiString("[fo2]").isBlank());
		assertTrue(new MultiString("[sc2]").isBlank());
		assertTrue(new MultiString("[tr1,1,20,20]").isBlank());

		assertFalse(new MultiString("A").isBlank());
		assertFalse(new MultiString("[g1]").isBlank());
		assertFalse(new MultiString("[pb0,1,2]").isBlank());
	}

	public void testStrip() {
		assertTrue(new MultiString("[fo3]ABC").stripFonts()
			.equals("ABC"));
		assertTrue(new MultiString("[pt50o0]ABC").stripPageTime()
			.equals("ABC"));
		assertTrue(new MultiString("[nl]").stripTrailingWhitespaceTags()
			.equals(""));
		assertTrue(new MultiString("ABC[nl]").stripTrailingWhitespaceTags()
			.equals("ABC"));
		assertTrue(new MultiString("ABC[nl][nl]").stripTrailingWhitespaceTags()
			.equals("ABC"));
		assertTrue(new MultiString("[nl]ABC[nl]").stripTrailingWhitespaceTags()
			.equals("[nl]ABC"));
		assertTrue(new MultiString("ABC[nl][np][nl]").stripTrailingWhitespaceTags()
			.equals("ABC"));
	}

	public void testTrailingTr() {
		assertTrue(new MultiString("")
			.trailingTextRectangle() == null);
		assertTrue(new MultiString("[tr1,1,2,2]ABC")
			.trailingTextRectangle() == null);
		assertTrue("[tr1,1,2,2]".equals(new MultiString("[tr1,1,2,2]")
			.trailingTextRectangle()));
		assertTrue("[tr1,1,2,2]".equals(new MultiString(
			"ABC[tr1,1,2,2]").trailingTextRectangle()));
	}

	public void testEachPageStartsWith() {
		assertFalse(new MultiString("")
			.eachPageStartsWith("[jl]"));
		assertTrue(new MultiString("[tr1,1,2,2]ABC")
			.eachPageStartsWith("[tr1,1,2,2]"));
		assertFalse(new MultiString("[tr1,1,2,2]ABC[np][tr2,2,2,2]123")
			.eachPageStartsWith("[tr1,1,2,2]"));
		assertTrue(new MultiString("[fo5]ABC[np][fo5]123")
			.eachPageStartsWith("[fo5]"));
		assertFalse(new MultiString("[fo5]ABC[np][fo5]123")
			.eachPageStartsWith("[fo5]_"));
	}

	public void testHasOneTextRectPerPage() {
		assertFalse(new MultiString("").hasOneTextRectPerPage());
		assertFalse(new MultiString("ABC").hasOneTextRectPerPage());
		assertTrue(new MultiString("[tr1,1,10,10]")
			.hasOneTextRectPerPage());
		assertTrue(new MultiString("ABC[tr1,1,10,10]123")
			.hasOneTextRectPerPage());
		assertFalse(new MultiString("[tr1,1,8,8]ABC[tr8,8,2,2]123")
			.hasOneTextRectPerPage());
		assertFalse(new MultiString("[tr1,1,8,8][np]")
			.hasOneTextRectPerPage());
		assertTrue(new MultiString("[tr1,1,8,8][np][tr1,1,5,5]")
			.hasOneTextRectPerPage());
		assertFalse(new MultiString("[tr1,1,4,4][np][tr4,4,2,2][np]")
			.hasOneTextRectPerPage());
		assertTrue(new MultiString("[tr1,1,2,2][np][tr3,3,2,2][np][tr6,6,2,2]")
			.hasOneTextRectPerPage());
	}
}
