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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.PropertiesAction;
import us.mn.state.dot.tms.client.sonar.ProxyJList;
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

	/** Color definition for AWS controlled style */
	static protected final Color COLOR_HELIOTROPE = new Color(1, 0.5f,0.9f);

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of travel time style */
	static public final String STYLE_TRAVEL_TIME = "Travel Time";

	/** Name of automated warning system deployed style */
	static public final String STYLE_AWS_DEPLOYED =
		I18NMessages.get("dms.aws.deployed");

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "User Deployed";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of automated warning system controlled style */
	static public final String STYLE_AWS_CONTROLLED =
		I18NMessages.get("dms.aws.controlled");

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Test if a DMS is available */
	static public boolean isAvailable(DMS proxy) {
		return isActive(proxy) &&
		       !isDeployed(proxy) &&
		       !isFailed(proxy);
	}

	/** Test if a DMS is active */
	static public boolean isActive(DMS proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && ctr.getActive();
	}

	/** Test if a DMS has a travel time message deployed */
	static public boolean isTravelTime(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		return m.getPriority() ==
		       DMSMessagePriority.TRAVEL_TIME.ordinal();
	}

	/** Test if a DMS is deployed */
	static public boolean isDeployed(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		MultiString ms = new MultiString(m.getMulti());
		return !ms.isBlank();
	}

	/** Test if a DMS can be controlled by AWS */
	static public boolean isAwsControlled(DMS proxy) {
		return proxy.getAwsAllowed() && proxy.getAwsControlled();
	}

	/** Test if a DMS has an AWS message deployed */
	static public boolean isAwsDeployed(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		return m.getPriority() == DMSMessagePriority.AWS.ordinal();
	}

	/** Test if a DMS has been deployed by a user */
	static public boolean isUserDeployed(DMS proxy) {
		return isDeployed(proxy) &&
		       !isTravelTime(proxy) &&
		       !isAwsDeployed(proxy);
	}

	/** Test if a DMS needs maintenance */
	static public boolean needsMaintenance(DMS proxy) {
		Controller ctr = proxy.getController();
		if(ctr != null && ctr.getStatus().equals("")) {
			String e = ctr.getError();
			return !e.equals("");
		} else
			return false;
	}

	/** Test if a DMS if failed */
	static public boolean isFailed(DMS proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && (!"".equals(ctr.getStatus()));
	}

	/** TMS connection */
	protected final TmsConnection connection;

	/** Create a new DMS manager */
	public DMSManager(TmsConnection tc, TypeCache<DMS> c, GeoLocManager lm)
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
			getProxyType(), new DmsMarker());
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_TRAVEL_TIME, Color.ORANGE);
		if(SystemAttributeHelper.isAwsEnabled())
			theme.addStyle(STYLE_AWS_DEPLOYED, Color.RED);
		theme.addStyle(STYLE_DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		if(SystemAttributeHelper.isAwsEnabled())
			theme.addStyle(STYLE_AWS_CONTROLLED, COLOR_HELIOTROPE);
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new DmsCellRenderer();
	}

	/** Create a proxy JList for the given style */
	public ProxyJList<DMS> createList(String style) {
		ProxyJList<DMS> list = super.createList(style);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(0);
		return list;
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<DMS> createStyleSummary() {
		StyleSummary<DMS> summary = super.createStyleSummary();
		summary.setStyle(STYLE_DEPLOYED);
		return summary;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, DMS proxy) {
		if(STYLE_INACTIVE.equals(s))
			return !isActive(proxy);
		else if(STYLE_FAILED.equals(s))
			return isFailed(proxy);
		else if(STYLE_TRAVEL_TIME.equals(s))
			return isTravelTime(proxy);
		else if(STYLE_AWS_DEPLOYED.equals(s))
			return isAwsDeployed(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isUserDeployed(proxy);
		else if(STYLE_MAINTENANCE.equals(s))
			return needsMaintenance(proxy);
		else if(STYLE_AWS_CONTROLLED.equals(s))
			return isAwsControlled(proxy);
		else if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
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
