/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  Minnesota Department of Transportation
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.TagReaderPoller;
import us.mn.state.dot.tms.server.event.TagReadEvent;

/**
 * A tag reader is a sensor for vehicle transponders, which are used for
 * toll lanes.
 *
 * @author Douglas Lau
 */
public class TagReaderImpl extends DeviceImpl implements TagReader {

	/** Load all the tag readers */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, TagReaderImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"toll_zone FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TagReaderImpl(
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6)	// toll_zone
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("toll_zone", toll_zone);
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

	/** Create a new tag reader with a string name */
	public TagReaderImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a tag reader */
	protected TagReaderImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt, TollZone tz)
	{
		super(n, c, p, nt);
		geo_loc = l;
		toll_zone = tz;
		initTransients();
	}

	/** Create a tag reader */
	protected TagReaderImpl(String n, String l, String c, int p, String nt,
		String tz)
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt,
		     lookupTollZone(tz));
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Toll zone */
	private TollZone toll_zone;

	/** Set the toll zone */
	@Override
	public void setTollZone(TollZone tz) {
		toll_zone = tz;
	}

	/** Set the toll zone */
	public void doSetTollZone(TollZone tz) throws TMSException {
		if (tz != toll_zone) {
			store.update(this, "toll_zone", tz);
			setTollZone(tz);
		}
	}

	/** Get the toll zone */
	@Override
	public TollZone getTollZone() {
		return toll_zone;
	}

	/** Request a device operation */
	@Override
	public void setDeviceRequest(int r) {
		sendDeviceRequest(DeviceRequest.fromOrdinal(r));
	}

	/** Request a device operation */
	public void sendDeviceRequest(DeviceRequest dr) {
		TagReaderPoller p = getTagReaderPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Get the tag reader poller */
	private TagReaderPoller getTagReaderPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof TagReaderPoller)
		     ? (TagReaderPoller)dp
		     : null;
	}

	/** Log a tag (transponder) read event.
	 * @param stamp Timestamp of read event.
	 * @param tt Tag Type.
	 * @param tid Tag (transponder) ID.
	 * @param hov HOV switch flag. */
	public void logRead(long stamp, TagType tt, int tid, boolean hov) {
		TagReadEvent ev = new TagReadEvent(EventType.TAG_READ,
			new Date(stamp), tt.ordinal(), tid, name, hov);
		try {
			ev.doStore();
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}
}
