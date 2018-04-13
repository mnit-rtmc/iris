/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Helper class for catalogs.
 *
 * @author Douglas Lau
 */
public class CatalogHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private CatalogHelper() {
		assert false;
	}

	/** Lookup the catalog with the specified name */
	static public Catalog lookup(String name) {
		return (Catalog) namespace.lookupObject(Catalog.SONAR_TYPE,
			name);
	}

	/** Get a catalog iterator */
	static public Iterator<Catalog> iterator() {
		return new IteratorWrapper<Catalog>(namespace.iterator(
			Catalog.SONAR_TYPE));
	}
}
