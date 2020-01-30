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
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.EncodingQuality;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.FlowStreamStatus;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.FlowStreamPoller;

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

	/** Update the controller and/or pin.
	 * @param oc Old controller.
	 * @param op Old pin.
	 * @param nc New controller.
	 * @param np New pin. */
	@Override
	protected void updateControllerPin(ControllerImpl oc, int op,
		ControllerImpl nc, int np)
	{
		super.updateControllerPin(oc, op, nc, np);
		FlowStreamPoller p = getFlowStreamPoller();
		if (p != null && nc != null) {
			p.sendConfig(nc);
			p.sendFlow(this);
		}
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
			updateStream();
		}
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
			updateStream();
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
			updateStream();
		}
	}

	/** Get the encoding quality ordinal */
	@Override
	public int getQuality() {
		return quality;
	}

	/** Get encoding quality */
	private EncodingQuality getEncodingQuality() {
		return EncodingQuality.fromOrdinal(quality);
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
			updateStream();
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
			updateStream();
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
			updateStream();
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

	/** Monitor camera */
	private transient Camera mon_cam;

	/** Set the monitor camera */
	public void setMonCamera(Camera cam) {
		mon_cam = isDisplayAllowed(cam) ? cam : null;
		updateStream();
	}

	/** Get the monitor camera */
	public Camera getMonCamera() {
		return mon_cam;
	}

	/** Update the flow stream */
	public void updateStream() {
		FlowStreamPoller p = getFlowStreamPoller();
		if (p != null)
			p.sendFlow(this);
	}

	/** Get the flow stream poller */
	private FlowStreamPoller getFlowStreamPoller() {
		ControllerImpl c = controller;	// Avoid race
		if (c != null) {
			DevicePoller dp = c.getPoller();
			if (dp instanceof FlowStreamPoller)
				return (FlowStreamPoller) dp;
		}
		return null;
	}

	/** Get source URI */
	public String getSourceUri() {
		Camera cam = getMonCamera();
		return (cam != null)
		      ? getCameraUri(cam, null)
		      : getCameraUri(camera, false);
	}

	/** Get a camera URI */
	private String getCameraUri(Camera cam, Boolean flow_stream) {
		EncodingQuality eq = getEncodingQuality();
		return isDisplayAllowed(cam)
		      ? CameraHelper.getUri(cam, eq, flow_stream)
		      : CameraHelper.getBlankUrl();
	}

	/** Check if a camera is published or we're not restricted */
	private boolean isDisplayAllowed(Camera cam) {
		return (cam == null) || cam.getPublish() || !restricted;
	}

	/** Get source encoding */
	public String getSourceEncoding() {
		Camera cam = getMonCamera();
		return (cam != null)
		      ? getEncoding(cam, null)
		      : getEncoding(camera, false);
	}

	/** Get camera stream encoding */
	private String getEncoding(Camera cam, Boolean flow_stream) {
		EncodingQuality eq = getEncodingQuality();
		Encoding enc = CameraHelper.getEncoding(cam, eq, flow_stream);
		return (enc != Encoding.UNKNOWN) ? enc.toString() : "PNG";
	}

	/** Get location overlay text */
	public String getOverlayText() {
		if (getLocOverlay()) {
			Camera cam = getSourceCamera();
			return (cam != null)
			      ? GeoLocHelper.getLocation(cam.getGeoLoc())
			      : "";
		} else
			return "";
	}

	/** Get the source camera */
	private Camera getSourceCamera() {
		Camera cam = getMonCamera();
		return (cam != null) ? cam : camera;
	}

	/** Get the sink address */
	public String getSinkAddress() {
		String adr = getAddress();
		if (adr != null)
			return adr;
		Camera cam = camera;
		if (cam != null) {
			adr = cam.getEncMcast();
			if (adr != null)
				return adr;
		}
		return "";
	}

	/** Get the sink port */
	public String getSinkPort() {
		Integer p = getPort();
		if (p != null)
			return p.toString();
		EncodingQuality eq = getEncodingQuality();
		EncoderStream es = CameraHelper.getStream(camera, eq, true);
		if (es != null) {
			p = es.getMcastPort();
			if (p != null)
				return p.toString();
		}
		return "";
	}

	/** Get sink encoding */
	public String getSinkEncoding() {
		Camera cam = getMonCamera();
		String enc = (cam != null)
		      ? Encoding.H264.toString()
		      : getEncoding(camera, true);
		if ("PNG".equals(enc))
			enc = getSourceEncoding();
		return (!"PNG".equals(enc)) ? enc : "H264";
	}
}
