/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.TMSException;

/**
 * Base object class for storable SONAR objects.
 *
 * @author Douglas Lau
 */
abstract public class BaseObjectImpl implements Storable, SonarObject {

	/** SONAR namespace */
	static public ServerNamespace namespace;

	/** SQL connection to database */
	static protected SQLConnection store;

	/** Load all objects from the database into the SONAR Namespace */
	static void loadAll(SQLConnection s, ServerNamespace ns)
		throws TMSException, NamespaceError
	{
		store = s;
		namespace = ns;
		SystemAttributeImpl.loadAll();
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
		R_NodeImpl.loadAll();
		AlarmImpl.loadAll();
		DetectorImpl.loadAll();
		CameraImpl.loadAll();
		WarningSignImpl.loadAll();
		RampMeterImpl.loadAll();
		SignMessageImpl.loadAll();
		QuickMessageImpl.loadAll();
		DMSImpl.loadAll();
		SignGroupImpl.loadAll();
		DmsSignGroupImpl.loadAll();
		SignTextImpl.loadAll();
		LCSArrayImpl.loadAll();
		LCSImpl.loadAll();
		LCSIndicationImpl.loadAll();
		TimingPlanImpl.loadAll();
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

	/** Get the object name */
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

	/** Notify SONAR clients of a change to an attribute */
	protected void notifyAttribute(String aname) {
		if(MainServer.server != null)
			MainServer.server.setAttribute(this, aname);
	}
}
