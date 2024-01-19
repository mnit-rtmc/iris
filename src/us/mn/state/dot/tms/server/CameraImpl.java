/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
 * Copyright (C) 2014       AHMCT, University of California
 * Copyright (C) 2022-2024  SRF Consulting Group
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.event.CameraVideoEvent;

/**
 * CameraImpl represents a single roadside camera.
 *
 * @author Douglas Lau
 * @author Tim Johnson
 * @author Travis Swanston
 * @author John L. Stanley - SRF Consulting
 */
public class CameraImpl extends DeviceImpl implements Camera {

	/** Invalid preset number */
	static private final int INVALID_PRESET = -1;

	/** Duration of video good/loss report "freshness" */
	static private final long VIDEO_REPORT_MS = 5000;

	/** Check if video report is stale */
	static private boolean isReportStale(long stamp, long now) {
		return stamp + VIDEO_REPORT_MS < now;
	}

	/** Load all the cameras */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CameraImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"cam_num, encoder_type, enc_address, enc_port, " +
			"enc_mcast, enc_channel, publish, streamable, " +
			"video_loss, cam_template FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
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
		map.put("enc_address", enc_address);
		map.put("enc_port", enc_port);
		map.put("enc_mcast", enc_mcast);
		map.put("enc_channel", enc_channel);
		map.put("publish", publish);
		map.put("streamable", streamable);
		map.put("video_loss", video_loss);
		map.put("cam_template", cam_template);
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
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		cam_num = null;
	}

	/** Create a camera */
	private CameraImpl(ResultSet row) throws SQLException {
		this(row.getString(1),           // name
		     row.getString(2),           // geo_loc
		     row.getString(3),           // controller
		     row.getInt(4),              // pin
		     row.getString(5),           // notes
		     (Integer) row.getObject(6), // cam_num
		     row.getString(7),           // encoder_type
		     row.getString(8),           // enc_address
		     (Integer) row.getObject(9), // enc_port
		     row.getString(10),          // enc_mcast
		     (Integer) row.getInt(11),   // enc_channel
		     row.getBoolean(12),         // publish
		     row.getBoolean(13),         // streamable
		     row.getBoolean(14),         // video_loss
		     row.getString(15)           // camera template
		);
	}

	/** Create a camera */
	private CameraImpl(String n, String l, String c, int p, String nt,
		Integer cn, String et, String ea, Integer ep, String em,
		Integer ec, boolean pb, boolean st, boolean vl, String ct)
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt, cn,
		     lookupEncoderType(et), ea, ep, em, ec, pb, st, vl,
		     lookupCameraTemplate(ct));
	}

	/** Create a camera */
	private CameraImpl(String n, GeoLocImpl l, ControllerImpl c, int p,
		String nt, Integer cn, EncoderType et, String ea, Integer ep,
		String em, Integer ec, boolean pb, boolean st, boolean vl,
		CameraTemplate ct)
	{
		super(n, c, p, nt);
		geo_loc = l;
		cam_num = cn;
		encoder_type = et;
		enc_address = ea;
		enc_port = ep;
		enc_mcast = em;
		enc_channel = ec;
		publish = pb;
		streamable = st;
		video_loss = vl;
		cam_template = ct;
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
		if (!objectEquals(et, encoder_type)) {
			store.update(this, "encoder_type", et);
			setEncoderType(et);
		}
	}

	/** Get the encoder type */
	@Override
	public EncoderType getEncoderType() {
		return encoder_type;
	}

	/** Encoder address */
	private String enc_address;

	/** Set the encoder address */
	@Override
	public void setEncAddress(String ea) {
		enc_address = ea;
	}

	/** Set the encoder address */
	public void doSetEncAddress(String ea) throws TMSException {
		if (!objectEquals(ea, enc_address)) {
			store.update(this, "enc_address", ea);
			setEncAddress(ea);
		}
	}

	/** Get the encoder address */
	@Override
	public String getEncAddress() {
		return enc_address;
	}

	/** Encoder unicast port */
	private Integer enc_port;

	/** Set the override encoder port */
	@Override
	public void setEncPort(Integer p) {
		enc_port = p;
	}

	/** Set the override encoder port */
	public void doSetEncPort(Integer p) throws TMSException {
		if (p != enc_port) {
			store.update(this, "enc_port", p);
			setEncPort(p);
		}
	}

	/** Get the override encoder port */
	@Override
	public Integer getEncPort() {
		return enc_port;
	}

	/** Encoder multicast address */
	private String enc_mcast;

	/** Set the encoder multicast address */
	@Override
	public void setEncMcast(String em) {
		enc_mcast = em;
	}

	/** Set the encoder multicast address */
	public void doSetEncMcast(String em) throws TMSException {
		if (!objectEquals(em, enc_mcast)) {
			store.update(this, "enc_mcast", em);
			setEncMcast(em);
		}
	}

	/** Get the encoder multicast URI */
	@Override
	public String getEncMcast() {
		return enc_mcast;
	}

	/** Input channel for video stream on encoder */
	private Integer enc_channel;

	/** Set the input channel on the encoder */
	@Override
	public void setEncChannel(Integer c) {
		enc_channel = c;
	}

	/** Set the input channel on the encoder */
	public void doSetEncChannel(Integer c) throws TMSException {
		if (c != enc_channel) {
			store.update(this, "enc_channel", c);
			setEncChannel(c);
		}
	}

	/** Get the input channel on the encoder */
	@Override
	public Integer getEncChannel() {
		return enc_channel;
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

	/** Streamable flag */
	private boolean streamable;

	/** Set streamable flag */
	@Override
	public void setStreamable(boolean s) {
		streamable = s;
	}

	/** Set streamable flag */
	public void doSetStreamable(boolean s) throws TMSException {
		if (s != streamable) {
			store.update(this, "streamable", s);
			setStreamable(s);
		}
	}

	/** Get streamable flag */
	@Override
	public boolean getStreamable() {
		return streamable;
	}

	/** Flag to indicate video loss */
	private boolean video_loss;

	/** Time stamp of most recent video loss report */
	private transient long video_loss_report = 0;

	/** Time stamp of most recent video good report */
	private transient long video_good_report = 0;

	/** Set flag to indicate video loss */
	private void setVideoLoss(boolean vl) {
		try {
			store.update(this, "video_loss", vl);
			video_loss = vl;
			updateStyles();
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}

	/** Set flag to indicate video loss */
	public void setVideoLossNotify(boolean vl, String mon) {
		// Clear video loss state if no encoder config
		if (!isEncoderConfigured())
			vl = false;
		long now = TimeSteward.currentTimeMillis();
		if (vl != video_loss && shouldUpdateVideoLoss(vl, now)) {
			setVideoLoss(vl);
			logEvent(vl, mon);
		}
		if (vl)
			video_loss_report = now;
		else
			video_good_report = now;
	}

	/** Log video event */
	private void logEvent(boolean vl, String mon) {
		EventType et = (vl)
			? EventType.CAMERA_VIDEO_LOST
			: EventType.CAMERA_VIDEO_RESTORED;
		logEvent(new CameraVideoEvent(et, name, mon));
	}

	/** Check if encoder is configured */
	private boolean isEncoderConfigured() {
		return getEncoderType() != null &&
		      (getEncAddress() != null || getEncMcast() != null);
	}

	/** Check if video loss flag should be updated */
	private boolean shouldUpdateVideoLoss(boolean vl, long now) {
		if (vl) {
			return isReportStale(video_good_report, now)
			   && !isReportStale(video_loss_report, now);
		} else
			return true;
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
	public void periodicPoll(boolean is_long) {
		if (!is_long) {
			// make sure controller is not failed
			clearFailed();
			// NOTE: This is a workaround for a Cohu bug, which
			//       never cleans up dropped TCP connections and
			//       eventually flakes out.  Firewalls will drop a
			//       TCP connection with no traffic, so send
			//       something as a keep-alive.
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
		}
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
		if (cp != null) {
			last_preset = INVALID_PRESET;
			cp.sendPTZ(this, p, t, z);
			if ((p != 0.0) || (t != 0.0) || (z != 0.0))
				savePtzInfo();
		}
	}

	/** Last commanded preset number */
	private transient int last_preset = INVALID_PRESET;

	/** Command the camera to store a preset */
	@Override
	public void setStorePreset(int preset) {
		CameraPoller cp = getCameraPoller();
		if (cp != null) {
			last_preset = preset;
			cp.sendStorePreset(this, preset);
		}
	}

	/** Command the camera to recall a preset */
	@Override
	public void setRecallPreset(int preset) {
		CameraPoller cp = getCameraPoller();
		if (cp != null) {
			last_preset = preset;
			cp.sendRecallPreset(this, preset);
			savePtzInfo();
		}
	}

	/** Get the active status */
	@Override
	public boolean isActive() {
		// since controller is only for PTZ,
		// it can be null for active cameras
		ControllerImpl c = controller;	// Avoid race
		return (c == null) || c.isActive();
	}

	/** Get the failure status */
	@Override
	public boolean isFailed() {
		// since controller is only for PTZ,
		// treat camera without controller as not failed
		ControllerImpl c = controller;	// Avoid race
		return (c != null) && c.isFailed();
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (!getPublish())
			s |= ItemStyle.UNPUBLISHED.bit();
		if (video_loss)
			s |= ItemStyle.VIDEO_LOSS.bit();
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
		        GeoLocHelper.getLocation(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
			        formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
			        formatDouble(pos.getLatitude())));
		}
		w.write("/>\n");
	}

	/** Camera template name */
	private CameraTemplate cam_template;

	/** Set the camera template */
	@Override
	public void setCameraTemplate(CameraTemplate ct) {
		cam_template = ct;
	}

	/** Set the camera template */
	public void doSetCameraTemplate(CameraTemplate ct) throws TMSException {
		if (!ct.equals(cam_template)) {
			store.update(this, "cam_template", ct);
			setCameraTemplate(ct);
		}
	}

	/** Get the camera template */
	@Override
	public CameraTemplate getCameraTemplate() {
		return cam_template;
	}

	/** Username of last user to attempt a PTZ or
	 *  preset-recall operation.
	 *  Resets at every server restart. */
	private String ptz_user = "";

	/** Timestamp (Epoch seconds) of last attempted 
	 *  PTZ or preset-recall operation.
	 *  Resets at every server restart. */
	private long ptz_timestamp = 0;

	/** Save the current SONAR username as the
	 *  most recent PTZ user and the current 
	 *  Epoch timestamp as the PTZ timestamp. */
	public void savePtzInfo() {
		ptz_user      = getProcUser();
		ptz_timestamp = java.time.Instant.now().getEpochSecond();
		notifyAttribute("ptzUser");
		notifyAttribute("ptzTimestamp");
	}

	/** Get name of last user to attempt a camera motion
	 *  (PTZ or camera preset-recall).
	 *  Returns empty string if no attempt has been made. */
	public String getPtzUser() {
		return ptz_user;
	}

	/** Get Epoch timestamp when latest camera motion
	 *  (PTZ or camera preset-recall) was attempted.
	 *  Returns zero if no attempt has been made. */
	public long getPtzTimestamp() {
		return ptz_timestamp;
	}
}
