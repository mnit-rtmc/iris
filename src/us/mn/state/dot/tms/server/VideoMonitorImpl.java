/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.SystemAttrEnum;
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

	/** Get the play list dwell time (seconds) */
	static private int getDwellSec() {
		return SystemAttrEnum.CAMERA_PLAYLIST_DWELL_SEC.getInt();
 	}

	/** Dwell time paused value */
	static private final int DWELL_PAUSED = -1;

	/** Play list state */
	static private class PlayListState {

		/** Create play list state */
		private PlayListState(PlayList pl) {
			play_list = pl;
			item = -1;	// nextItem will advance to 0
			dwell = 0;
		}

		/** Running play list */
		private final PlayList play_list;

		/** Item in play list */
		private int item;

		/** Remaining dwell time (negative means paused) */
		private int dwell;

		/** Pause the play list */
		private void pause() {
			dwell = DWELL_PAUSED;
		}

		/** Unpause the play list */
		private void unpause() {
			dwell = getDwellSec();
		}

		/** Update dwell time */
		private Camera updateDwell() {
			if (dwell > 0) {
				dwell--;
				return null;
			} else if (0 == dwell) {
				dwell = getDwellSec();
				return nextItem();
			} else {
				// paused
				return null;
			}
		}

		/** Get next item */
		private Camera nextItem() {
			Camera[] cams = play_list.getCameras();
			item = (item + 1 < cams.length) ? item + 1 : 0;
			return (item < cams.length) ? cams[item] : null;
		}

		/** Go to the next item */
		private void goNextItem() {
			resetDwell();
			Camera[] cams = play_list.getCameras();
			item = (item + 1 < cams.length) ? item + 1 : 0;
		}

		/** Go to the previous item */
		private void goPrevItem() {
			resetDwell();
			Camera[] cams = play_list.getCameras();
			item = (item > 0) ? item - 1 : cams.length - 1;
		}

		/** Reset dwell time */
		private void resetDwell() {
			dwell = (dwell >= 0) ? getDwellSec() : DWELL_PAUSED;
		}
	}

	/** Play list switching scheduler */
	static private final Scheduler PLAY_LIST = new Scheduler("play_list");

	/** Check if the camera video should be published */
	static private boolean isCameraPublished(Camera c) {
		return c != null && c.getPublish();
	}

	/** Cast a camera to an impl or null */
	static private CameraImpl toCameraImpl(Camera c) {
		return (c instanceof CameraImpl) ? (CameraImpl) c : null;
	}

	/** Set camera on all video monitors with a given number */
	static public void setCameraNotify(int mn, CameraImpl c, String src) {
		setCameraNotify(null, mn, c, src);
	}

	/** Set camera on all video monitors with a given number.
	 * @param svm Video monitor to skip.
	 * @param mn Monitor number.
	 * @param c Camera to display.
	 * @param src Source of command. */
	static private void setCameraNotify(VideoMonitorImpl svm, int mn,
		CameraImpl c, String src)
	{
		Iterator<VideoMonitor> it = VideoMonitorHelper.iterator();
		while (it.hasNext()) {
			VideoMonitor m = it.next();
			if (svm != m && (m instanceof VideoMonitorImpl)) {
				VideoMonitorImpl vm = (VideoMonitorImpl) m;
				if (vm.getMonNum() == mn)
					vm.setCameraNotify(c, src, true);
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
	}

	/** Load all the video monitors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, VideoMonitorImpl.class);
		store.query("SELECT name, controller, pin, notes, mon_num, " +
		            "restricted, monitor_style, camera FROM iris." +
		            SONAR_TYPE + ";", new ResultFactory()
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
		this(row.getString(1),		// name
		     row.getString(2),		// controller
		     row.getInt(3),		// pin
		     row.getString(4),		// notes
		     row.getInt(5),		// mon_num
		     row.getBoolean(6),		// restricted
		     row.getString(7),		// monitor_style
		     row.getString(8)		// camera
		);
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, String c, int p, String nt, int mn,
		boolean r, String ms, String cam)
	{
		this(n, lookupController(c), p, nt, mn, r,
		     lookupMonitorStyle(ms), lookupCamera(cam));
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, ControllerImpl c, int p, String nt,
		int mn, boolean r, MonitorStyle ms, Camera cam)
	{
		super(n, c, p, nt);
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
		if (r == restricted)
			return;
		store.update(this, "restricted", r);
		setRestricted(r);
		blankRestricted();
	}

	/** Blank restricted monitor */
	private void blankRestricted() {
		if (getRestricted() && !isCameraPublished(getCamera()))
			setCameraNotify(null, "RESTRICTED", false);
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
	public void doSetCamera(Camera c) throws TMSException {
		setCamSrc(toCameraImpl(c), getProcUser());
	}

	/** Set the camera displayed on the monitor.
	 * @param c Camera to display.
	 * @param src Source of request. */
	private void setCamSrc(CameraImpl c, String src) throws TMSException {
		if (doSetCam(c, src, true)) {
			// Switch all other monitors with same mon_num
			if (mon_num > 0)
				setCameraNotify(this, mon_num, c, src);
		}
	}

	/** Set the camera displayed on the monitor.
	 * @param c Camera to display.
	 * @param src Source of request.
	 * @param select Was source a new camera selection.
	 * @return true if switch was permitted. */
	private boolean doSetCam(CameraImpl c, String src, boolean select)
		throws TMSException
	{
		boolean r = restricted && !isCameraPublished(c);
		if (r)
			c = null;
		if (c != camera) {
			store.update(this, "camera", c);
			if (!PlayList.SONAR_TYPE.equals(src))
				setPlayList(null);
			setCamera(c);
			if (select || r)
				selectCamera(c, src);
		}
		return !r;
	}

	/** Set the camera and notify clients of the change */
	public void setCameraNotify(CameraImpl c, String src, boolean select) {
		try {
			Camera oc = camera;
			doSetCam(c, src, select);
			if (camera != oc)
				notifyAttribute("camera");
		}
		catch (TMSException e) {
			e.printStackTrace();
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
		// NOTE: we need to iterate through all controllers to support
		//       Pelco switcher protocol.  Otherwise, we could just
		//       call getController here.
		selectCameraWithSwitcher(cam);
		String cid = (cam != null) ? cam.getName() : "";
		logEvent(new CameraSwitchEvent(getName(), cid, src));
	}

	/** Select a camera for the video monitor with a switcher */
	private void selectCameraWithSwitcher(CameraImpl cam) {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			if (c instanceof ControllerImpl)
				selectCamera((ControllerImpl) c, cam);
		}
	}

	/** Select a camera for the video monitor */
	private void selectCamera(ControllerImpl c, CameraImpl cam) {
		DevicePoller dp = c.getPoller();
		if (dp instanceof VideoMonitorPoller) {
			VideoMonitorPoller vmp = (VideoMonitorPoller) dp;
			vmp.switchCamera(c, this, cam);
		}
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}

	/** Current play list state */
	private transient PlayListState pl_state;

	/** Set the play list.
	 * This will start the given play list from the beginning. */
	public void setPlayList(PlayList pl) {
		PlayListState pls = (pl != null) ? new PlayListState(pl) : null;
		pl_state = pls;
		if (pls != null)
			PLAY_LIST.addJob(new PlayListUpdateJob(pls));
	}

	/** Get the play list */
	public PlayList getPlayList() {
		PlayListState pls = pl_state;
		return (pls != null) ? pls.play_list : null;
	}

	/** Pause the running play list */
	public boolean pausePlayList() {
		PlayListState pls = pl_state;
		if (pls != null)
			pls.pause();
		return pls != null;
	}

	/** Unpause the running play list */
	public boolean unpausePlayList() {
		PlayListState pls = pl_state;
		if (pls != null)
			pls.unpause();
		return pls != null;
	}

	/** Go to next item in play list */
	public void nextPlayList() {
		PlayListState pls = pl_state;
		if (pls != null)
			pls.goNextItem();
	}

	/** Go to previous item in play list */
	public void prevPlayList() {
		PlayListState pls = pl_state;
		if (pls != null)
			pls.goPrevItem();
	}

	/** Job for updating play list state */
	private class PlayListUpdateJob extends Job {
		private final PlayListState pls;
		private PlayListUpdateJob(PlayListState pls) {
			super(Calendar.SECOND, 1);
			this.pls = pls;
		}
		@Override
		public void perform() throws TMSException {
			if (pls == pl_state) {
				Camera c = pls.updateDwell();
				if (c != null) {
					setCamSrc(toCameraImpl(c),
						PlayList.SONAR_TYPE);
				}
			} else {
				PLAY_LIST.removeJob(this);
			}
		}
	}
}
