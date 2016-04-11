/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import java.util.Set;

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
	static public IncLocator match(Set<SignGroup> groups, IncRange rng,
		boolean branched, boolean pickable)
	{
		Iterator<IncLocator> it = iterator();
		while (it.hasNext()) {
			IncLocator iloc = it.next();
			if (groups.contains(iloc.getSignGroup()) &&
			    iloc.getRange() == rng.ordinal() &&
			    iloc.getBranched() == branched &&
			    iloc.getPickable() == pickable)
				return iloc;
		}
		return null;
	}
}
