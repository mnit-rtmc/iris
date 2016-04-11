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
 * Helper class for Incident Descriptors.
 *
 * @author Douglas Lau
 */
public class IncDescriptorHelper extends BaseHelper {

	/** Don't instantiate */
	private IncDescriptorHelper() {
		assert false;
	}

	/** Get an incident descriptor iterator */
	static public Iterator<IncDescriptor> iterator() {
		return new IteratorWrapper<IncDescriptor>(namespace.iterator(
			IncDescriptor.SONAR_TYPE));
	}

	/** Find a matching incident descriptor */
	static public IncDescriptor match(Incident inc, Set<SignGroup> groups) {
		Iterator<IncDescriptor> it = iterator();
		while (it.hasNext()) {
			IncDescriptor dsc = it.next();
			if (groups.contains(dsc.getSignGroup()) &&
			    dsc.getEventType() == inc.getEventType() &&
			    dsc.getLaneType() == inc.getLaneType() &&
			    dsc.getDetail() == inc.getDetail() &&
			    dsc.getCleared() == inc.getCleared())
				return dsc;
		}
		return null;
	}
}
