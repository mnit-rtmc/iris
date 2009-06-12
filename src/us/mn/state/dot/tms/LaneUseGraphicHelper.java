/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.TreeMap;
import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for LaneUseGraphics.
 *
 * @author Douglas Lau
 */
public class LaneUseGraphicHelper extends BaseHelper {

	/** Prevent object creation */
	private LaneUseGraphicHelper() {
		assert false;
	}

	/** Find LaneUseGraphics using a Checker */
	static public LaneUseGraphic find(Checker<LaneUseGraphic> checker) {
		return (LaneUseGraphic)namespace.findObject(
			LaneUseGraphic.SONAR_TYPE, checker);
	}

	/** Get a collection of lane-use graphics for a given indication.
	 * @param ind LaneUseIndication ordinal value.
	 * @return A collection of lane-use graphics, sorted by page number. */
	static public Collection<LaneUseGraphic> getIndicationGraphics(
		final int ind)
	{
		final TreeMap<Integer, LaneUseGraphic> g_pages =
			new TreeMap<Integer, LaneUseGraphic>();
		find(new Checker<LaneUseGraphic>() {
			public boolean check(LaneUseGraphic lug) {
				if(lug.getIndication() == ind)
					g_pages.put(lug.getPage(), lug);
				return false;
			}
		});
		return g_pages.values();
	}
}
