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
import us.mn.state.dot.tms.ChangeVetoException;
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
		store.query("SELECT name, indication, graphic, foreground, " +
			"page, on_time FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneUseGraphicImpl(
					namespace,
					row.getString(1),	// name
					row.getInt(2),		// indication
					row.getString(3),	// graphic
					row.getInt(4),		// foreground
					row.getInt(5),		// page
					row.getInt(6)		// on_time
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("indication", indication);
		map.put("graphic", graphic);
		map.put("foreground", foreground);
		map.put("page", page);
		map.put("on_time", on_time);
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
	public LaneUseGraphicImpl(Namespace ns, String n, int i, String g,
		int f, int p, int t)
	{
		this(n, i, (Graphic)ns.lookupObject(Graphic.SONAR_TYPE, g), f,
		     p, t);
	}

	/** Create a new lane-use graphic */
	public LaneUseGraphicImpl(String n, int i, Graphic g, int f, int p,
		int t)
	{
		this(n);
		indication = i;
		graphic = g;
		foreground = f;
		page = p;
		on_time = t;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Set the indication (ordinal of LaneUseIndication) */
	public void setIndication(int i) {
		indication = i;
	}

	/** Set the indication (ordinal of LaneUseIndication) */
	public void doSetIndication(int i) throws TMSException {
		if(i == indication)
			return;
		LaneUseIndication ind = LaneUseIndication.fromOrdinal(i);
		if(ind == null)
			throw new ChangeVetoException("Invalid indication:" +i);
		store.update(this, "indication", i);
		setIndication(i);
	}

	/** Get the indication (ordinal of LaneUseIndication) */
	public int getIndication() {
		return indication;
	}

	/** Graphic associated with the lane-use indication */
	protected Graphic graphic;

	/** Set the graphic */
	public void setGraphic(Graphic g) {
		graphic = g;
	}

	/** Set the graphic */
	public void doSetGraphic(Graphic g) throws TMSException {
		if(g == graphic)
			return;
		store.update(this, "graphic", g.getName());
		setGraphic(g);
	}

	/** Get the graphic */
	public Graphic getGraphic() {
		return graphic;
	}

	/** Foreground color */
	protected int foreground;

	/** Set the foreground color */
	public void setForeground(int f) {
		foreground = f;
	}

	/** Set the foreground color */
	public void doSetForeground(int f) throws TMSException {
		if(f == foreground)
			return;
		store.update(this, "foreground", f);
		setForeground(f);
	}

	/** Get the foreground color */
	public int getForeground() {
		return foreground;
	}

	/** Page number */
	protected int page;

	/** Set the page number */
	public void setPage(int p) {
		page = p;
	}

	/** Set the page number */
	public void doSetPage(int p) throws TMSException {
		if(p == page)
			return;
		if(p < 1 || p > 6)
			throw new ChangeVetoException("Invalid page:" + p);
		store.update(this, "page", p);
		setPage(p);
	}

	/** Get the page number */
	public int getPage() {
		return page;
	}

	/** Page on time (tenths of a second) */
	protected int on_time;

	/** Set the page on time (tenths of a second) */
	public void setOnTime(int t) {
		on_time = t;
	}

	/** Set the page on time (tenths of a second) */
	public void doSetOnTime(int t) throws TMSException {
		if(t == on_time)
			return;
		if(t < 1 || t > 60)
			throw new ChangeVetoException("Invalid on time:" + t);
		store.update(this, "on_time", t);
		setOnTime(t);
	}

	/** Get the page on time (tenths of a second) */
	public int getOnTime() {
		return on_time;
	}
}
