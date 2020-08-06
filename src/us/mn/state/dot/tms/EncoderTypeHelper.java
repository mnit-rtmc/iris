/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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
 * Helper class for EncoderType objects
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class EncoderTypeHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private EncoderTypeHelper() {
		assert false;
	}

	/** Lookup the encoder type object with the specified name */
	static public EncoderType lookup(String name) {
		return (EncoderType) namespace.lookupObject(
				EncoderType.SONAR_TYPE, name);
	}

	/** Get an encoder type object iterator */
	static public Iterator<EncoderType> iterator() {
		return new IteratorWrapper<EncoderType>(namespace.iterator(
				EncoderType.SONAR_TYPE));
	}
}
