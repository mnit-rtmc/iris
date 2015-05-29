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
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.ShortErrorStatus.*;

public class EnumTest extends TestCase {

	public void testLegend() {
		ASN1Enum<DmsLegend> legend = new ASN1Enum<DmsLegend>(
			DmsLegend.class, dmsLegend.node);
		legend.setEnum(DmsLegend.legendExists);
		assertTrue(legend.getEnum() == DmsLegend.legendExists);
		legend.setEnum(DmsLegend.noLegend);
		assertTrue(legend.getEnum() == DmsLegend.noLegend);
	}

	public void testIllumControl() {
		ASN1Enum<DmsIllumControl> ctrl = new ASN1Enum<DmsIllumControl>(
			DmsIllumControl.class, dmsIllumControl.node);
		ctrl.setEnum(DmsIllumControl.photocell);
		assertTrue(ctrl.getEnum() == DmsIllumControl.photocell);
		ctrl.setEnum(DmsIllumControl.timer);
		assertTrue(ctrl.getEnum() == DmsIllumControl.timer);
	}

	public void testShortError() {
		ASN1Flags<ShortErrorStatus> se =new ASN1Flags<ShortErrorStatus>(
			ShortErrorStatus.class, shortErrorStatus.node);
		se.setInteger(POWER.bit());
		checkOnly(se, POWER);
		se.setInteger(CRITICAL_TEMPERATURE.bit());
		checkOnly(se, CRITICAL_TEMPERATURE);
	}

	private void checkOnly(ASN1Flags<ShortErrorStatus> se,
		ShortErrorStatus s)
	{
		int v = se.getInteger();
		for (ShortErrorStatus ses: ShortErrorStatus.values()) {
			if (ses == s)
				assertTrue(ses.isSet(v));
			else
				assertFalse(ses.isSet(v));
		}
	}
}
