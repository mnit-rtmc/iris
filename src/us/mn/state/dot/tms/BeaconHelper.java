/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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
import java.util.NoSuchElementException;

/**
 * Beacon helper methods.
 *
 * @author Douglas Lau
 */
public class BeaconHelper extends BaseHelper {

	/** Disallow instantiation */
	protected BeaconHelper() {
		assert false;
	}

	/** Lookup the beacon with the specified name */
	static public Beacon lookup(String name) {
		return (Beacon) namespace.lookupObject(Beacon.SONAR_TYPE, name);
	}

	/** Get a beacon iterator */
	static public Iterator<Beacon> iterator() {
		return new IteratorWrapper<Beacon>(namespace.iterator(
			Beacon.SONAR_TYPE));
	}

	/** Get a beacon iterator for an associated device */
	static public Iterator<Beacon> iterator(final Device dev) {
		return new Iterator<Beacon>() {
			final Iterator<Beacon> it = iterator();
			boolean has_next;
			Beacon next_b = filterNext();
			private Beacon filterNext() {
				while (it.hasNext()) {
					Beacon b = it.next();
					if (b.getDevice() == dev) {
						has_next = true;
						return b;
					}
				}
				has_next = false;
				return null;
			}
			@Override public boolean hasNext() {
				return has_next;
			}
			@Override public Beacon next() {
				if (!has_next)
					throw new NoSuchElementException();
				Beacon b = next_b;
				next_b = filterNext();
				return b;
			}
			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
