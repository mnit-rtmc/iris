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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.PropertiesAction;
import us.mn.state.dot.tms.client.sonar.ProxyManager;
import us.mn.state.dot.tms.client.sonar.ProxyTheme;
import us.mn.state.dot.tms.client.sonar.StyleSummary;
import us.mn.state.dot.tms.client.sonar.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * A DMS manager is a container for SONAR DMS objects.
 *
 * @author Douglas Lau
 */
public class DMSManager extends ProxyManager<DMS> {

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "Deployed";

	/** Name of travel time style */
	static public final String STYLE_TRAVEL_TIME = "Travel Time";

	/** Name of automated warning system style */
	static public final String STYLE_AWS = "AWS";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Test if a DMS is active */
	static public boolean isActive(DMS proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && ctr.getActive();
	}

	/** TMS connection */
	protected final TmsConnection connection;

	/** Create a new DMS manager */
	public DMSManager(TmsConnection tc, TypeCache<DMS> c,
		GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18NMessages.get("dms.abbreviation");
	}

	/** Create a styled theme for DMSs */
	protected StyledTheme createTheme() {
		ProxyTheme<DMS> theme = new ProxyTheme<DMS>(this,
			getProxyType(), new DMSMarker());
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_TRAVEL_TIME, Color.ORANGE);
		theme.addStyle(STYLE_AWS, Color.RED);
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new DmsCellRenderer(this);
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<DMS> createStyleSummary() {
		StyleSummary<DMS> summary = super.createStyleSummary();
		summary.setStyle(STYLE_DEPLOYED);

		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(0);

		// FIXME:
/*		handler.addRefreshListener(new RefreshListener() {
			public void dataChanged() {
				repaint();
			}
		}); */

		return summary;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, DMS proxy) {
		if(STYLE_AVAILABLE.equals(s))
			return isActive(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isDeployed(proxy);
		else if(STYLE_TRAVEL_TIME.equals(s))
			return isTravelTime(proxy);
		else if(STYLE_AWS.equals(s))
			return isAws(proxy);
		else if(STYLE_MAINTENANCE.equals(s))
			return isMaintenance(proxy);
		else if(STYLE_FAILED.equals(s))
			return isFailed(proxy);
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else if(STYLE_INACTIVE.equals(s))
			return !isActive(proxy);
		else
			return STYLE_ALL.equals(s);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(DMS dms: s_model.getSelected())
				showPropertiesForm(dms);
		}
	}

	/** Show the properteis form for the given proxy */
	protected void showPropertiesForm(DMS dms) {
		SmartDesktop desktop = connection.getDesktop();
		try {
			desktop.show(new DMSProperties(connection, dms));
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
			for(DMS dms: s_model.getSelected())
				return createSinglePopup(dms);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new javax.swing.JLabel("" + n_selected + " DMSs"));
		p.addSeparator();
		// FIXME: add clear all action
		return p;
	}

	/** Create a popup menu for a single DMS selection */
	protected JPopupMenu createSinglePopup(DMS proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		if(TeslaAction.isConfigured())
			p.add(new TeslaAction<DMS>(proxy));
		p.add(new PropertiesAction<DMS>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(DMS proxy) {
		return proxy.getGeoLoc();
	}
}
