/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
 * Flow stream helper methods.
 *
 * @author Douglas Lau
 */
public class FlowStreamHelper extends BaseHelper {

	/** Disallow instantiation */
	private FlowStreamHelper() {
		assert false;
	}

	/** Lookup the flow stream with the specified name */
	static public FlowStream lookup(String name) {
		return (FlowStream) namespace.lookupObject(
			FlowStream.SONAR_TYPE, name);
	}

	/** Get a flow stream iterator */
	static public Iterator<FlowStream> iterator() {
		return new IteratorWrapper<FlowStream>(namespace.iterator(
			FlowStream.SONAR_TYPE));
	}
}
