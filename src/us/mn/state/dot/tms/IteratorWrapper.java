/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.SonarObject;

/**
 * Iterator wrapper to cast SONAR objects.
 *
 * @author Douglas Lau
 */
public class IteratorWrapper<T extends SonarObject> implements Iterator<T> {

	/** Iterator being wrapped */
	private final Iterator<SonarObject> wrapped;

	/** Create a new iterator wrapper */
	public IteratorWrapper(Iterator<SonarObject> it) {
		wrapped = it;
	}

	/** Check if the iterator has a next value */
	@Override public boolean hasNext() {
		return wrapped.hasNext();
	}

	/** Get the next object from the iterator */
	@Override public T next() {
		return (T)wrapped.next();
	}

	/** Remove most recent object */
	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
