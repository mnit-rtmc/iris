/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Cache for LCS proxy objects.
 *
 * @author Douglas Lau
 */
public class LcsCache {

	/** Cache of LCS arrays */
	private final TypeCache<Lcs> lcss;

	/** Get the LCS array cache */
	public TypeCache<Lcs> getLcss() {
		return lcss;
	}

	/** Cache of LCS states */
	private final TypeCache<LcsState> lcs_states;

	/** Get the LCS cache */
	public TypeCache<LcsState> getLcsStates() {
		return lcs_states;
	}

	/** Create a new LCS cache */
	public LcsCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		lcss = new TypeCache<Lcs>(Lcs.class, client);
		lcs_states = new TypeCache<LcsState>(LcsState.class, client);
	}

	/** Populate the LCS cache */
	public void populate(SonarState client) {
		client.populateReadable(lcss);
		if (client.canRead(Lcs.SONAR_TYPE)) {
			lcss.ignoreAttribute("operation");
			// We can't ignore status, because LcsCellRenderer
			// lists need the updates
		}
		client.populateReadable(lcs_states);
	}
}
