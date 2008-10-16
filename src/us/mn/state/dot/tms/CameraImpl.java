/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.comm.CameraPoller;
import us.mn.state.dot.tms.comm.MessagePoller;

/**
 * CameraImpl represents a single CCTV camera.
 *
 * @author Douglas Lau
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class CameraImpl extends Device2Impl implements Camera {

	/** Load all the cameras */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading cameras...");
		namespace.registerType(SONAR_TYPE, CameraImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"encoder, encoder_channel, nvr, publish " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new CameraImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// encoder
					row.getInt(7),	// encoder_channel
					row.getString(8),	// nvr
					row.getBoolean(9)	// publish
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc.getName());
		if(controller != null)
			map.put("controller", controller.getName());
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("encoder", encoder);
		map.put("encoder_channel", encoder_channel);
		map.put("nvr", nvr);
		map.put("publish", publish);
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

	/** Create a new camera with a string name */
	public CameraImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
	}

	/** Create a camera */
	protected CameraImpl(String n, GeoLocImpl l, ControllerImpl c, int p,
		String nt, String e, int ec, String nv, boolean pb)
	{
		super(n, c, p, nt);
		geo_loc = l;
		encoder = e;
		encoder_channel = ec;
		nvr = nv;
		publish = pb;
		initTransients();
	}

	/** Create a camera */
	protected CameraImpl(Namespace ns, String n, String l, String c,
		int p, String nt, String e, int ec, String nv, boolean pb)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, l),
			(ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,
			c), p, nt, e, ec, nv, pb);
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		store.destroy(geo_loc);
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Host (and port) of encoder for digital video stream */
	protected String encoder = "";

	/** Set the video encoder host name (and port) */
	public void setEncoder(String enc) {
		encoder = enc;
	}

	/** Set the video encoder host name (and port) */
	public void doSetEncoder(String enc) throws TMSException {
		if(enc.equals(encoder))
			return;
		store.update(this, "encoder", enc);
		setEncoder(enc);
	}

	/** Get the video encoder host name (and port) */
	public String getEncoder() {
		return encoder;
	}

	/** Input channel for video stream on encoder */
	protected int encoder_channel;

	/** Set the input channel on the encoder */
	public void setEncoderChannel(int c) {
		encoder_channel = c;
	}

	/** Set the input channel on the encoder */
	public void doSetEncoderChannel(int c) throws TMSException {
		if(c == encoder_channel)
			return;
		store.update(this, "encoder_channel", c);
		setEncoderChannel(c);
	}

	/** Get the input channel on the encoder */
	public int getEncoderChannel() {
		return encoder_channel;
	}

	/** Host (and port) of NVR for digital video stream */
	protected String nvr = "";

	/** Set the video NVR host name (and port) */
	public void setNvr(String n) {
		nvr = n;
	}

	/** Set the video NVR host name (and port) */
	public void doSetNvr(String n) throws TMSException {
		if(n.equals(nvr))
			return;
		store.update(this, "nvr", n);
		setNvr(n);
	}

	/** Get the video NVR host name (and port) */
	public String getNvr() {
		return nvr;
	}

	/** Flag to allow publishing camera images */
	protected boolean publish;

	/** Set flag to allow publishing camera images */
	public void setPublish(boolean p) {
		publish = p;
	}

	/** Set flag to allow publishing camera images */
	public void doSetPublish(boolean p) throws TMSException {
		if(p == publish)
			return;
		store.update(this, "publish", p);
		setPublish(p);
		try {
			TMSImpl.unpublishCamera(this);
		}
		catch(NamespaceError e) {
			throw new TMSException(e);
		}
	}

	/** Get flag to allow publishing camera images */
	public boolean getPublish() {
		return publish;
	}

	/** Command the camera pan, tilt or zoom */
	public void setPtz(Float[] ptz) {
		// FIXME: SONAR should not send notification to clients for
		// this write-only attribute.
		if(ptz.length != 3)
			return;
		float p = ptz[0];
		float t = ptz[1];
		float z = ptz[2];
		MessagePoller mp = getPoller();
		if(mp instanceof CameraPoller) {
			CameraPoller cp = (CameraPoller)mp;
			cp.sendPTZ(this, p, t, z);
		}
	}

	/** Command the camera to set the preset */
	public void setPreset(int preset) {
		MessagePoller mp = getPoller();
		if(mp instanceof CameraPoller) {
			CameraPoller cp = (CameraPoller)mp;
			cp.sendSetPreset(this, preset);
		}
	}

	/** Command the camera to goto the preset */
	public void setGoToPreset(int preset) {
		MessagePoller mp = getPoller();
		if(mp instanceof CameraPoller) {
			CameraPoller cp = (CameraPoller)mp;
			cp.sendGoToPreset(this, preset);
		}
	}

}
