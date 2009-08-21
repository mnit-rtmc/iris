/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import java.util.List;
import java.util.HashSet;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A camera manager is a container for SONAR camera objects.
 *
 * @author Douglas Lau
 */
public class CameraManager extends ProxyManager<Camera> {

	/** Camera map object shape */
	static protected final Shape SHAPE = new CameraMarker();

	/** Name of active style */
	static public final String STYLE_ACTIVE = "Active";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of unpublished style */
	static public final String STYLE_UNPUBLISHED = "Not published";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of "playlist" style */
	static public final String STYLE_PLAYLIST = "Playlist";

	/** Color for active camera style */
	static protected final Color COLOR_ACTIVE = new Color(0, 192, 255);

	/** User session */
	protected final Session session;

	/** Set of cameras in the playlist */
	protected final HashSet<Camera> playlist = new HashSet<Camera>();

	/** Create a new camera manager */
	public CameraManager(Session s, TypeCache<Camera> c, GeoLocManager lm) {
		super(c, lm);
		session = s;
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Camera";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(Camera proxy, float scale) {
		return new CameraMarker(20 * scale);
	}

	/** Create a styled theme for cameras */
	protected StyledTheme createTheme() {
		ProxyTheme<Camera> theme = new ProxyTheme<Camera>(this,
			getProxyType(), SHAPE);
		theme.addStyle(STYLE_UNPUBLISHED, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_PLAYLIST, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_ACTIVE, COLOR_ACTIVE);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<Camera> createStyleSummary() {
		StyleSummary<Camera> summary = super.createStyleSummary();
		summary.setStyle(STYLE_ACTIVE);
		return summary;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Camera proxy) {
		if(STYLE_ACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr != null && ctr.getActive();
		} else if(STYLE_INACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr == null || !ctr.getActive();
		} else if(STYLE_UNPUBLISHED.equals(s))
			return !proxy.getPublish();
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else if(STYLE_PLAYLIST.equals(s))
			return inPlaylist(proxy);
		else
			return STYLE_ALL.equals(s);
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
		try {
			desktop.show(new CameraProperties(session, cam));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
		p.add(new javax.swing.JLabel("" + n_selected + " Cameras"));
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
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
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
		StyleListModel<Camera> p_model = getStyleModel(STYLE_PLAYLIST);
		p_model.proxyChanged(c, STYLE_PLAYLIST);
	}

	/** Remove a camera from the playlist */
	public void removePlaylist(Camera c) {
		synchronized(playlist) {
			playlist.remove(c);
		}
		StyleListModel<Camera> p_model = getStyleModel(STYLE_PLAYLIST);
		p_model.proxyChanged(c, STYLE_PLAYLIST);
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Camera proxy) {
		return proxy.getGeoLoc();
	}
}
