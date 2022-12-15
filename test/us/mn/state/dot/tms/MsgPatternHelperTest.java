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

/**
 * @author Douglas Lau
 */
public class MsgPatternHelperTest extends TestCase {

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
		assertTrue(true);
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
