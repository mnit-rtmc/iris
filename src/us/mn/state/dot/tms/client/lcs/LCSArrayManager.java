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
import java.awt.Shape;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyJList;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * The LCSArrayManager class provides proxies for LCSArray objects.
 *
 * @author Douglas Lau
 */
public class LCSArrayManager extends ProxyManager<LCSArray> {

	/** LCS array map object shape */
	static protected final Shape SHAPE = new LcsMarker();

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
		for(Integer i: ind) {
			if(i == null || i != LaneUseIndication.DARK.ordinal())
				return true;
		}
		return false;
	}

	/** Get the LCS array cache */
	static protected TypeCache<LCSArray> getCache(Session s) {
		LcsCache cache = s.getSonarState().getLcsCache();
		return cache.getLCSArrays();
	}

	/** Test if an LCS array is available */
	protected boolean isAvailable(LCSArray proxy) {
		return !isLocked(proxy) &&
		       !LCSArrayHelper.isFailed(proxy) &&
		       !isDeployed(proxy) &&
		       !needsMaintenance(proxy);
	}

	/** User session */
	protected final Session session;

	/** Action to blank the selected LCS array */
	protected BlankLcsAction blankAction;

	/** Set the blank LCS action */
	public void setBlankAction(BlankLcsAction a) {
		blankAction = a;
	}

	/** Create a new LCS array manager */
	public LCSArrayManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		session = s;
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "LCS";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(LCSArray proxy) {
		return SHAPE;
	}

	/** Create a styled theme for LCS arrays */
	protected StyledTheme createTheme() {
		ProxyTheme<LCSArray> theme = new ProxyTheme<LCSArray>(this,
			getProxyType(), SHAPE);
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
			return LCSArrayHelper.isFailed(proxy);
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
		SmartDesktop desktop = session.getDesktop();
		try {
			desktop.show(new LCSArrayProperties(session, la));
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
		if(isDeployed(la) && blankAction != null)
			p.add(blankAction);
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
			DMS dms = DMSHelper.lookup(name);
			if(dms != null)
				return dms.getGeoLoc();
		}
		return null;
	}
}
