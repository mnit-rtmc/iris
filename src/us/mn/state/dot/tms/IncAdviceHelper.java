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
			    adv.getLaneType() == inc.getLaneType() &&
			    adv.getCleared() == inc.getCleared())
			{
				int il = IncImpact.getImpactedLanes(inc);
				int ol = IncImpact.getOpenLanes(inc);
				Integer ail = adv.getImpactedLanes();
				Integer aol = adv.getOpenLanes();
				if (ail != null && aol != null &&
				    ail == il && aol == ol)
				{
					res = adv;
					priority = 3;
				} else
				if (ail != null && ail == il &&
				    priority < 2)
				{
					res = adv;
					priority = 2;
				} else
				if (aol != null && aol == ol &&
				    priority < 1)
				{
					res = adv;
					priority = 1;
				} else
				if (ail == null && aol == null)
					res = adv;
			}
		}
		return res;
	}
}
