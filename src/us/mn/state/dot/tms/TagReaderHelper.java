/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
 * Tag reader helper methods.
 *
 * @author Douglas Lau
 */
public class TagReaderHelper extends BaseHelper {

	/** Disallow instantiation */
	protected TagReaderHelper() {
		assert false;
	}

	/** Get a tag reader iterator */
	static public Iterator<TagReader> iterator() {
		return new IteratorWrapper<TagReader>(namespace.iterator(
			TagReader.SONAR_TYPE));
	}

	/** Lookup the tag reader with the specified name */
	static public TagReader lookup(String name) {
		return (TagReader)namespace.lookupObject(TagReader.SONAR_TYPE,
			name);
	}
}
