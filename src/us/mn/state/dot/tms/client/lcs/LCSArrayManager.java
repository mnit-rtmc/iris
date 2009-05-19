/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.PropertiesAction;
import us.mn.state.dot.tms.client.sonar.ProxyJList;
import us.mn.state.dot.tms.client.sonar.ProxyManager;
import us.mn.state.dot.tms.client.sonar.ProxyTheme;
import us.mn.state.dot.tms.client.sonar.StyleListModel;
import us.mn.state.dot.tms.client.sonar.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * The LCSArrayManager class provides proxies for LCSArray objects.
 *
 * @author Douglas Lau
 */
public class LCSArrayManager extends ProxyManager<LCSArray> {

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "Deployed";

	/** Name of locked style */
	static public final String STYLE_LOCKED = "Locked";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Test if an LCS array is locked */
	static protected boolean isLocked(LCSArray proxy) {
		return proxy.getLcsLock() != null;
	}

	/** Test if an LCS array needs maintenance */
	static protected boolean needsMaintenance(LCSArray proxy) {
		LCSArrayLock lck = LCSArrayLock.fromOrdinal(proxy.getLcsLock());
		return lck == LCSArrayLock.MAINTENANCE;
	}

	/** Test if an LCS array is deployed */
	static protected boolean isDeployed(LCSArray proxy) {
		Integer[] ind = proxy.getIndicationsCurrent();
		for(int i: ind) {
			if(i != LaneUseIndication.DARK.ordinal())
				return true;
		}
		return false;
	}

	/** Test if an LCS array is available */
	protected boolean isAvailable(LCSArray proxy) {
		return !isLocked(proxy) &&
		       !isFailed(proxy) &&
		       !isDeployed(proxy) &&
		       !needsMaintenance(proxy);
	}

	/** Test if an LCS array is failed */
	protected boolean isFailed(final LCSArray proxy) {
		return null != namespace.findObject(LCS.SONAR_TYPE,
		       new Checker<LCS>()
		{
			public boolean check(LCS lcs) {
				if(lcs.getArray() == proxy)
					return isFailed(lcs);
				else
					return false;
			}
		});
	}

	/** Test if an LCS is failed */
	protected boolean isFailed(LCS lcs) {
		String name = lcs.getName();
		DMS dms = (DMS)namespace.lookupObject(DMS.SONAR_TYPE, name);
		if(dms != null) {
			Controller ctr = dms.getController();
			if(ctr != null && "".equals(ctr.getStatus()))
				return false;
		}
		return true;
	}

	/** TMS connection */
	protected final TmsConnection connection;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Action to clear the selected LCS array */
	protected ClearLcsAction clearAction;

	/** Set the clear LCS action */
	public void setClearAction(ClearLcsAction a) {
		clearAction = a;
	}

	/** Create a new LCS array manager */
	public LCSArrayManager(TmsConnection tc, TypeCache<LCSArray> c,
		GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		namespace = tc.getSonarState().getNamespace();
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "LCS";
	}

	/** Create a styled theme for LCS arrays */
	protected StyledTheme createTheme() {
		ProxyTheme<LCSArray> theme = new ProxyTheme<LCSArray>(this,
			getProxyType(), new LcsMarker());
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_AVAILABLE,
			Outline.createSolid(Color.BLACK, 10));
		theme.addStyle(STYLE_DEPLOYED, ProxyTheme.COLOR_DEPLOYED,
			Outline.createSolid(Color.BLACK, 10));
		theme.addStyle(STYLE_LOCKED, null,
			Outline.createSolid(Color.RED, 10));
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE,
			Outline.createSolid(Color.BLACK, 10));
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED,
			Outline.createSolid(Color.BLACK, 10));
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new LCSArrayCellRenderer();
	}

	/** Create a proxy JList for the given style */
	public ProxyJList<LCSArray> createList(String style) {
		ProxyJList<LCSArray> list = super.createList(style);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(0);
		return list;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, LCSArray proxy) {
		if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isDeployed(proxy);
		else if(STYLE_LOCKED.equals(s))
			return isLocked(proxy);
		else if(STYLE_MAINTENANCE.equals(s))
			return needsMaintenance(proxy);
		else if(STYLE_FAILED.equals(s))
			return isFailed(proxy);
		else
			return STYLE_ALL.equals(s);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(LCSArray la: s_model.getSelected())
				showPropertiesForm(la);
		}
	}

	/** Show the properteis form for the given proxy */
	protected void showPropertiesForm(LCSArray la) {
		SmartDesktop desktop = connection.getDesktop();
		try {
			desktop.show(new LCSArrayProperties(connection, la));
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
			for(LCSArray la: s_model.getSelected())
				return createSinglePopup(la);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " LCS arrays"));
		p.addSeparator();
		return p;
	}

	/** Create a popup menu for a single LCS array selection */
	protected JPopupMenu createSinglePopup(final LCSArray la) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(la)));
		p.addSeparator();
		if(isDeployed(la) && clearAction != null)
			p.add(clearAction);
		if(TeslaAction.isConfigured()) {
			p.addSeparator();
			p.add(new TeslaAction<LCSArray>(la));
		}
		p.addSeparator();
		p.add(new PropertiesAction<LCSArray>(la) {
			protected void do_perform() {
				showPropertiesForm(la);
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(final LCSArray proxy) {
		LCS lcs = LCSArrayHelper.lookupLCS(proxy, 1);
		if(lcs != null) {
			String name = lcs.getName();
			DMS dms = (DMS)namespace.lookupObject(DMS.SONAR_TYPE,
				name);
			if(dms != null)
				return dms.getGeoLoc();
		}
		return null;
	}
}
