/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.server.Namespace;

/**
 * Base object class for storable SONAR objects.
 *
 * @author Douglas Lau
 */
abstract public class BaseObjectImpl implements Storable {

	/** SONAR namespace */
	static protected Namespace namespace;

	/** SQL connection to database */
	static protected SQLConnection store;

	/** Load all objects from the database into the SONAR Namespace */
	static void loadAll(SQLConnection s, Namespace ns) throws TMSException {
		store = s;
		namespace = ns;
		SystemAttributeImpl.loadAll();
		SystemPolicyImpl.loadAll();
		HolidayImpl.loadAll();
		GraphicImpl.loadAll();
		FontImpl.loadAll();
		GlyphImpl.loadAll();
		VideoMonitorImpl.loadAll();
		RoadImpl.loadAll();
		GeoLocImpl.loadAll();
		CommLinkImpl.loadAll();
		CabinetStyleImpl.loadAll();
		CabinetImpl.loadAll();
		ControllerImpl.loadAll();
		SignGroupImpl.loadAll();
		DmsSignGroupImpl.loadAll();
		SignTextImpl.loadAll();
		AlarmImpl.loadAll();
		CameraImpl.loadAll();
		WarningSignImpl.loadAll();
		TrafficDeviceAttributeImpl.loadAll();
	}

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
		// FIXME: validate for SQL injection
		name = n;
	}

	/** Get a string representation of the object */
	public String toString() {
		return name;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
		initTransients();
	}

	/** Destroy an object */
	public void destroy() {
		// Handled by doDestroy() method
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Initialize the transient fields */
	protected void initTransients() throws TMSException {
		// Override this to initialize new objects
	}
}
