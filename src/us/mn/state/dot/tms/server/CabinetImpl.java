/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TMSException;

/**
 * A cabinet is a roadside enclosure containing one or more device controllers.
 *
 * @author Douglas Lau
 */
public class CabinetImpl extends BaseObjectImpl implements Cabinet {

	/** Load all the cabinets */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading cabinets...");
		namespace.registerType(SONAR_TYPE, CabinetImpl.class);
		store.query("SELECT name, style, geo_loc, mile FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CabinetImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// style
					row.getString(3),	// geo_loc
					row.getFloat(4)		// mile
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("style", style);
		map.put("geo_loc", geo_loc);
		map.put("mile", mile);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new cabinet */
	public CabinetImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(n);
		MainServer.server.createObject(g);
		geo_loc = g;
	}

	/** Create a new cabinet */
	protected CabinetImpl(String n, CabinetStyle s, GeoLoc l, Float m) {
		super(n);
		style = s;
		geo_loc = l;
		mile = m;
	}

	/** Create a new cabinet */
	protected CabinetImpl(Namespace ns, String n, String s, String l,
		Float m)
	{
		this(n, (CabinetStyle)ns.lookupObject( CabinetStyle.SONAR_TYPE,
			s), (GeoLoc)ns.lookupObject(GeoLoc.SONAR_TYPE, l), m);
	}

	/** Cabinet style */
	protected CabinetStyle style;

	/** Set the cabinet style */
	public void setStyle(CabinetStyle s) {
		style = s;
	}

	/** Set the cabinet style */
	public void doSetStyle(CabinetStyle s) throws TMSException {
		if(s == style)
			return;
		store.update(this, "style", s);
		setStyle(s);
	}

	/** Get the cabinet style */
	public CabinetStyle getStyle() {
		return style;
	}

	/** Cabinet location */
	protected GeoLoc geo_loc;

	/** Set the cabinet location */
	public void setGeoLoc(GeoLoc l) {
		geo_loc = l;
	}

	/** Set the cabinet location */
	public void doSetGeoLoc(GeoLoc l) throws TMSException {
		if(l == geo_loc)
			return;
		store.update(this, "geo_loc", l);
		setGeoLoc(l);
	}

	/** Get the cabinet location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Milepoint on freeway */
	protected Float mile;

	/** Set the milepoint */
	public void setMile(Float m) {
		mile = m;
	}

	/** Set the milepoint */
	public void doSetMile(Float m) throws TMSException {
		if(m == mile)
			return;
		store.update(this, "mile", m);
		setMile(m);
	}

	/** Get the milepoint */
	public Float getMile() {
		return mile;
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		MainServer.server.removeObject(geo_loc);
	}
}
