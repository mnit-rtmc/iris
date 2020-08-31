/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2020  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.FlowStreamHelper;
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;
import us.mn.state.dot.tms.server.event.CameraSwitchEvent;

/**
 * A video monitor device.
 *
 * @author Douglas Lau
 */
public class VideoMonitorImpl extends DeviceImpl implements VideoMonitor {

	/** Camera sequence source */
	static private final String SRC_SEQ = "seq #";

	/** Current monitor number to camera sequence mapping */
	static private HashMap<Integer, CamSequence> cam_seqs =
		new HashMap<Integer, CamSequence>();

	/** Set camera sequence for a monitor number */
	static private void setCamSequence(int mn, CamSequence seq) {
		Integer num = new Integer(mn);
		synchronized (cam_seqs) {
			if (seq != null)
				cam_seqs.put(num, seq);
			else
				cam_seqs.remove(num);
		}
	}

	/** Get camera sequence for a monitor number */
	static private CamSequence getCamSequence(int mn) {
		synchronized (cam_seqs) {
			return cam_seqs.get(new Integer(mn));
		}
	}

	/** Cam switching scheduler */
	static private final Scheduler CAM_SWITCH = new Scheduler("cam_switch");

	/** Check if the camera video should be published */
	static private boolean isCameraPublished(Camera c) {
		return c != null && c.getPublish();
	}

	/** Cast a camera to an impl or null */
	static private CameraImpl toCameraImpl(Camera c) {
		return (c instanceof CameraImpl) ? (CameraImpl) c : null;
	}

	/** Set camera on all video monitors with a given number.
	 * @param mn Monitor number.
	 * @param c Camera to display.
	 * @param src Source of command. */
	static public void setCamMirrored(final int mn, final CameraImpl c,
		final String src)
	{
		if (mn > 0) {
			CAM_SWITCH.addJob(new Job() {
				@Override
				public void perform() throws TMSException {
					doSetCamMirrored(mn, c, src, true);
				}
			});
		}
	}

	/** Set camera on all video monitors / flow streams with a given number.
	 * @param mn Monitor number.
	 * @param c Camera to display.
	 * @param src Source of command.
	 * @param select Was source a new camera selection. */
	static private void doSetCamMirrored(int mn, CameraImpl c, String src,
		boolean select) throws TMSException
	{
		Iterator<VideoMonitor> it = VideoMonitorHelper.iterator();
		while (it.hasNext()) {
			VideoMonitor m = it.next();
			if (m instanceof VideoMonitorImpl) {
				VideoMonitorImpl vm = (VideoMonitorImpl) m;
				if (vm.getMonNum() == mn)
					vm.setCamNotify(c, src, select);
			}
		}
		Iterator<FlowStream> fit = FlowStreamHelper.iterator();
		while (fit.hasNext()) {
			FlowStream f = fit.next();
			if (f instanceof FlowStreamImpl) {
				FlowStreamImpl fs = (FlowStreamImpl) f;
				Integer num = fs.getMonNum();
				if (num != null && num == mn)
					fs.setMonCamera(c);
			}
		}
	}

	/** Blank restricted video monitors viewing a camera */
	static public void blankRestrictedMonitors() {
		Iterator<VideoMonitor> it = VideoMonitorHelper.iterator();
		while (it.hasNext()) {
			VideoMonitor m = it.next();
			if (m instanceof VideoMonitorImpl) {
				VideoMonitorImpl vm = (VideoMonitorImpl) m;
				vm.blankRestricted();
			}
		}
		Iterator<FlowStream> fit = FlowStreamHelper.iterator();
		while (fit.hasNext()) {
			FlowStream f = fit.next();
			if (f instanceof FlowStreamImpl) {
				FlowStreamImpl fs = (FlowStreamImpl) f;
				fs.updateStream();
			}
		}
	}

	/** Load all the video monitors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, VideoMonitorImpl.class,
			GROUP_CHECKER);
		store.query("SELECT name, controller, pin, notes, group_n, " +
		            "mon_num, restricted, monitor_style, camera " +
		            "FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new VideoMonitorImpl(row));
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
		map.put("notes", notes);
		map.put("group_n", group_n);
		map.put("mon_num", mon_num);
		map.put("restricted", restricted);
		map.put("monitor_style", monitor_style);
		map.put("camera", camera);
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

	/** Create a video monitor */
	private VideoMonitorImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // controller
		     row.getInt(3),     // pin
		     row.getString(4),  // notes
		     row.getString(5),  // group_n
		     row.getInt(6),     // mon_num
		     row.getBoolean(7), // restricted
		     row.getString(8),  // monitor_style
		     row.getString(9)   // camera
		);
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, String c, int p, String nt,
		String gn, int mn, boolean r, String ms, String cam)
	{
		this(n, lookupController(c), p, nt, gn, mn, r,
		     lookupMonitorStyle(ms), lookupCamera(cam));
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, ControllerImpl c, int p, String nt,
		String gn, int mn, boolean r, MonitorStyle ms, Camera cam)
	{
		super(n, c, p, nt);
		group_n = gn;
		mon_num = mn;
		restricted = r;
		monitor_style = ms;
		camera = cam;
		initTransients();
	}

	/** Create a new video monitor */
	public VideoMonitorImpl(String n) throws TMSException, SonarException {
		super(n);
	}

	/** Group name */
	private String group_n;

	/** Set the group name */
	@Override
	public void setGroupN(String g) {
		group_n = g;
	}

	/** Set the group name */
	public void doSetGroupN(String g) throws TMSException {
		if (!objectEquals(g, group_n)) {
			store.update(this, "group_n", g);
			setGroupN(g);
		}
	}

	/** Get the group name */
	@Override
	public String getGroupN() {
		return group_n;
	}

	/** Monitor number */
	private int mon_num;

	/** Set the monitor number */
	@Override
	public void setMonNum(int mn) {
		mon_num = mn;
	}

	/** Set the monitor number */
	public void doSetMonNum(int mn) throws TMSException {
		if (mn != mon_num) {
			store.update(this, "mon_num", mn);
			setMonNum(mn);
		}
	}

	/** Get the monitor number */
	@Override
	public int getMonNum() {
		return mon_num;
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

	/** Blank restricted monitor */
	private void blankRestricted() {
		if (getRestricted() && !isCameraPublished(getCamera()))
			setCamSrc(null, "RESTRICTED", false);
	}

	/** Get flag to restrict publishing camera images */
	@Override
	public boolean getRestricted() {
		return restricted;
	}

	/** Monitor style */
	private MonitorStyle monitor_style;

	/** Set the monitor style */
	@Override
	public void setMonitorStyle(MonitorStyle ms) {
		monitor_style = ms;
	}

	/** Set the monitor style */
	public void doSetMonitorStyle(MonitorStyle ms) throws TMSException {
		if (ms != monitor_style) {
			store.update(this, "monitor_style", ms);
			setMonitorStyle(ms);
		}
	}

	/** Get the monitor style */
	@Override
	public MonitorStyle getMonitorStyle() {
		return monitor_style;
	}

	/** Camera displayed on the video monitor */
	private Camera camera;

	/** Set the camera displayed on the monitor */
	@Override
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the camera displayed on the monitor */
	public void doSetCamera(Camera c) {
		setCamSrc(toCameraImpl(c), getProcUser(), true);
	}

	/** Set camera already displayed (without selecting) */
	public void setCamNoSelect(Camera c, final String src) {
		if (c instanceof CameraImpl) {
			final CameraImpl ci = (CameraImpl) c;
			CAM_SWITCH.addJob(new Job() {
				@Override
				public void perform() throws TMSException {
					setCamNotify(ci, src, false);
				}
			});
		}
	}

	/** Set the camera displayed on the monitor (with mirroring).
	 * @param c Camera to display.
	 * @param src Source of request.
	 * @param select Was source a new camera selection. */
	private void setCamSrc(final CameraImpl c, final String src,
		final boolean select)
	{
		CAM_SWITCH.addJob(new Job() {
			@Override
			public void perform() throws TMSException {
				doSetCamSrc(c, src, select);
			}
		});
	}

	/** Set the camera displayed on the monitor (with mirroring).
	 * @param c Camera to display.
	 * @param src Source of request.
	 * @param select Was source a new camera selection. */
	private void doSetCamSrc(CameraImpl c, String src, boolean select)
		throws TMSException
	{
		if (mon_num > 0)
			doSetCamMirrored(mon_num, c, src, select);
		else
			setCamNotify(c, src, select);
	}

	/** Set the camera and notify clients of the change.
	 * @param c Camera to display.
	 * @param src Source of request.
	 * @param select Was source a new camera selection. */
	private void setCamNotify(CameraImpl c, String src, boolean select)
		throws TMSException
	{
		boolean r = restricted && !isCameraPublished(c);
		if (r)
			c = null;
		if (c != camera) {
			store.update(this, "camera", c);
			// Clear sequence on camera selection
			// from a source other than a sequence.
			if (select && !src.startsWith(SRC_SEQ))
				setCamSequence(mon_num, null);
			setCamera(c);
			if (select || r)
				selectCamera(c, src);
			notifyAttribute("camera");
		}
	}

	/** Get the camera displayed on the monitor */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Get the video monitor poller */
	private VideoMonitorPoller getVideoMonitorPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof VideoMonitorPoller)
		      ? (VideoMonitorPoller) dp
		      : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		VideoMonitorPoller vmp = getVideoMonitorPoller();
		if (vmp != null)
			vmp.sendRequest(this, dr);
	}

	/** Select a camera for the video monitor */
	private void selectCamera(CameraImpl cam, String src) {
		VideoMonitorPoller vmp = getVideoMonitorPoller();
		if (vmp != null)
			vmp.switchCamera(this, cam);
		String cid = (cam != null) ? cam.getName() : "";
		logEvent(new CameraSwitchEvent(getName(), cid, src));
	}

	/** Find next (or first) camera */
	private CameraImpl findNextOrFirst() {
		Camera c = getCamera();
		if (c != null) {
			Integer cn = c.getCamNum();
			if (cn != null) {
				return toCameraImpl(
					CameraHelper.findNextOrFirst(cn));
			}
		}
		return null;
	}

	/** Select the next (non-sequence) camera */
	private void nextCam(String src) throws TMSException {
		CameraImpl c = findNextOrFirst();
		if (c != null)
			doSetCamSrc(c, "NEXT " + src, true);
	}

	/** Select the next camera (sequence or global) */
	public void selectNextCam(final String src) {
		CAM_SWITCH.addJob(new Job() {
			@Override
			public void perform() throws TMSException {
				if (!nextSequence())
					nextCam(src);
			}
		});
	}

	/** Find previous (or last) camera */
	private CameraImpl findPrevOrLast() {
		Camera c = getCamera();
		if (c != null) {
			Integer cn = c.getCamNum();
			if (cn != null) {
				return toCameraImpl(
					CameraHelper.findPrevOrLast(cn));
			}
		}
		return null;
	}

	/** Select the previous (non-sequence) camera */
	private void prevCam(String src) throws TMSException {
		CameraImpl c = findPrevOrLast();
		if (c != null)
			doSetCamSrc(c, "PREV " + src, true);
	}

	/** Select the previous camera (sequence or global) */
	public void selectPrevCam(final String src) {
		CAM_SWITCH.addJob(new Job() {
			@Override
			public void perform() throws TMSException {
				if (!prevSequence())
					prevCam(src);
			}
		});
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		ControllerImpl c = controller;
		if (c != null && c.getFirstVideoMonitor() == this)
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}

	/** Set the play list.
	 * This will start the given play list from the beginning. */
	@Override
	public void setPlayList(PlayList pl) {
		CamSequence seq = (pl != null) ? new CamSequence(pl) : null;
		if (!seq.isValid())
			seq = null;
		setCamSequence(mon_num, seq);
		if (seq != null)
			CAM_SWITCH.addJob(new CamSequenceUpdateJob(seq));
	}

	/** Get the camera sequence */
	private CamSequence getCamSequence() {
		return getCamSequence(mon_num);
	}

	/** Check if monitor has selected camera sequence */
	public boolean hasSequence() {
		return getCamSequence() != null;
	}

	/** Get the camera sequence number */
	public Integer getSeqNum() {
		CamSequence seq = getCamSequence();
		return (seq != null) ? seq.getSeqNum() : null;
	}

	/** Set the camera sequence number */
	public boolean setSeqNum(Integer sn) {
		CamSequence seq = (sn != null) ? new CamSequence(sn) : null;
		if (!seq.isValid())
			seq = null;
		setCamSequence(mon_num, seq);
		if (seq != null)
			CAM_SWITCH.addJob(new CamSequenceUpdateJob(seq));
		return seq != null;
	}

	/** Check if a sequence is running */
	public boolean isSequenceRunning() {
		CamSequence seq = getCamSequence();
		return (seq != null) ? seq.isRunning() : false;
	}

	/** Pause the selected sequence */
	public boolean pauseSequence() {
		CamSequence seq = getCamSequence();
		if (seq != null)
			seq.pause();
		return seq != null;
	}

	/** Unpause the selected sequence */
	public boolean unpauseSequence() {
		CamSequence seq = getCamSequence();
		if (seq != null)
			seq.unpause();
		return seq != null;
	}

	/** Go to next item in sequence */
	private boolean nextSequence() throws TMSException {
		CamSequence seq = getCamSequence();
		if (seq != null) {
			seq.goNextItem();
			setCamFromSequence(seq);
		}
		return seq != null;
	}

	/** Go to previous item in sequence */
	private boolean prevSequence() throws TMSException {
		CamSequence seq = getCamSequence();
		if (seq != null) {
			seq.goPrevItem();
			setCamFromSequence(seq);
		}
		return seq != null;
	}

	/** Set camera from a sequence */
	private void setCamFromSequence(CamSequence seq) throws TMSException {
		Camera c = seq.getCamera();
		if (c instanceof CameraImpl) {
			CameraImpl cam = (CameraImpl) c;
			doSetCamSrc(cam, SRC_SEQ + seq.getSeqNum(), true);
		}
	}

	/** Job for updating camera sequence */
	private class CamSequenceUpdateJob extends Job {
		private final CamSequence seq;
		private CamSequenceUpdateJob(CamSequence seq) {
			super(Calendar.SECOND, 1, true);
			this.seq = seq;
		}
		@Override
		public void perform() throws TMSException {
			if (seq == getCamSequence() && isActive()) {
				seq.updateDwell();
				setCamFromSequence(seq);
			} else
				CAM_SWITCH.removeJob(this);
		}
	}
}
