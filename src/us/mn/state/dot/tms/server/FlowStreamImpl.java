/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncodingQuality;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.FlowStreamStatus;
import us.mn.state.dot.tms.TMSException;

/**
 * A flow stream "device".
 *
 * @author Douglas Lau
 */
public class FlowStreamImpl extends ControllerIoImpl implements FlowStream {

	/** Load all the flow streams */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, FlowStreamImpl.class);
		store.query("SELECT name, controller, pin, restricted, " +
			"loc_overlay, quality, camera, mon_num, address, " +
			"port, status FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new FlowStreamImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("restricted", restricted);
		map.put("loc_overlay", loc_overlay);
		map.put("quality", quality);
		map.put("camera", camera);
		map.put("mon_num", mon_num);
		map.put("address", address);
		map.put("port", port);
		map.put("status", status);
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

	/** Create a new flow stream */
	public FlowStreamImpl(String n) {
		super(n, null, 0);
		quality = EncodingQuality.MEDIUM.ordinal();
		status = FlowStreamStatus.FAILED.ordinal();
	}

	/** Create a flow stream */
	private FlowStreamImpl(ResultSet row) throws SQLException {
		this(row.getString(1),            // name
		     row.getString(2),            // controller
		     row.getInt(3),               // pin
		     row.getBoolean(4),           // restricted
		     row.getBoolean(5),           // loc_overlay
		     row.getInt(6),               // quality
		     row.getString(7),            // camera
		     (Integer) row.getObject(8),  // mon_num
		     row.getString(9),            // address
		     (Integer) row.getObject(10), // port
		     row.getInt(11)               // status
		);
	}

	/** Create a flow stream */
	private FlowStreamImpl(String n, String c, int p, boolean r, boolean lo,
		int q, String cam, Integer  mn, String a, Integer pt, int s)
	{
		super(n, lookupController(c), p);
		restricted = r;
		loc_overlay = lo;
		quality = q;
		camera = lookupCamera(cam);
		mon_num = mn;
		address = a;
		port = pt;
		status = s;
		initTransients();
	}

	/** Flag to restrict publishing camera images */
	private boolean restricted;

	/** Set flag to restrict publishing camera images */
	@Override
	public void setRestricted(boolean r) {
		restricted = r;
	}

	/** Set flag to restrict publishing camera images */
	public void doSetRestricted(boolean r) throws TMSException {
		if (r != restricted) {
			store.update(this, "restricted", r);
			setRestricted(r);
			blankRestricted();
		}
	}

	/** Blank restricted stream */
	private void blankRestricted() {
		// FIXME
	}

	/** Get flag to restrict publishing camera images */
	@Override
	public boolean getRestricted() {
		return restricted;
	}

	/** Flag to enable location overlay text */
	private boolean loc_overlay;

	/** Set flag to enable location overlay text */
	@Override
	public void setLocOverlay(boolean lo) {
		loc_overlay = lo;
	}

	/** Set flag to enable location overlay text */
	public void doSetLocOverlay(boolean lo) throws TMSException {
		if (lo != loc_overlay) {
			store.update(this, "loc_overlay", lo);
			setLocOverlay(lo);
		}
	}

	/** Get flag to enable location overlay text */
	@Override
	public boolean getLocOverlay() {
		return loc_overlay;
	}

	/** Encoding quality ordinal */
	private int quality;

	/** Set the encoding quality ordinal */
	@Override
	public void setQuality(int q) {
		quality = q;
	}

	/** Set the encoding quality ordinal */
	public void doSetQuality(int q) throws TMSException {
		if (q != quality) {
			store.update(this, "quality", q);
			setQuality(q);
		}
	}

	/** Get the encoding quality ordinal */
	@Override
	public int getQuality() {
		return quality;
	}

	/** Source camera */
	private Camera camera;

	/** Set the source camera */
	@Override
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the source camera */
	public void doSetCamera(Camera c) throws TMSException {
		if (c != camera) {
			store.update(this, "camera", c);
			setCamera(c);
		}
	}

	/** Get the source camera */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Source monitor number */
	private Integer mon_num;

	/** Set the source monitor number */
	@Override
	public void setMonNum(Integer mn) {
		mon_num = mn;
	}

	/** Set the source monitor number */
	public void doSetMonNum(Integer  mn) throws TMSException {
		if (!objectEquals(mn, mon_num)) {
			store.update(this, "mon_num", mn);
			setMonNum(mn);
		}
	}

	/** Get the source monitor number */
	@Override
	public Integer getMonNum() {
		return mon_num;
	}

	/** Sink address */
	private String address;

	/** Set the sink address */
	@Override
	public void setAddress(String a) {
		address = a;
	}

	/** Set the sink address */
	public void doSetAddress(String a) throws TMSException {
		if (!objectEquals(a, address)) {
			store.update(this, "address", a);
			setAddress(a);
		}
	}

	/** Get the sink address */
	@Override
	public String getAddress() {
		return address;
	}

	/** Sink port */
	private Integer port;

	/** Set the sink port */
	@Override
	public void setPort(Integer p) {
		port = p;
	}

	/** Set the sink port */
	public void doSetPort(Integer p) throws TMSException {
		if (!objectEquals(p, port)) {
			store.update(this, "port", p);
			setPort(p);
		}
	}

	/** Get the sink port */
	@Override
	public Integer getPort() {
		return port;
	}

	/** Status ordinal */
	private int status;

	/** Set the status ordinal */
	public void setStatusNotify(int s) throws TMSException {
		if (s != status) {
			store.update(this, "status", s);
			status = s;
			notifyAttribute("status");
		}
	}

	/** Get the status ordinal */
	@Override
	public int getStatus() {
		return status;
	}
}
