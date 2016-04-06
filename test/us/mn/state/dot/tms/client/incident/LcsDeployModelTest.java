/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import junit.framework.TestCase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneUseIndication;
import static us.mn.state.dot.tms.LaneUseIndication.DARK;
import static us.mn.state.dot.tms.LaneUseIndication.LANE_OPEN;
import static us.mn.state.dot.tms.LaneUseIndication.LANE_CLOSED;
import static us.mn.state.dot.tms.LaneUseIndication.USE_CAUTION;
import us.mn.state.dot.tms.units.Distance;

/** 
 * LcsDeployModel test cases
 *
 * @author Douglas Lau
 */
public class LcsDeployModelTest extends TestCase {

	public LcsDeployModelTest(String name) {
		super(name);
	}

	public void testUpstreamShort() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LcsDeployModel p = createModel("....");
		assertTrue(getIndications(p, up, 2, 0)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1)[0] == DARK);
		assertTrue(getIndications(p, up, 2, 1)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[1] == DARK);
		p = createModel("...!");
		assertTrue(getIndications(p, up, 2, 0)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1)[0] == DARK);
		assertTrue(getIndications(p, up, 2, -1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[1] == DARK);
		p = createModel("..?.");
		assertTrue(getIndications(p, up, 2, 0)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_OPEN);
		p = createModel("..!.");
		assertTrue(getIndications(p, up, 2, 0)[0] == LANE_CLOSED);
		assertTrue(getIndications(p, up, 2, 0)[1] == USE_CAUTION);
		p = createModel(".?..");
		assertTrue(getIndications(p, up, 2, 0)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 0)[1] == USE_CAUTION);
		p = createModel(".!..");
		assertTrue(getIndications(p, up, 2, 0)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_CLOSED);
	}

	private LaneUseIndication[] getIndications(LcsDeployModel p,
		Distance up, int n_lcs, int shift)
	{
		return p.createIndications(up, n_lcs, shift);
	}

	protected LcsDeployModel createModel(String impact) {
		LaneConfiguration config = new LaneConfiguration(0, 2);
		return new LcsDeployModel(createIncident(impact), config);
	}

	protected ClientIncident createIncident(String impact) {
		return new ClientIncident(null,
			EventType.INCIDENT_CRASH.ordinal(), null, (short)1,
			null, (short)1, 0, 0, impact);
	}
}
