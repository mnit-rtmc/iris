/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;

/**
 * An incident is an event (crash, stall, etc.) which has an effect on traffic.
 *
 * @author Douglas Lau
 */
public class IncidentImpl extends BaseObjectImpl implements Incident {

	/** Impact code validation regex pattern */
	static protected final Pattern IMPACT_PATTERN = Pattern.compile(
		"[.?!]*");

	/** Validate an impact code */
	static protected void validateImpact(String imp)
		throws ChangeVetoException
	{
		Matcher m = IMPACT_PATTERN.matcher(imp);
		if(!m.matches())
			throw new ChangeVetoException("Invalid impact: " + imp);
	}

	/** Load all the incidents */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading active incidents...");
		namespace.registerType(SONAR_TYPE, IncidentImpl.class);
		store.query("SELECT name, event_desc_id, event_date, " +
			"lane_type, road, dir, easting, northing, camera, " +
			"impact, cleared FROM event." + SONAR_TYPE +
			" WHERE cleared = 'f';", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IncidentImpl(namespace,
					row.getString(1),	// name
					row.getInt(2),		// event_desc_id
					row.getTimestamp(3),	// event_date
					row.getShort(4),	// lane_type
					row.getString(5),	// road
					row.getShort(6),	// dir
					row.getInt(7),		// easting
					row.getInt(8),		// northing
					row.getString(9),	// camera
					row.getString(10),	// impact
					row.getBoolean(11)	// cleared
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("event_desc_id", event_desc_id);
		map.put("event_date", event_date);
		map.put("lane_type", (short)lane_type.ordinal());
		map.put("road", road);
		map.put("dir", dir);
		map.put("easting", easting);
		map.put("northing", northing);
		map.put("camera", camera);
		map.put("impact", impact);
		map.put("cleared", cleared);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "event." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new incident (by SONAR clients) */
	public IncidentImpl(String n) {
		super(n);
	}

	/** Create an incident */
	protected IncidentImpl(Namespace ns, String n, int et, Date ed,
		short lt, String r, short d, int ue, int un, String cam,
		String im, boolean c)
	{
		this(n, et, ed, lt, (Road)ns.lookupObject(Road.SONAR_TYPE, r),
		     d, ue, un, (Camera)ns.lookupObject(Camera.SONAR_TYPE, cam),
		     im, c);
	}

	/** Create an incident */
	protected IncidentImpl(String n, int et, Date ed, short lt, Road r,
		short d, int ue, int un, Camera cam, String im, boolean c)
	{
		super(n);
		event_desc_id = et;
		event_date = new Date(ed.getTime());
		lane_type = LaneType.fromOrdinal(lt);
		road = r;
		dir = d;
		easting = ue;
		northing = un;
		camera = cam;
		impact = im;
		cleared = c;
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		// This happens when the SONAR object is being destroyed.
		// Incidents are stored in the event schema, and should never
		// be DELETEd.  Just set the 'cleared' column to true, and it
		// won't get loaded on the next server restart.
		doSetCleared(true);
	}

	/** Event type (id of EventType enum) */
	protected int event_desc_id;

	/** Get the event type */
	public int getEventType() {
		return event_desc_id;
	}

	/** Event date (timestamp) */
	protected Date event_date = new Date();

	/** Get the event date (timestamp) */
	public long getEventDate() {
		return event_date.getTime();
	}

	/** Lane type */
	protected LaneType lane_type = LaneType.MAINLINE;

	/** Get the lane type */
	public short getLaneType() {
		return (short)lane_type.ordinal();
	}

	/** Road for incident location */
	protected Road road;

	/** Get the road */
	public Road getRoad() {
		return road;
	}

	/** Direction on road */
	protected short dir;

	/** Get the road direction */
	public short getDir() {
		return dir;
	}

	/** UTM Easting */
	protected int easting;

	/** Get the UTM Easting */
	public int getEasting() {
		return easting;
	}

	/** UTM Northing */
	protected int northing;

	/** Get the UTM Northing */
	public int getNorthing() {
		return northing;
	}

	/** Camera for verificaiton */
	protected Camera camera;

	/** Get the verification camera */
	public Camera getCamera() {
		return camera;
	}

	/** Impact code */
	protected String impact = "";

	/** Get the current impact code.
	 * @see us.mn.state.dot.tms.Incident.getImpact() */
	public String getImpact() {
		return impact;
	}

	/** Set the impact code */
	public void setImpact(String imp) {
		impact = imp;
	}

	/** Set the impact code */
	public void doSetImpact(String imp) throws TMSException {
		if(imp.equals(impact))
			return;
		validateImpact(imp);
		store.update(this, "impact", imp);
		setImpact(imp);
	}

	/** Incident cleared status */
	protected boolean cleared = false;

	/** Get the cleared status */
	public boolean getCleared() {
		return cleared;
	}

	/** Set the cleared status */
	public void setCleared(boolean c) {
		cleared = c;
		clear_time = System.currentTimeMillis();
	}

	/** Set the cleared status */
	public void doSetCleared(boolean c) throws TMSException {
		if(c == cleared)
			return;
		store.update(this, "cleared", c);
		setCleared(c);
	}

	/** Time the incident was cleared */
	protected long clear_time = System.currentTimeMillis();

	/** Get the time the incident was cleared */
	public long getClearTime() {
		return clear_time;
	}

	/** Render the incident as xml */
	public void printXmlElement(PrintWriter out) {
		String loc = lookupLocation();
		out.print("<incident");
		out.print(XmlWriter.createAttribute("id", getName()));
		out.print(XmlWriter.createAttribute("event_type",
			EventType.fromId(event_desc_id)));
		out.print(XmlWriter.createAttribute("event_date", event_date));
		out.print(XmlWriter.createAttribute("lane_type", lane_type));
		out.print(XmlWriter.createAttribute("road", road));
		out.print(XmlWriter.createAttribute("dir",
			Direction.DIRECTION[dir]));
		if(loc != null)
			out.print(XmlWriter.createAttribute("location", loc));
		out.print(XmlWriter.createAttribute("easting", easting));
		out.print(XmlWriter.createAttribute("northing", northing));
		out.print(XmlWriter.createAttribute("camera", camera));
		out.print(XmlWriter.createAttribute("impact", impact));
		out.print(XmlWriter.createAttribute("cleared", cleared));
		out.println("/>");
	}

	/** Lookup the incident location */
	protected String lookupLocation() {
		if(corridors == null)
			return null;
		Corridor cor = corridors.getCorridor(
			GeoLocHelper.getCorridorName(road, dir));
		if(cor == null)
			return null;
		R_Node rnd = cor.findNearest(easting, northing);
		if(rnd == null)
			return null;
		return GeoLocHelper.getCrossDescription(rnd.getGeoLoc());
	}
}
