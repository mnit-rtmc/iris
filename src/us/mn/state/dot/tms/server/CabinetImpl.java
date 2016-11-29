/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
 * @author Travis Swanston
 */
public class CabinetImpl extends BaseObjectImpl implements Cabinet {

	/** Load all the cabinets */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CabinetImpl.class);
		store.query("SELECT name, style, geo_loc FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CabinetImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// style
					row.getString(3)	// geo_loc
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("style", style);
		map.put("geo_loc", geo_loc);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new cabinet */
	public CabinetImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(n);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a new cabinet */
	private CabinetImpl(String n, CabinetStyle s, GeoLocImpl l) {
		super(n);
		style = s;
		geo_loc = l;
	}

	/** Create a new cabinet */
	private CabinetImpl(Namespace ns, String n, String s, String l) {
		this(n, (CabinetStyle) ns.lookupObject(CabinetStyle.SONAR_TYPE,
		     s), lookupGeoLoc(l));
	}

	/** Cabinet style */
	private CabinetStyle style;

	/** Set the cabinet style */
	@Override
	public void setStyle(CabinetStyle s) {
		style = s;
	}

	/** Set the cabinet style */
	public void doSetStyle(CabinetStyle s) throws TMSException {
		if (s != style) {
			store.update(this, "style", s);
			setStyle(s);
		}
	}

	/** Get the cabinet style */
	@Override
	public CabinetStyle getStyle() {
		return style;
	}

	/** Cabinet location */
	private GeoLocImpl geo_loc;

	/** Get the cabinet location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}
}
