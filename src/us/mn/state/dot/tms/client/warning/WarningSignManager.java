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
package us.mn.state.dot.tms.client.warning;

import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A warning sign manager is a container for SONAR warning sign objects.
 *
 * @author Douglas Lau
 */
public class WarningSignManager extends ProxyManager<WarningSign> {

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "Deployed";

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** TMS connection */
	protected final TmsConnection connection;

	/** Create a new warning sign manager */
	public WarningSignManager(TmsConnection tc, TypeCache<WarningSign> c,
		GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Warning Sign";
	}

	/** Create a styled theme for warning signs */
	protected StyledTheme createTheme() {
		ProxyTheme<WarningSign> theme =new ProxyTheme<WarningSign>(this,
			getProxyType(), new WarningSignMarker());
		theme.addStyle(STYLE_DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, WarningSign proxy) {
		if(STYLE_DEPLOYED.equals(s))
			return proxy.getDeployed();
		else if(STYLE_AVAILABLE.equals(s)) {
			return (!isControllerFailed(proxy.getController())) &&
				!proxy.getDeployed();
		} else if(STYLE_FAILED.equals(s))
			return isControllerFailed(proxy.getController());
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else
			return STYLE_ALL.equals(s);
	}

	/** Test if a controller is failed */
	static protected boolean isControllerFailed(Controller ctr) {
		return ctr == null || !("".equals(ctr.getStatus()));
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(WarningSign s: s_model.getSelected()) {
				SmartDesktop desktop = connection.getDesktop();
				try {
					desktop.show(new WarningSignProperties(
						connection, s));
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
			for(WarningSign s: s_model.getSelected())
				return createSinglePopup(s);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new javax.swing.JLabel("" + n_selected +
			" Warning Signs"));
		p.addSeparator();
		p.add(new DeployAction(s_model, true));
		p.add(new DeployAction(s_model, false));
		return p;
	}

	/** Create a popup menu for a single selection */
	protected JPopupMenu createSinglePopup(WarningSign proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new DeployAction(s_model, true));
		p.add(new DeployAction(s_model, false));
		p.addSeparator();
		p.add(new PropertiesAction<WarningSign>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(WarningSign proxy) {
		return proxy.getGeoLoc();
	}
}
