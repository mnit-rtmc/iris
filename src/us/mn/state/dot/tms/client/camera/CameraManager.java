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

import java.util.List;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
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
		};
	}

	/** Camera dispatcher */
	private final CameraDispatcher dispatcher;

	/** Camera tab */
	private final CameraTab tab;

	/** Set of cameras in the playlist */
	private final HashSet<Camera> playlist = new HashSet<Camera>();

	/** Create a new camera manager */
	public CameraManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 13, ItemStyle.ACTIVE);
		dispatcher = new CameraDispatcher(s, this);
		tab = new CameraTab(s, this, dispatcher);
		getSelectionModel().setAllowMultiple(true);
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
		if (inPlaylist(c))
			p.add(new RemovePlaylistAction(this, sel_model));
		else
			p.add(new AddPlaylistAction(this, sel_model));
		p.addSeparator();
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
		p.addSeparator();
		p.add(new AddPlaylistAction(this, sel_model));
		p.add(new RemovePlaylistAction(this, sel_model));
		return p;
	}

	/** Test if a camera is in the playlist */
	public boolean inPlaylist(Camera c) {
		synchronized(playlist) {
			return playlist.contains(c);
		}
	}

	/** Add a camera to the playlist */
	public void addPlaylist(Camera c) {
		synchronized(playlist) {
			playlist.add(c);
		}
		// FIXME: add server-side playlists
	}

	/** Remove a camera from the playlist */
	public void removePlaylist(Camera c) {
		synchronized(playlist) {
			playlist.remove(c);
		}
		// FIXME: add server-side playlists
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
