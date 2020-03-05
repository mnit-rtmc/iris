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
 * Helper class for Incident Locators.
 *
 * @author Douglas Lau
 */
public class IncLocatorHelper extends BaseHelper {

	/** Don't instantiate */
	private IncLocatorHelper() {
		assert false;
	}

	/** Get an incident locator iterator */
	static public Iterator<IncLocator> iterator() {
		return new IteratorWrapper<IncLocator>(namespace.iterator(
			IncLocator.SONAR_TYPE));
	}

	/** Find a matching incident locator */
	static public IncLocator match(IncRange rng, boolean branched,
		boolean picked)
	{
		Iterator<IncLocator> it = iterator();
		while (it.hasNext()) {
			IncLocator iloc = it.next();
			if (iloc.getRange() == rng.ordinal() &&
			    iloc.getBranched() == branched &&
			    iloc.getPicked() == picked)
				return iloc;
		}
		return null;
	}

	/** Validate a MULTI string */
	static public boolean isMultiValid(String m) {
		return m.equals(new MultiString(m).normalizeLocator()
			.toString());
	}
}
