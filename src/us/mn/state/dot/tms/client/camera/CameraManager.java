/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapAction;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A camera manager is a container for SONAR camera objects.
 *
 * @author Douglas Lau
 */
public class CameraManager extends ProxyManager<Camera> {

	/** Camera map object marker */
	static protected final CameraMarker MARKER = new CameraMarker();

	/** Color for active camera style */
	static protected final Color COLOR_ACTIVE = new Color(0, 192, 255);

	/** User session */
	protected final Session session;

	/** Set of cameras in the playlist */
	protected final HashSet<Camera> playlist = new HashSet<Camera>();

	/** Create a new camera manager */
	public CameraManager(Session s, TypeCache<Camera> c, GeoLocManager lm) {
		super(c, lm, ItemStyle.ACTIVE);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18N.get("camera");
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for cameras */
	protected ProxyTheme<Camera> createTheme() {
		ProxyTheme<Camera> theme = new ProxyTheme<Camera>(this, MARKER);
		theme.addStyle(ItemStyle.UNPUBLISHED,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(ItemStyle.PLAYLIST, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.ACTIVE, COLOR_ACTIVE);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, Camera proxy) {
		switch(is) {
		case ACTIVE:
			return ControllerHelper.isActive(proxy.getController());
		case INACTIVE:
			return !ControllerHelper.isActive(
				proxy.getController());
		case UNPUBLISHED:
			return !proxy.getPublish();
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case PLAYLIST:
			return inPlaylist(proxy);
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(Camera cam: s_model.getSelected())
				showPropertiesForm(cam);
		}
	}

	/** Show the properteis form for the given proxy */
	protected void showPropertiesForm(Camera cam) {
		SmartDesktop desktop = session.getDesktop();
		desktop.show(new CameraProperties(session, cam));
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(Camera cam: s_model.getSelected())
				return createSinglePopup(cam);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " +
			I18N.get("camera.plural")));
		p.addSeparator();
		p.add(new PublishAction(s_model));
		p.add(new UnpublishAction(s_model));
		p.addSeparator();
		p.add(new AddPlaylistAction(this, s_model));
		p.add(new RemovePlaylistAction(this, s_model));
		return p;
	}

	/** Create a popup menu for a single camera selection */
	protected JPopupMenu createSinglePopup(Camera proxy) {
		SmartDesktop desktop = session.getDesktop();
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new MapAction(desktop.client, proxy, proxy.getGeoLoc()));
		p.addSeparator();
		p.add(new PublishAction(s_model));
		p.add(new UnpublishAction(s_model));
		p.addSeparator();
		if(inPlaylist(proxy))
			p.add(new RemovePlaylistAction(this, s_model));
		else
			p.add(new AddPlaylistAction(this, s_model));
		p.addSeparator();
		if(TeslaAction.isConfigured())
			p.add(new TeslaAction<Camera>(proxy));
		p.add(new PropertiesAction<Camera>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
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
	protected GeoLoc getGeoLoc(Camera proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 13;
	}
}
