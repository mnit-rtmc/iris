/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;

/**
 * CameraImpl represents a single CCTV camera.
 *
 * @author Douglas Lau
 * @author Tim Johnson
 * @author Travis Swanston
 */
public class CameraImpl extends DeviceImpl implements Camera {

	/** Load all the cameras */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CameraImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"cam_num, encoder_type, encoder, enc_mcast, " +
			"encoder_channel, publish FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CameraImpl(row));
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
		map.put("cam_num", cam_num);
		map.put("encoder_type", encoder_type);
		map.put("encoder", encoder);
		map.put("enc_mcast", enc_mcast);
		map.put("encoder_channel", encoder_channel);
		map.put("publish", publish);
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

	/** Create a new camera with a string name */
	public CameraImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
		cam_num = null;
	}

	/** Create a camera */
	private CameraImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// geo_loc
		     row.getString(3),		// controller
		     row.getInt(4),		// pin
		     row.getString(5),		// notes
		     (Integer) row.getObject(6),// cam_num
		     row.getString(7),		// encoder_type
		     row.getString(8),		// encoder
		     row.getString(9),		// enc_mcast
		     row.getInt(10),		// encoder_channel
		     row.getBoolean(11)		// publish
		);
	}

	/** Create a camera */
	private CameraImpl(String n, String l, String c, int p, String nt,
		Integer cn, String et, String e, String em, int ec, boolean pb)
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt, cn,
		     lookupEncoderType(et), e, em, ec, pb);
	}

	/** Create a camera */
	private CameraImpl(String n, GeoLocImpl l, ControllerImpl c, int p,
		String nt, Integer cn, EncoderType et, String e, String em,
		int ec, boolean pb)
	{
		super(n, c, p, nt);
		geo_loc = l;
		cam_num = cn;
		encoder_type = et;
		encoder = e;
		enc_mcast = em;
		encoder_channel = ec;
		publish = pb;
		initTransients();
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

	/** Camera number */
	private Integer cam_num;

	/** Set the camera number */
	@Override
	public void setCamNum(Integer cn) {
		cam_num = cn;
	}

	/** Set the camera number */
	public void doSetCamNum(Integer cn) throws TMSException {
		if (cn != cam_num) {
			if (cn != null && (cn < CAM_NUM_MIN || cn > CAM_NUM_MAX))
				throw new ChangeVetoException("Invalid cam_num");
			store.update(this, "cam_num", cn);
			setCamNum(cn);
		}
	}

	/** Get the camera number */
	@Override
	public Integer getCamNum() {
		return cam_num;
	}

	/** Encoder type */
	private EncoderType encoder_type;

	/** Set the encoder type */
	@Override
	public void setEncoderType(EncoderType et) {
		encoder_type = et;
	}

	/** Set the encoder type */
	public void doSetEncoderType(EncoderType et) throws TMSException {
		if (et != encoder_type) {
			store.update(this, "encoder_type", et);
			setEncoderType(et);
		}
	}

	/** Get the encoder type */
	@Override
	public EncoderType getEncoderType() {
		return encoder_type;
	}

	/** Encoder stream URI */
	private String encoder = "";

	/** Set the encoder stream URI */
	@Override
	public void setEncoder(String enc) {
		encoder = enc;
	}

	/** Set the encoder stream URI */
	public void doSetEncoder(String enc) throws TMSException {
		if (!enc.equals(encoder)) {
			store.update(this, "encoder", enc);
			setEncoder(enc);
		}
	}

	/** Get the encoder stream URI */
	@Override
	public String getEncoder() {
		return encoder;
	}

	/** Encoder multicast URI */
	private String enc_mcast = "";

	/** Set the encoder multicast URI */
	@Override
	public void setEncMulticast(String em) {
		enc_mcast = em;
	}

	/** Set the encoder multicast URI */
	public void doSetEncMulticast(String em) throws TMSException {
		if (!em.equals(enc_mcast)) {
			store.update(this, "enc_mcast", em);
			setEncMulticast(em);
		}
	}

	/** Get the encoder multicast URI */
	@Override
	public String getEncMulticast() {
		return enc_mcast;
	}

	/** Input channel for video stream on encoder */
	private int encoder_channel;

	/** Set the input channel on the encoder */
	@Override
	public void setEncoderChannel(int c) {
		encoder_channel = c;
	}

	/** Set the input channel on the encoder */
	public void doSetEncoderChannel(int c) throws TMSException {
		if (c == encoder_channel)
			return;
		store.update(this, "encoder_channel", c);
		setEncoderChannel(c);
	}

	/** Get the input channel on the encoder */
	@Override
	public int getEncoderChannel() {
		return encoder_channel;
	}

	/** Flag to allow publishing camera images */
	private boolean publish;

	/** Set flag to allow publishing camera images */
	@Override
	public void setPublish(boolean p) {
		publish = p;
	}

	/** Set flag to allow publishing camera images */
	public void doSetPublish(boolean p) throws TMSException {
		if (p != publish) {
			store.update(this, "publish", p);
			setPublish(p);
			if (!p)
				VideoMonitorImpl.blankRestrictedMonitors();
			updateStyles();
		}
	}

	/** Get flag to allow publishing camera images */
	@Override
	public boolean getPublish() {
		return publish;
	}

	/** Get the camera poller */
	private CameraPoller getCameraPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof CameraPoller) ? (CameraPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		CameraPoller cp = getCameraPoller();
		if (cp != null)
			cp.sendRequest(this, dr);
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		// make sure controller is not failed
		clearFailed();
		// NOTE: Firewalls will drop a TCP connection with no traffic,
		//       so send something as a keep-alive.  This is a
		//       workaround for a Cohu bug, which never cleans up
		//       dropped TCP connections and eventually flakes out.
		sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}

	/** Clear the camera failed status */
	private void clearFailed() {
		ControllerImpl ctl = controller;
		if (ctl != null)
			ctl.setFailed(false);
	}

	/** Command the camera pan, tilt or zoom */
	@Override
	public void setPtz(Float[] ptz) {
		if (checkPtz(ptz))
			sendPTZ(ptz[0], ptz[1], ptz[2]);
	}

	/** Check for valid PTZ parameters */
	private boolean checkPtz(Float[] ptz) {
		return (ptz != null) && (ptz.length == 3)
		    && (ptz[0] != null) && (ptz[1] != null) && (ptz[2] != null);
	}

	/** Send pan, tilt, zoom command */
	public void sendPTZ(float p, float t, float z) {
		CameraPoller cp = getCameraPoller();
		if (cp != null)
			cp.sendPTZ(this, p, t, z);
	}

	/** Command the camera to store a preset */
	@Override
	public void setStorePreset(int preset) {
		CameraPoller cp = getCameraPoller();
		if (cp != null)
			cp.sendStorePreset(this, preset);
	}

	/** Command the camera to recall a preset */
	@Override
	public void setRecallPreset(int preset) {
		CameraPoller cp = getCameraPoller();
		if (cp != null)
			cp.sendRecallPreset(this, preset);
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (!getPublish())
			s |= ItemStyle.UNPUBLISHED.bit();
		return s;
	}

	/** Print camera as an XML element */
	public void writeXml(Writer w) throws IOException {
		if (isActive())
			doWriteXml(w);
	}

	/** Write camera an an XML element */
	private void doWriteXml(Writer w) throws IOException {
		w.write("<camera");
		w.write(createAttribute("name", getName()));
		w.write(createAttribute("description",
		        GeoLocHelper.getDescription(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
			        formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
			        formatDouble(pos.getLatitude())));
		}
		w.write("/>\n");
	}
}
