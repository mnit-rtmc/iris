/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Cache for LCS proxy objects.
 *
 * @author Douglas Lau
 */
public class LcsCache {

	/** Cache of LCS arrays */
	protected final TypeCache<LCSArray> lcs_arrays;

	/** Get the LCS array cache */
	public TypeCache<LCSArray> getLCSArrays() {
		return lcs_arrays;
	}

	/** Cache of LCS */
	protected final TypeCache<LCS> lcss;

	/** Get the LCS cache */
	public TypeCache<LCS> getLCSs() {
		return lcss;
	}

	/** Cache of LCS indications */
	protected final TypeCache<LCSIndication> lcs_indications;

	/** Get the LCS indication cache */
	public TypeCache<LCSIndication> getLCSIndications() {
		return lcs_indications;
	}

	/** Cache of lane-use MULTI strings */
	protected final TypeCache<LaneUseMulti> lane_use_multis;

	/** Get the lane-use MULTI cache */
	public TypeCache<LaneUseMulti> getLaneUseMultis() {
		return lane_use_multis;
	}

	/** Create a new LCS cache */
	public LcsCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException 
	{
		lcs_arrays = new TypeCache<LCSArray>(LCSArray.class, client);
		lcss = new TypeCache<LCS>(LCS.class, client);
		lcs_indications = new TypeCache<LCSIndication>(
			LCSIndication.class, client);
		lane_use_multis = new TypeCache<LaneUseMulti>(
			LaneUseMulti.class, client);
	}

	/** Populate the LCS cache */
	public void populate(SonarState client) {
		client.populateReadable(lcs_arrays);
		if(client.canRead(LCSArray.SONAR_TYPE)) {
			lcs_arrays.ignoreAttribute("operation");
			// We can't ignore indicationsCurrent and ownerCurrent,
			// because LCSArrayCellRenderer lists need the updates
		}
		client.populateReadable(lcss);
		client.populateReadable(lcs_indications);
		client.populateReadable(lane_use_multis);
	}
}
