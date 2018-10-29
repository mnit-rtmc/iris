/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.IteratorWrapper;

/**
 * Helper class for RptConduit.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptConduitHelper extends BaseHelper {

	/** don't instantiate */
	private RptConduitHelper() {
		assert false;
	}

	/** Lookup the RptConduit with the specified name */
	static public RptConduit lookup(String name) {
		return (RptConduit)namespace.lookupObject(RptConduit.SONAR_TYPE, name);
	}

	/** Get a RptConduit iterator */
	static public Iterator<RptConduit> iterator() {
		return new IteratorWrapper<RptConduit>(namespace.iterator(
			RptConduit.SONAR_TYPE));
	}
}
