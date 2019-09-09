/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An empty iterator for unknown types.
 *
 * @author Douglas Lau
 */
public final class EmptyIterator implements Iterator<SonarObject> {
	@Override public boolean hasNext() {
		return false;
	}
	@Override public SonarObject next() {
		throw new NoSuchElementException();
	}
	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
