/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.units.Distance;

/** 
 * IncidentPolicy test cases
 *
 * @author Douglas Lau
 */
public class IncidentPolicyTest extends TestCase {

	static protected final int DARK = LaneUseIndication.DARK.ordinal();
	static protected final int LANE_OPEN =
		LaneUseIndication.LANE_OPEN.ordinal();
	static protected final int LANE_CLOSED =
		LaneUseIndication.LANE_CLOSED.ordinal();
	static protected final int USE_CAUTION =
		LaneUseIndication.USE_CAUTION.ordinal();

	public IncidentPolicyTest(String name) {
		super(name);
	}

	public void testUpstreamShort() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		IncidentPolicy p = createPolicy("....");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1, 2)[0] == DARK);
		assertTrue(getIndications(p, up, 2, 1, 2)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1, 2)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1, 2)[1] == DARK);
		p = createPolicy("...!");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1, 2)[0] == DARK);
		assertTrue(getIndications(p, up, 2, -1, 2)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1, 2)[1] == DARK);
		p = createPolicy("..?.");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == LANE_OPEN);
		p = createPolicy("..!.");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == LANE_CLOSED);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == USE_CAUTION);
		p = createPolicy(".?..");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == USE_CAUTION);
		p = createPolicy(".!..");
		assertTrue(getIndications(p, up, 2, 0, 2)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0, 2)[1] == LANE_CLOSED);
	}

	private Integer[] getIndications(IncidentPolicy p, Distance up,
		int n_lcs, int shift, int n_lanes)
	{
		return p.createIndications(up, n_lcs, shift, n_lanes);
	}

	protected IncidentPolicy createPolicy(String impact) {
		return new IncidentPolicy(createIncident(impact));
	}

	protected ClientIncident createIncident(String impact) {
		return new ClientIncident(null,
			EventType.INCIDENT_CRASH.ordinal(), null, (short)1,
			null, (short)1, 0, 0, impact);
	}
}
