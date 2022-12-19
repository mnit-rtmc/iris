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

import junit.framework.TestCase;
import us.mn.state.dot.tms.MsgPatternHelper;
import static us.mn.state.dot.tms.MsgPatternHelper.fillTextRectangles;
import static us.mn.state.dot.tms.MsgPatternHelper.findTextRectangles;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * @author Douglas Lau
 */
public class MsgPatternHelperTest extends TestCase {

	static class SignConfigMock implements SignConfig {
		public String getTypeName() { return SONAR_TYPE; }
		public String getName() { return "name"; }
		public void destroy() {}
		public int getFaceWidth() { return 0; }
		public int getFaceHeight() { return 0; }
		public int getBorderHoriz() { return 0; }
		public int getBorderVert() { return 0; }
		public int getPitchHoriz() { return 0; }
		public int getPitchVert() { return 0; }
		public int getPixelWidth() { return 50; }
		public int getPixelHeight() { return 50; }
		public int getCharWidth() { return 0; }
		public int getCharHeight() { return 0; }
		public int getMonochromeForeground() { return 0; }
		public int getMonochromeBackground() { return 0; }
		public int getColorScheme() { return 4; }
		public void setDefaultFont(Font f) {}
		public Font getDefaultFont() { return null; }
		public int getModuleWidth() { return 0; }
		public void setModuleWidth(int mw) {}
		public int getModuleHeight() { return 0; }
		public void setModuleHeight(int mh) {}
	}

	static class MsgPatternMock implements MsgPattern {
		private SignConfig sc;
		private String multi = "";
		public String getTypeName() { return SONAR_TYPE; }
		public String getName() { return "name"; }
		public void destroy() {}
		public SignConfig getSignConfig() { return sc; }
		public void setSignConfig(SignConfig sc) { this.sc = sc; }
		public SignGroup getSignGroup() { return null; }
		public void setSignGroup(SignGroup sg) {}
		public int getMsgCombining() { return 0; }
		public void setMsgCombining(int mc) {}
		public String getMulti() { return multi; }
		public void setMulti(String multi) { this.multi = multi; }
	}

	public MsgPatternHelperTest(String name) {
		super(name);
	}

	public void testFindTextRectangles() {
		MsgPattern pat = new MsgPatternMock();
		pat.setSignConfig(new SignConfigMock());
		pat.setMulti("");
		TextRect[] rects =
			findTextRectangles(pat).toArray(new TextRect[0]);
		assertTrue(rects.length == 1);
		assertTrue(rects[0].equals(new TextRect(1, 50, 50, 1)));
		pat.setMulti("[np]");
		rects = findTextRectangles(pat).toArray(new TextRect[0]);
		assertTrue(rects.length == 2);
		assertTrue(rects[0].equals(new TextRect(1, 50, 50, 1)));
		assertTrue(rects[1].equals(new TextRect(2, 50, 50, 1)));
		pat.setMulti("[tr1,1,50,24]");
		rects = findTextRectangles(pat).toArray(new TextRect[0]);
		assertTrue(rects.length == 1);
		assertTrue(rects[0].equals(new TextRect(1, 50, 24, 1)));
		pat.setMulti("[tr1,1,50,24][fo2][tr1,25,50,24]");
		rects = findTextRectangles(pat).toArray(new TextRect[0]);
		assertTrue(rects.length == 2);
		assertTrue(rects[0].equals(new TextRect(1, 50, 24, 1)));
		assertTrue(rects[1].equals(new TextRect(1, 50, 24, 2)));
		pat.setMulti("[tr1,1,50,24][fo2][tr1,25,50,24][fo3][np]");
		rects = findTextRectangles(pat).toArray(new TextRect[0]);
		assertTrue(rects.length == 3);
		assertTrue(rects[0].equals(new TextRect(1, 50, 24, 1)));
		assertTrue(rects[1].equals(new TextRect(1, 50, 24, 2)));
		assertTrue(rects[2].equals(new TextRect(2, 50, 50, 3)));
	}

	public void testFillTextRectangles() {
		MsgPattern pat = new MsgPatternMock();
		pat.setMulti("");
		assertTrue("ABC".equals(fillTextRectangles(pat,
			 new String[] { "ABC" }
		)));
		assertTrue("ABC[nl]123".equals(fillTextRectangles(pat,
			 new String[] { "ABC[nl]123" }
		)));
		assertTrue("[jl2]ABC".equals(fillTextRectangles(pat,
			 new String[] { "[jl2]ABC" }
		)));
		pat.setMulti("[np]");
		assertTrue("ABC[np]".equals(fillTextRectangles(pat,
			 new String[] { "ABC" }
		)));
		assertTrue("ABC[np]123".equals(fillTextRectangles(pat,
			 new String[] { "ABC", "123" }
		)));
		pat.setMulti("[np][np]");
		assertTrue("ABC[np]123[np]XYZ".equals(fillTextRectangles(pat,
			 new String[] { "ABC", "123", "XYZ" }
		)));
		pat.setMulti("[np]TEST");
		assertTrue("ABC[np]TEST".equals(fillTextRectangles(pat,
			 new String[] { "ABC" }
		)));
		pat.setMulti("TEST[np]");
		assertTrue("TEST[np]ABC".equals(fillTextRectangles(pat,
			 new String[] { "ABC" }
		)));
		pat.setMulti("[tr1,1,50,24]");
		assertTrue("[tr1,1,50,24]ABC".equals(
			fillTextRectangles(pat, new String[] { "ABC" } )
		));
		pat.setMulti("[tr1,1,50,24]TEST");
		assertTrue("[tr1,1,50,24]TEST".equals(
			fillTextRectangles(pat, new String[] { "ABC" } )
		));
		pat.setMulti("[tr1,1,50,24][tr1,25,50,24]");
		assertTrue("[tr1,1,50,24]ABC[tr1,25,50,24]".equals(
			fillTextRectangles(pat, new String[] { "ABC" } )
		));
		assertTrue("[tr1,1,50,24]ABC[tr1,25,50,24]123".equals(
			fillTextRectangles(pat, new String[] { "ABC", "123" } )
		));
		pat.setMulti("[tr1,1,50,24][tr1,25,50,24][np]");
		assertTrue("[tr1,1,50,24]ABC[tr1,25,50,24]123[np]XYZ".equals(
			fillTextRectangles(pat, new String[] { "ABC", "123", "XYZ" } )
		));
	}
}
