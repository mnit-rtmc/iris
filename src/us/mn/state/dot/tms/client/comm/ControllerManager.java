/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A controller manager is a container for SONAR Controller objects.
 *
 * @author Douglas Lau
 */
public class ControllerManager extends ProxyManager<Controller> {

	/** Controller map object marker */
	static private final ControllerMarker MARKER = new ControllerMarker();

	/** Name of active style */
	static public final String STYLE_ACTIVE = "Active";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** User session */
	private final Session session;

	/** Create a new controller manager */
	public ControllerManager(Session s, TypeCache<Controller> c,
		GeoLocManager lm)
	{
		super(c, lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Controller";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for controllers */
	protected ProxyTheme<Controller> createTheme() {
		ProxyTheme<Controller> theme = new ProxyTheme<Controller>(this,
			getProxyType(), MARKER);
		theme.addStyle(STYLE_ACTIVE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_ALL, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		return theme;
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(Controller ctrl: s_model.getSelected())
				showPropertiesForm(ctrl);
		}
	}

	/** Show the properteis form for the given proxy */
	public void showPropertiesForm(Controller ctrl) {
		SmartDesktop desktop = session.getDesktop();
		desktop.show(new ControllerForm(session, ctrl));
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(Controller ctrl: s_model.getSelected())
				return createSinglePopup(ctrl);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " Controllers"));
		p.addSeparator();
		return p;
	}

	/** Create a popup menu for a single controller selection */
	protected JPopupMenu createSinglePopup(Controller proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		if(TeslaAction.isConfigured())
			p.add(new TeslaAction<Controller>(proxy));
		p.add(new PropertiesAction<Controller>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Controller proxy) {
		Cabinet cab = proxy.getCabinet();
		if(cab != null)
			return cab.getGeoLoc();
		else
			return null;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Controller proxy) {
		if(STYLE_ACTIVE.equals(s))
			return ControllerHelper.isActive(proxy);
		else if(STYLE_MAINTENANCE.equals(s))
			return ControllerHelper.needsMaintenance(proxy);
		else if(STYLE_FAILED.equals(s))
			return ControllerHelper.isFailed(proxy);
		else
			return STYLE_ALL.equals(s);
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 14;
	}
}
