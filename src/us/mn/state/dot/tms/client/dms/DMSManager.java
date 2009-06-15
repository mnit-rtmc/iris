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
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyJList;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.sonar.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A DMS manager is a container for SONAR DMS objects.
 *
 * @author Douglas Lau
 */
public class DMSManager extends ProxyManager<DMS> {

	/** Color definition for AWS controlled style */
	static protected final Color COLOR_HELIOTROPE = new Color(1, 0.5f,0.9f);

	/** TMS connection */
	protected final TmsConnection connection;

	/** Action to clear the selected DMS */
	protected ClearDmsAction clearAction;

	/** Set the clear DMS action */
	public void setClearAction(ClearDmsAction a) {
		clearAction = a;
	}

	/** Create a new DMS manager */
	public DMSManager(TmsConnection tc, TypeCache<DMS> c, GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		initialize();
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<DMS> createStyleListModel(Symbol s) {
		return new DMSStyleModel(this, s.getLabel(), s.getLegend(),
			connection.getSonarState().getControllers());
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18N.get("dms.abbreviation");
	}

	/** Create a styled theme for DMSs */
	protected StyledTheme createTheme() {
		// NOTE: the ordering of themes controls which color is used
		//       to render the sign icon on the map
		ProxyTheme<DMS> theme = new ProxyTheme<DMS>(this,
			getProxyType(), new DmsMarker());
		theme.addStyle(DMSHelper.STYLE_AVAILABLE,
			ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(DMSHelper.STYLE_DEPLOYED,
			ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(DMSHelper.STYLE_TRAVEL_TIME, Color.ORANGE);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean())
			theme.addStyle(DMSHelper.STYLE_AWS_DEPLOYED, Color.RED);
		theme.addStyle(DMSHelper.STYLE_MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(DMSHelper.STYLE_INACTIVE,
			ProxyTheme.COLOR_INACTIVE, ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(DMSHelper.STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			theme.addStyle(DMSHelper.STYLE_AWS_CONTROLLED,
				COLOR_HELIOTROPE);
		}
		theme.addStyle(DMSHelper.STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(DMSHelper.STYLE_ALL);
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
		summary.setStyle(DMSHelper.STYLE_DEPLOYED);
		return summary;
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
		if(clearAction != null)
			p.add(clearAction);
		return p;
	}

	/** Create a popup menu for a single DMS selection */
	protected JPopupMenu createSinglePopup(DMS proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		if(clearAction != null)
			p.add(clearAction);
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

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, DMS proxy) {
		return DMSHelper.checkStyle(s, proxy);
	}
}
