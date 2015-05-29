/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import junit.framework.TestCase;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;

public class NameTest extends TestCase {

	public void testSize() {
		ASN1Integer i = dmsSignHeight.makeInt();
		assertTrue(i.getName().equals("dmsSignHeight"));
		i.setInteger(37);
		assertTrue(i.toString().equals("dmsSignHeight.0: 37"));
		i = dmsSignWidth.makeInt();
		assertTrue(i.getName().equals("dmsSignWidth"));
		i.setInteger(73);
		assertTrue(i.toString().equals("dmsSignWidth.0: 73"));
	}

	public void testCRC() {
		ASN1Integer i = dmsMessageCRC.makeInt(
			DmsMessageMemoryType.changeable, 2);
		assertTrue(i.getName().equals("dmsMessageCRC"));
		i.setInteger(1234);
		assertTrue(i.toString().equals("dmsMessageCRC.3.2: 1234"));
	}
}
