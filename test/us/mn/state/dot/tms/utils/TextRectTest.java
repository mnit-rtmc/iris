/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2024  Minnesota Department of Transportation
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
import java.util.List;
import junit.framework.TestCase;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * @author Douglas Lau
 */
public class TextRectTest extends TestCase {

	// page size for 3 lines of text
	final TextRect tr3 = new TextRect(1, 1, 1, 50, 26, 0, 1, true);

	public TextRectTest(String name) {
		super(name);
	}

	public void testFind1() {
		List<TextRect> rects = tr3.find("");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(tr3));
	}

	public void testFind2() {
		List<TextRect> rects = tr3.find("TEXT");
		assertTrue(rects.size() == 0);
	}

	public void testFind3() {
		List<TextRect> rects = tr3.find("[np]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 26, 0, 1, true)));
		assertTrue(rects.get(1).equals(new TextRect(2, 1, 1, 50, 26, 0, 1, true)));
	}

	public void testFind4() {
		List<TextRect> rects = tr3.find("FIRST[np]");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(2, 1, 1, 50, 26, 0, 1, true)));
	}

	public void testFind5() {
		List<TextRect> rects = tr3.find("[np]SECOND");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 26, 0, 1, true)));
	}

	public void testFind6() {
		List<TextRect> rects = tr3.find("[np][np]");
		assertTrue(rects.size() == 3);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 26, 0, 1, true)));
		assertTrue(rects.get(1).equals(new TextRect(2, 1, 1, 50, 26, 0, 1, true)));
		assertTrue(rects.get(2).equals(new TextRect(3, 1, 1, 50, 26, 0, 1, true)));
	}

	public void testFind7() {
		List<TextRect> rects = tr3.find("[tr1,1,50,24]");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 24, 0, 1, false)));
	}

	public void testFind8() {
		List<TextRect> rects = tr3.find("[tr1,1,50,24]TEXT");
		assertTrue(rects.size() == 0);
	}

	public void testFind9() {
		List<TextRect> rects = tr3.find("[tr1,1,50,24][tr1,25,50,24]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 24, 0, 1, false)));
		assertTrue(rects.get(1).equals(new TextRect(1, 1, 25, 50, 24, 0, 1, false)));
	}

	public void testFind10() {
		List<TextRect> rects = tr3.find(
			"[tr1,1,50,24][fo2][tr1,25,50,24]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 24, 0, 1, false)));
		assertTrue(rects.get(1).equals(new TextRect(1, 1, 25, 50, 24, 0, 2, false)));
	}

	public void testFind11() {
		List<TextRect> rects = tr3.find(
			"[tr1,1,50,24][fo2][tr1,25,50,24][fo3][np]");
		assertTrue(rects.size() == 3);
		assertTrue(rects.get(0).equals(new TextRect(1, 1, 1, 50, 24, 0, 1, false)));
		assertTrue(rects.get(1).equals(new TextRect(1, 1, 25, 50, 24, 0, 2, false)));
		assertTrue(rects.get(2).equals(new TextRect(2, 1, 1, 50, 26, 0, 3, true)));
	}

	public void testFillFail() {
		assertTrue("TEXT".equals(
			tr3.fill("TEXT", Arrays.asList("ABC"))));
		assertTrue("FIRST[np]ABC[nl][nl]".equals(
			tr3.fill("FIRST[np]", Arrays.asList("ABC"))));
		assertTrue("ABC[nl]DEF[nl]GHI".equals(tr3.fill("",
			Arrays.asList("ABC", "DEF", "GHI", "JKL"))));
		assertTrue("[tr1,1,50,24]TEXT".equals(tr3.fill(
			"[tr1,1,50,24]TEXT",
			Arrays.asList("ABC"))));
	}

	private void checkSplit(String multi, List<String> lines) {
		List<String> lns = tr3.splitLines("", multi);
		assertTrue(lines.equals(lns));
	}

	public void testSplitWithTags() {
		// new line tags with spacing
		checkSplit("ABC[nl3]DEF", Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl]DEF[nl2]GHI",
			Arrays.asList("ABC", "DEF", "GHI"));
		// invalid tags
		checkSplit("ABC[nl]D[j1x]E[j1x]F[nl]GHI",
			Arrays.asList("ABC", "DEF", "GHI"));
		// line justification tags
		checkSplit("ABC[nl][jl2]D[jl3]E[jl4]F[nl]GHI",
			Arrays.asList("ABC", "[jl2]D[jl3]E[jl4]F", "GHI"));
		// character spacing tags
		checkSplit("ABC[sc3]DEF",
			Arrays.asList("ABC[sc3]DEF", "", ""));
		checkSplit("ABC[sc3]DEF[/sc]GHI",
			Arrays.asList("ABC[sc3]DEF[/sc]GHI", "", ""));
		// Test for non-line tags being stripped
		checkSplit("[cb8]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[pb0,0,0]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[cr255,0,0]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[fo1]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[g1,0,0]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[jp3]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("[pt50o0]ABC", Arrays.asList("ABC", "", ""));
		checkSplit("ABC[nl][cb8]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][pb0,0,0]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][cr255,0,0]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][fo1]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][g1,0,0]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][jp3]DEF",
			Arrays.asList("ABC", "DEF", ""));
		checkSplit("ABC[nl][pt50o0]DEF",
			Arrays.asList("ABC", "DEF", ""));
		// mixed line and non-line tags
		checkSplit("[jp3]ABC[jl4]DEF",
			Arrays.asList("ABC[jl4]DEF", "", ""));
		checkSplit("[jl2]ABC[nl]DEF[g1,1,1]",
			Arrays.asList("[jl2]ABC", "DEF", ""));
		checkSplit("[cf0,0,0]ABC[nl]DE[sc5]F",
			Arrays.asList("[cf0,0,0]ABC", "DE[sc5]F", ""));
	}

	// round-trip test filling and splitting
	private void fillSplit(String pat_ms, List<String> lines,
		String multi)
	{
		String ms = tr3.fill(pat_ms, lines);
		assertTrue(multi.equals(ms));
		List<String> lns = tr3.splitLines(pat_ms, multi);
		assertTrue(lines.equals(lns));
	}

	public void testFillSplit() {
		fillSplit("", Arrays.asList("", "", ""), "[nl][nl]");
		fillSplit("[nl]", Arrays.asList(), "[nl]");
		fillSplit("", Arrays.asList("ABC", "", ""), "ABC[nl][nl]");
		fillSplit("", Arrays.asList("ABC", "123", ""),
			"ABC[nl]123[nl]");
		fillSplit("", Arrays.asList("", "ABC", ""),
			"[nl]ABC[nl]");
		fillSplit("", Arrays.asList("", "", "ABC"),
			"[nl][nl]ABC");
		fillSplit("", Arrays.asList("ABC", "123", "DEF"),
			"ABC[nl]123[nl]DEF");
		fillSplit("", Arrays.asList("[jl2]ABC", "", ""),
			"[jl2]ABC[nl][nl]");
		fillSplit("",
			Arrays.asList("CRASH", "AT MAIN ST", "USE CAUTION"),
			"CRASH[nl]AT MAIN ST[nl]USE CAUTION");
		fillSplit("[feedL0000]",
			Arrays.asList("SNOWPLOW", "AHEAD", "USE CAUTION"),
			"SNOWPLOW[nl]AHEAD[nl]USE CAUTION");
	}

	public void testFillSplitTr() {
		fillSplit("[tr1,1,50,8]", Arrays.asList("ABC"),
			"[tr1,1,50,8]ABC");
		fillSplit("ABC[tr1,1,50,8]", Arrays.asList("DEF"),
			"ABC[tr1,1,50,8]DEF");
		fillSplit("[tr1,1,50,8][tr1,10,50,8]",
			Arrays.asList("ABC", "123"),
			"[tr1,1,50,8]ABC[tr1,10,50,8]123"
		);
		fillSplit("[tr1,1,50,8][fo2][tr1,10,50,8]",
			Arrays.asList("ABC", "123"),
			"[tr1,1,50,8]ABC[fo2][tr1,10,50,8]123"
		);
		fillSplit("[g1][tr1,1,50,8]",
			Arrays.asList("ABC"),
			"[g1][tr1,1,50,8]ABC"
		);
	}

	public void testFillSplitNp() {
		fillSplit("[np]", Arrays.asList("", "", "", "", "", ""),
			"[nl][nl][np][nl][nl]");
		fillSplit("[np]", Arrays.asList("ABC", "", "", "", "", ""),
			"ABC[nl][nl][np][nl][nl]");
		fillSplit("[np]", Arrays.asList("ABC", "", "", "123", "", ""),
			"ABC[nl][nl][np]123[nl][nl]");
		fillSplit("[np][np]",
			Arrays.asList("","","ABC","","","DEF","","","GHI"),
			"[nl][nl]ABC[np][nl][nl]DEF[np][nl][nl]GHI");
		fillSplit("[np]123", Arrays.asList("ABC", "", ""),
			"ABC[nl][nl][np]123");
		fillSplit("123[np]", Arrays.asList("", "ABC", ""),
			"123[np][nl]ABC[nl]");
		fillSplit("[fo2][np]",
			Arrays.asList("ABC", "DEF", "", "", "123", "456"),
			"ABC[nl]DEF[nl][fo2][np][nl]123[nl]456"
		);
	}

	public void testFillSplitTrNp() {
		fillSplit("[fo2][np][tr1,1,50,8]",
			Arrays.asList("ABC", "DEF", "XYZ", "123"),
			"ABC[nl]DEF[nl]XYZ[fo2][np][tr1,1,50,8]123"
		);
		fillSplit("[fo2][tr1,1,50,8][fo3][np]",
			Arrays.asList("ABC", "123", "XYZ", ""),
			"[fo2][tr1,1,50,8]ABC[fo3][np]123[nl]XYZ[nl]"
		);
		fillSplit("[fo2][tr1,1,50,8][fo3][tr1,10,50,8][np]",
			Arrays.asList("ABC", "123", "XYZ", "", ""),
			"[fo2][tr1,1,50,8]ABC[fo3][tr1,10,50,8]123[np]XYZ[nl][nl]"
		);
	}
}
