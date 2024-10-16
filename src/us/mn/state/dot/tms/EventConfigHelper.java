/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * Helper for dealing with event configurations.
 *
 * @author Douglas Lau
 */
public class EventConfigHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private EventConfigHelper() {
		assert false;
	}

	/** Lookup the event config with the specified name */
	static public EventConfig lookup(String name) {
		return (EventConfig) namespace.lookupObject(
			EventConfig.SONAR_TYPE, name);
	}

	/** Get an event config iterator */
	static public Iterator<EventConfig> iterator() {
		return new IteratorWrapper<EventConfig>(namespace.iterator(
			EventConfig.SONAR_TYPE));
	}

	/** Check if store is enabled for an event table */
	static public boolean isStoreEnabled(String name) {
		EventConfig ec = lookup(name);
		if (ec != null)
			return ec.getEnableStore();
		else {
			System.err.println("EventConfig not found: " + name);
			return false;
		}
	}

	/** Check if purge is enabled for an event table */
	static public boolean isPurgeEnabled(String name) {
		EventConfig ec = lookup(name);
		if (ec != null)
			return ec.getEnablePurge();
		else {
			System.err.println("EventConfig not found: " + name);
			return false;
		}
	}
}
