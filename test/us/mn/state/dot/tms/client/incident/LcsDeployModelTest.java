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

import java.util.Arrays;
import junit.framework.TestCase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneUseIndication;
import static us.mn.state.dot.tms.LaneUseIndication.*;
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

	private LcsDeployModel createModel(String impact, int left, int right) {
		LaneConfiguration config = new LaneConfiguration(left, right);
		return new LcsDeployModel(createIncident(impact), config);
	}

	private LcsDeployModel createModel(String impact) {
		return createModel(impact, 4, 6);
	}

	private ClientIncident createIncident(String impact) {
		return new ClientIncident(null,
			EventType.INCIDENT_CRASH.ordinal(), null, (short) 1,
			null, (short) 1, 0, 0, impact);
	}

	private void checkIndications(LcsDeployModel m, LaneConfiguration cfg,
		Distance up, int n_lcs, int lcs_shift, LaneUseIndication[] ind)
	{
		LaneUseIndication[] inds = m.createIndications(cfg, up, n_lcs,
			lcs_shift);
/*		for (LaneUseIndication l: inds)
			System.err.print(l + " ");
		System.err.println();*/
		assertTrue(Arrays.equals(ind, inds));
	}

	public void testShortClear() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("....");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[]
			{ LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[]
			{ DARK, LANE_OPEN } );
		// three LCS
		checkIndications(m, cfg, up, 3, 3, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[]
			{ DARK, LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 3, 5, new LaneUseIndication[]
			{ DARK, DARK, LANE_OPEN } );
		// four LCS
		checkIndications(m, cfg, up, 4, 2, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN, DARK, DARK } );
		checkIndications(m, cfg, up, 4, 3, new LaneUseIndication[]
			{ DARK, LANE_OPEN, LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 4, 4, new LaneUseIndication[]
			{ DARK, DARK, LANE_OPEN, LANE_OPEN } );
	}

	public void testShortClearShifted() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(3, 5);
		LcsDeployModel m = createModel("....");
		// two LCS
		checkIndications(m, cfg, up, 2, 2, new LaneUseIndication[]
			{ LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[]
			{ DARK, LANE_OPEN } );
		// three LCS
		checkIndications(m, cfg, up, 3, 2, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 3, 3, new LaneUseIndication[]
			{ DARK, LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[]
			{ DARK, DARK, LANE_OPEN } );
		// four LCS
		checkIndications(m, cfg, up, 4, 1, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN, DARK, DARK } );
		checkIndications(m, cfg, up, 4, 2, new LaneUseIndication[]
			{ DARK, LANE_OPEN, LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 4, 3, new LaneUseIndication[]
			{ DARK, DARK, LANE_OPEN, LANE_OPEN } );
	}

	public void testShortRightShoulderBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("...!");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			USE_CAUTION, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, USE_CAUTION } );
		// three LCS
		cfg = new LaneConfiguration(4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_OPEN, USE_CAUTION, LANE_OPEN } );
	}

	public void testShortLeftShoulderBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("!...");
		// two LCS
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_OPEN, USE_CAUTION } );
		// three LCS
		cfg = new LaneConfiguration(3, 6);
		checkIndications(m, cfg, up, 3, 3, new LaneUseIndication[] {
			LANE_OPEN, USE_CAUTION, LANE_OPEN } );
	}

	public void testShortRightLanePartBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("..?.");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			USE_CAUTION, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, USE_CAUTION } );
	}

	public void testShortLeftLanePartBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel(".?..");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			USE_CAUTION, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_OPEN, USE_CAUTION } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, LANE_OPEN } );
	}

	public void testShortRightLaneBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("..!.");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			USE_CAUTION, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_CLOSED, USE_CAUTION } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, LANE_CLOSED } );
	}

	public void testShortLeftLaneBlocked() {
		Distance up = new Distance(0.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel(".!..");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_CLOSED, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			USE_CAUTION, LANE_CLOSED } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, USE_CAUTION } );
	}

	public void testMediumClear() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("....");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[]
			{ LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[]
			{ LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[]
			{ DARK, LANE_OPEN } );
	}

	public void testMediumRightShoulderBlocked() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("...!");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, LANE_OPEN } );
	}

	public void testMediumRightLanePartBlocked() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("..?.");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_OPEN, LANE_OPEN } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, LANE_OPEN } );
	}

	public void testMediumRightLaneBlocked() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel("..!.");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			LANE_OPEN, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			MERGE_LEFT, LANE_OPEN} );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, MERGE_LEFT} );
	}

	public void testMediumLeftLaneBlocked() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 6);
		LcsDeployModel m = createModel(".!..");
		// two LCS
		checkIndications(m, cfg, up, 2, 3, new LaneUseIndication[] {
			MERGE_RIGHT, DARK } );
		checkIndications(m, cfg, up, 2, 4, new LaneUseIndication[] {
			LANE_OPEN, MERGE_RIGHT } );
		checkIndications(m, cfg, up, 2, 5, new LaneUseIndication[] {
			DARK, LANE_OPEN } );
	}

	public void testMediumLanesBlocked() {
		Distance up = new Distance(0.75f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 7);
		LcsDeployModel m = createModel("..!..", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_OPEN, MERGE_BOTH, LANE_OPEN } );
		m = createModel("..!!.", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			MERGE_LEFT, MERGE_LEFT, LANE_OPEN } );
		m = createModel(".!!..", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_OPEN, MERGE_RIGHT, MERGE_RIGHT } );
		m = createModel(".!!!.", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			MERGE_RIGHT, MERGE_BOTH, MERGE_LEFT } );
		m = createModel(".!!!!", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			MERGE_LEFT, MERGE_LEFT, MERGE_LEFT } );
		m = createModel("!!!!.", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			MERGE_RIGHT, MERGE_RIGHT, MERGE_RIGHT } );
		m = createModel("!!!!!", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_CLOSED, LANE_CLOSED, LANE_CLOSED } );
	}

	public void testLongLaneBlocked() {
		Distance up = new Distance(1.25f, Distance.Units.MILES);
		LaneConfiguration cfg = new LaneConfiguration(4, 7);
		LcsDeployModel m = createModel("...!.", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_CLOSED_AHEAD, LANE_OPEN, LANE_OPEN } );
		m = createModel("..!..", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_OPEN, LANE_CLOSED_AHEAD, LANE_OPEN } );
		m = createModel(".!...", 4, 7);
		checkIndications(m, cfg, up, 3, 4, new LaneUseIndication[] {
			LANE_OPEN, LANE_OPEN, LANE_CLOSED_AHEAD } );
	}
}
