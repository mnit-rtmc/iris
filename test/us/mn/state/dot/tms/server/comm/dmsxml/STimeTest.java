/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.util.Calendar;
import java.util.Date;
import junit.framework.TestCase;

/**
 * Test cases
 * @author Michael Darter
 * @author Douglas Lau
 */
public class STimeTest extends TestCase {

	/** constructor */
	public STimeTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// STime.XMLtoDate
		Date d1 = STime.XMLtoDate("2008-04-29T15:37:22Z");
		assertTrue(d1.getTime() == 1209483442000L);

		// STime.CalendarToXML
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		String xml = STime.CalendarToXML(c);
		assertTrue(xml.equals("2008-04-29T15:37:22Z")); // UTM
	}
}
