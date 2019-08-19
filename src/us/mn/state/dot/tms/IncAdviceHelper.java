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
		IncImpact imp = IncImpact.getImpact(inc);
		Iterator<IncAdvice> it = iterator();
		while (it.hasNext()) {
			IncAdvice adv = it.next();
			if (adv.getRange() == rng.ordinal() &&
			    adv.getLaneType() == inc.getLaneType() &&
			    adv.getImpact() == imp.ordinal() &&
			    adv.getCleared() == inc.getCleared())
				return adv;
		}
		return null;
	}
}
