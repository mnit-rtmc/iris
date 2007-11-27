/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

/**
 * Base object class for storable SONAR objects.
 *
 * @author Douglas Lau
 */
abstract public class BaseObjectImpl implements Storable {

	/** SQL connection to database */
	static protected SQLConnection store;

	/** Get the primary key name */
	public String getKeyName() {
		return "name";
	}

	/** Get the primary key */
	public String getKey() {
		return name;
	}

	/** Base object name */
	protected final String name;

	/** Get the graphic name */
	public String getName() {
		return name;
	}

	/** Create a new base object */
	protected BaseObjectImpl(String n) {
		name = n;
	}

	/** Get a string representation of the object */
	public String toString() {
		return name;
	}

	/** Destroy an object */
	public void destroy() {
		// Handled by doDestroy() method
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}
}
