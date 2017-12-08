/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A camera manager is a container for SONAR camera objects.
 *
 * @author Douglas Lau
 */
public class CameraManager extends DeviceManager<Camera> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Camera> descriptor(final Session s) {
		return new ProxyDescriptor<Camera>(
			s.getSonarState().getCamCache().getCameras(), true
		) {
			@Override
			public CameraProperties createPropertiesForm(
				Camera cam)
			{
				return new CameraProperties(s, cam);
			}
			@Override
			public CameraForm makeTableForm() {
				return new CameraForm(s);
			}
		};
	}

	/** Check if an array contains a camera */
	static private boolean containsCam(Camera[] cams, Camera c) {
		for (Camera cm: cams) {
			if (c.equals(cm))
				return true;
		}
		return false;
	}

	/** Camera dispatcher */
	private final CameraDispatcher dispatcher;

	/** Camera tab */
	private final CameraTab tab;

	/** Selected play list */
	private PlayList play_list;

	/** Play list watcher */
	private final ProxyWatcher<PlayList> watcher;

	/** Play list view */
	private final ProxyView<PlayList> pl_view = new ProxyView<PlayList>() {
		public void update(PlayList pl, String a) {
			play_list = pl;
		}
		public void clear() {
			play_list = null;
		}
	};

	/** Create a new camera manager */
	public CameraManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 13, ItemStyle.ACTIVE);
		dispatcher = new CameraDispatcher(s, this);
		tab = new CameraTab(s, this, dispatcher);
		getSelectionModel().setAllowMultiple(true);
		watcher = new ProxyWatcher<PlayList>(s.getSonarState()
			.getCamCache().getPlayLists(), pl_view, true);
	}

	/** Initialize the manager */
	@Override
	public void initialize() {
		watcher.initialize();
		super.initialize();
		String n = "PL_" + session.getUser().getName();
		PlayList pl = PlayListHelper.lookup(n);
		if (pl != null)
			watcher.setProxy(pl);
		else if (session.isWritePermitted(PlayList.SONAR_TYPE)) {
			session.getSonarState().getCamCache().getPlayLists()
				.createObject(n);
		}
	}

	/** Dispose of the manager */
	@Override
	public void dispose() {
		super.dispose();
		watcher.dispose();
	}

	/** Check if user can read cameras / video_monitors */
	@Override
	public boolean canRead() {
		return super.canRead() &&
		       session.canRead(VideoMonitor.SONAR_TYPE);
	}

	/** Create a camera map tab */
	@Override
	public CameraTab createTab() {
		return tab;
	}

	/** Create a theme for cameras */
	@Override
	protected CameraTheme createTheme() {
		return new CameraTheme(this);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, Camera proxy) {
		if (is == ItemStyle.PLAYLIST)
			return inPlaylist(proxy);
		else
			return super.checkStyle(is, proxy);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, Camera c) {
		ProxySelectionModel<Camera> sel_model = getSelectionModel();
		p.add(new PublishAction(sel_model));
		p.add(new UnpublishAction(sel_model));
		p.addSeparator();
		if (canEditPlayList(play_list)) {
			if (inPlaylist(c))
				p.add(new RemovePlaylistAction(this,sel_model));
			else
				p.add(new AddPlaylistAction(this, sel_model));
			p.addSeparator();
		}
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		ProxySelectionModel<Camera> sel_model = getSelectionModel();
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("camera.title") + ": " +
			n_selected));
		p.addSeparator();
		p.add(new PublishAction(sel_model));
		p.add(new UnpublishAction(sel_model));
		if (canEditPlayList(play_list)) {
			p.addSeparator();
			p.add(new AddPlaylistAction(this, sel_model));
			p.add(new RemovePlaylistAction(this, sel_model));
		}
		return p;
	}

	/** Test if a camera is in the play list */
	public boolean inPlaylist(Camera c) {
		PlayList pl = play_list;
		return pl != null && containsCam(pl.getCameras(), c);
	}

	/** Add a camera to the play list */
	public void addPlaylist(Camera c) {
		PlayList pl = play_list;
		if (canEditPlayList(pl)) {
			ArrayList<Camera> cams = new ArrayList<Camera>(
				Arrays.asList(pl.getCameras()));
			cams.add(c);
			pl.setCameras(cams.toArray(new Camera[0]));
		}
	}

	/** Check if the user can edit a play list */
	private boolean canEditPlayList(PlayList pl) {
		return session.isWritePermitted(pl, "cameras");
	}

	/** Remove a camera from the play list */
	public void removePlaylist(Camera c) {
		PlayList pl = play_list;
		if (canEditPlayList(pl)) {
			ArrayList<Camera> cams = new ArrayList<Camera>(
				Arrays.asList(pl.getCameras()));
			cams.remove(c);
			pl.setCameras(cams.toArray(new Camera[0]));
		}
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(Camera proxy) {
		return proxy.getGeoLoc();
	}

	/** Select a camera */
	public void selectCamera(Camera c) {
		if (tab.isSelectedTab()) {
			if (c != null)
				getSelectionModel().setSelected(c);
			else
				getSelectionModel().clearSelection();
		} else
			dispatcher.selectMonitorCamera(c);
	}

	/** Select a video monitor */
	public void selectMonitor(VideoMonitor m) {
		dispatcher.selectMonitor(m);
	}

	/** Select the next camera */
	public void selectNextCamera() {
		dispatcher.selectNextCamera();
	}

	/** Select the previous camera */
	public void selectPreviousCamera() {
		dispatcher.selectPreviousCamera();
	}

	/** Get the selected video monitor */
	public VideoMonitor getSelectedMonitor() {
		return dispatcher.getSelectedMonitor();
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(Camera proxy) {
		Integer num = proxy.getCamNum();
		if (num != null) {
			return proxy.getName() + " - #" + num + " - " +
				GeoLocHelper.getDescription(getGeoLoc(proxy));
		} else
			return super.getDescription(proxy);
	}
}
