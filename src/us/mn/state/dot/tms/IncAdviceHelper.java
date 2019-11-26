/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper class for Incident Advices.
 *
 * @author Douglas Lau
 */
public class IncAdviceHelper extends BaseHelper {

	/** Don't instantiate */
	private IncAdviceHelper() {
		assert false;
	}

	/** Get an incident advice iterator */
	static public Iterator<IncAdvice> iterator() {
		return new IteratorWrapper<IncAdvice>(namespace.iterator(
			IncAdvice.SONAR_TYPE));
	}

	/** Find a matching incident advice */
	static public IncAdvice match(IncRange rng, Incident inc) {
		IncAdvice res = null;
		int priority = 0;
		IncImpact imp = IncImpact.getImpact(inc);
		Iterator<IncAdvice> it = iterator();
		while (it.hasNext()) {
			IncAdvice adv = it.next();
			if (adv.getImpact() == imp.ordinal() &&
			    adv.getRange() == rng.ordinal() &&
			    adv.getLaneType() == inc.getLaneType())
			{
				LaneMatch open = LaneMatch.check(
					adv.getOpenLanes(),
					IncImpact.getOpenLanes(inc));
				LaneMatch impacted = LaneMatch.check(
					adv.getImpactedLanes(),
					IncImpact.getImpactedLanes(inc));
				Integer p = matchPriority(open, impacted);
				if (p != null && p >= priority) {
					res = adv;
					priority = p;
				}
			}
		}
		return res;
	}

	/** Match enum for incident lanes */
	static private enum LaneMatch {
		NO,	// lanes do not match
		YES,	// lanes match
		ANY;	// any lanes allowed

		/** Check lane match.
		 * @param al Advice lanes.
		 * @param il Incident lanes.
		 * @return Lane match. */
		static private LaneMatch check(Integer al, int il) {
			if (al != null) {
				return (al == il)
				      ? LaneMatch.YES
				      : LaneMatch.NO;
			} else
				return LaneMatch.ANY;
		}
	}

	/** Match open/impacted lanes.
	 * @param open Match of open lanes.
	 * @param impacted Match of impacted lanes.
	 * @return Priority of match, or null if not matched */
	static private Integer matchPriority(LaneMatch open, LaneMatch impacted)
	{
		if ((open == LaneMatch.YES) && (impacted == LaneMatch.YES))
			return 3;
		if ((open == LaneMatch.YES) && (impacted == LaneMatch.ANY))
			return 2;
		if ((open == LaneMatch.ANY) && (impacted == LaneMatch.YES))
			return 1;
		if ((open == LaneMatch.ANY) && (impacted == LaneMatch.ANY))
			return 0;
		else
			return null; // no match
	}

	/** Validate a MULTI string */
	static public boolean isMultiValid(String m) {
		return m.equals(new MultiString(m).normalizeLine().toString());
	}
}
