/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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

import java.util.List;
import junit.framework.TestCase;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * @author Douglas Lau
 */
public class TextRectTest extends TestCase {

	public TextRectTest(String name) {
		super(name);
	}

	public void testFind1() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 50, 1)));
	}

	public void testFill1() {
		assertTrue("ABC".equals(TextRect.fill("",
			 new String[] { "ABC" }
		)));
	}

	public void testFill1b() {
		assertTrue("ABC[nl]123".equals(TextRect.fill("",
			 new String[] { "ABC[nl]123" }
		)));
	}

	public void testFill1c() {
		assertTrue("[jl2]ABC".equals(TextRect.fill("",
			 new String[] { "[jl2]ABC" }
		)));
	}

	public void testFind2() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "TEXT");
		assertTrue(rects.size() == 0);
	}

	public void testFill2() {
		assertTrue("TEXT".equals(TextRect.fill("TEXT",
			 new String[] { "ABC" }
		)));
	}

	public void testFind3() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "[np]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 50, 1)));
		assertTrue(rects.get(1).equals(new TextRect(2, 50, 50, 1)));
	}

	public void testFill3() {
		assertTrue("[np]".equals(TextRect.fill("[np]",
			 new String[] {}
		)));
	}

	public void testFill3a() {
		assertTrue("ABC[np]".equals(TextRect.fill("[np]",
			 new String[] { "ABC" }
		)));
	}

	public void testFill3b() {
		assertTrue("ABC[np]123".equals(TextRect.fill("[np]",
			 new String[] { "ABC", "123" }
		)));
	}

	public void testFind4() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "FIRST[np]");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(2, 50, 50, 1)));
	}

	public void testFill4() {
		assertTrue("FIRST[np]ABC".equals(TextRect.fill(
			"FIRST[np]",
			 new String[] { "ABC" }
		)));
	}

	public void testFind5() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "[np]SECOND");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 50, 1)));
	}

	public void testFill5() {
		assertTrue("ABC[np]SECOND".equals(TextRect.fill(
			"[np]SECOND",
			 new String[] { "ABC" }
		)));
	}

	public void testFind6() {
		List<TextRect> rects = TextRect.find(50, 50, 1, "[np][np]");
		assertTrue(rects.size() == 3);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 50, 1)));
		assertTrue(rects.get(1).equals(new TextRect(2, 50, 50, 1)));
		assertTrue(rects.get(2).equals(new TextRect(3, 50, 50, 1)));
	}

	public void testFill6() {
		assertTrue("ABC[np]123[np]XYZ".equals(TextRect.fill(
			"[np][np]",
			 new String[] { "ABC", "123", "XYZ" }
		)));
	}

	public void testFind7() {
		List<TextRect> rects = TextRect.find(50, 50, 1,
			"[tr1,1,50,24]");
		assertTrue(rects.size() == 1);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 24, 1)));
	}

	public void testFill7() {
		assertTrue("[tr1,1,50,24]ABC".equals(TextRect.fill(
			"[tr1,1,50,24]",
			new String[] { "ABC" }
		)));
	}

	public void testFind8() {
		List<TextRect> rects = TextRect.find(50, 50, 1,
			"[tr1,1,50,24]TEXT");
		assertTrue(rects.size() == 0);
	}

	public void testFill8() {
		assertTrue("[tr1,1,50,24]TEXT".equals(TextRect.fill(
			"[tr1,1,50,24]TEXT",
			new String[] { "ABC" }
		)));
	}

	public void testFind9() {
		List<TextRect> rects = TextRect.find(50, 50, 1,
			"[tr1,1,50,24][tr1,25,50,24]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 24, 1)));
		assertTrue(rects.get(1).equals(new TextRect(1, 50, 24, 1)));
	}

	public void testFill9() {
		assertTrue("[tr1,1,50,24]ABC[tr1,25,50,24]123".equals(
			TextRect.fill(
				"[tr1,1,50,24][tr1,25,50,24]",
				new String[] { "ABC", "123" }
			)
		));
	}

	public void testFind10() {
		List<TextRect> rects = TextRect.find(50, 50, 1,
			"[tr1,1,50,24][tr1,25,50,24][fo2]");
		assertTrue(rects.size() == 2);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 24, 1)));
		assertTrue(rects.get(1).equals(new TextRect(1, 50, 24, 2)));
	}

	public void testFill10() {
		assertTrue("[tr1,1,50,24][fo2]ABC[tr1,25,50,24]123".equals(
			TextRect.fill(
				"[tr1,1,50,24][fo2][tr1,25,50,24]",
				new String[] { "ABC", "123" }
			)
		));
	}

	public void testFind11() {
		List<TextRect> rects = TextRect.find(50, 50, 1,
			"[tr1,1,50,24][tr1,25,50,24][fo2][np][fo3]");
		assertTrue(rects.size() == 3);
		assertTrue(rects.get(0).equals(new TextRect(1, 50, 24, 1)));
		assertTrue(rects.get(1).equals(new TextRect(1, 50, 24, 2)));
		assertTrue(rects.get(2).equals(new TextRect(2, 50, 50, 3)));
	}

	public void testFill11() {
		assertTrue("[tr1,1,50,24][fo2]ABC[tr1,25,50,24][fo3]123[np]XYZ".equals(
			TextRect.fill(
				"[tr1,1,50,24][fo2][tr1,25,50,24][fo3][np]",
				new String[] { "ABC", "123", "XYZ" }
			)
		));
	}
}
