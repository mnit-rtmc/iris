/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
import java.util.List;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.MapGeoLoc;
import us.mn.state.dot.tms.client.sonar.PropertiesAction;
import us.mn.state.dot.tms.client.sonar.ProxyManager;
import us.mn.state.dot.tms.client.sonar.ProxyTheme;
import us.mn.state.dot.tms.client.sonar.StyleListModel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A camera manager is a container for SONAR camera objects.
 *
 * @author Douglas Lau
 */
public class CameraManager extends ProxyManager<Camera> {

	/** Name of active style */
	static public final String STYLE_ACTIVE = "Active";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of unpublished style */
	static public final String STYLE_UNPUBLISHED = "Not published";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Color for active camera style */
	static protected final Color COLOR_ACTIVE = new Color(0, 192, 255);

	/** TMS connection */
	protected final TmsConnection connection;

	/** Create a new camera manager */
	public CameraManager(TmsConnection tc, TypeCache<Camera> c,
		GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Camera";
	}

	/** Create a styled theme for cameras */
	protected StyledTheme createTheme() {
		ProxyTheme<Camera> theme = new ProxyTheme<Camera>(this,
			getProxyType(), new CameraMarker());
		theme.addStyle(STYLE_UNPUBLISHED, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_ACTIVE, COLOR_ACTIVE);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
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
		else
			return STYLE_ALL.equals(s);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(Camera cam: s_model.getSelected()) {
				SmartDesktop desktop = connection.getDesktop();
				try {
					desktop.show(new CameraProperties(
						connection, cam));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
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
		List<Camera> sel = s_model.getSelected();
		p.add(new PublishAction(sel));
		p.add(new UnpublishAction(sel));
		return p;
	}

	/** Create a popup menu for a single camera selection */
	protected JPopupMenu createSinglePopup(Camera proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(proxy.getName() + " - " +
			GeoLocHelper.getDescription(proxy.getGeoLoc())));
		p.addSeparator();
		List<Camera> sel = s_model.getSelected();
		p.add(new PublishAction(sel));
		p.add(new UnpublishAction(sel));
		p.addSeparator();
		p.add(new PropertiesAction<Camera>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Camera proxy) {
		return proxy.getGeoLoc();
	}
}
