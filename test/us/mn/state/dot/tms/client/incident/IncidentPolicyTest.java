/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneUseIndication;

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

	public void testUpstream1() {
		float up = 0.25f;
		IncidentPolicy p = createPolicy("....");
		assertTrue(getIndications(p, up, 2, 0)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[1] == LANE_OPEN);
		p = createPolicy("...!");
		assertTrue(getIndications(p, up, 2, 0)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, 1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[0] == LANE_OPEN);
		assertTrue(getIndications(p, up, 2, -1)[1] == LANE_OPEN);
		p = createPolicy("..!.");
		assertTrue(getIndications(p, up, 2, 0)[0] == LANE_CLOSED);
		assertTrue(getIndications(p, up, 2, 0)[1] == USE_CAUTION);
		p = createPolicy(".!..");
		assertTrue(getIndications(p, up, 2, 0)[0] == USE_CAUTION);
		assertTrue(getIndications(p, up, 2, 0)[1] == LANE_CLOSED);
	}

	protected Integer[] getIndications(IncidentPolicy p, float up,
		int n_lanes, int shift)
	{
		return p.createIndications(up, n_lanes, shift);
	}

	protected IncidentPolicy createPolicy(String impact) {
		return new IncidentPolicy(createIncident(impact));
	}

	protected ClientIncident createIncident(String impact) {
		return new ClientIncident(21, (short)1, null, (short)1, 0, 0,
			impact);
	}
}
