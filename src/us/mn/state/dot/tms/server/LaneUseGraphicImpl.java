/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.LaneUseGraphic;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.TMSException;

/**
 * A lane-use graphic is an association between lane-use indication and a
 * graphic.
 *
 * @author Douglas Lau
 */
public class LaneUseGraphicImpl extends BaseObjectImpl
	implements LaneUseGraphic
{
	/** Load all the lane-use graphics */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading lane-use graphics...");
		namespace.registerType(SONAR_TYPE, LaneUseGraphicImpl.class);
		store.query("SELECT name, indication, g_number, graphic "+
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneUseGraphicImpl(
					namespace,
					row.getString(1),	// name
					row.getInt(2),		// indication
					row.getInt(3),		// g_number
					row.getString(4)	// graphic
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("indication", indication);
		map.put("g_number", g_number);
		map.put("graphic", graphic);
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

	/** Create a new lane-use graphic */
	public LaneUseGraphicImpl(String n) {
		super(n);
	}

	/** Create a new lane-use graphic */
	public LaneUseGraphicImpl(Namespace ns, String n, int i, int gn,
		String g)
	{
		this(n, i, gn, (Graphic)ns.lookupObject(Graphic.SONAR_TYPE, g));
	}

	/** Create a new lane-use graphic */
	public LaneUseGraphicImpl(String n, int i, int gn, Graphic g) {
		this(n);
		indication = i;
		g_number = gn;
		graphic = g;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Get the indication (ordinal of LaneUseIndication) */
	public int getIndication() {
		return indication;
	}

	/** Graphic number */
	protected int g_number;

	/** Get the graphic number */
	public int getGNumber() {
		return g_number;
	}

	/** Graphic associated with the lane-use indication */
	protected Graphic graphic;

	/** Get the graphic */
	public Graphic getGraphic() {
		return graphic;
	}
}
