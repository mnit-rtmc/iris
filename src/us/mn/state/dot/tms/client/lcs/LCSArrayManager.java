/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import java.awt.geom.AffineTransform;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
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

	/** LCS array map object marker */
	static protected final LcsMarker MARKER = new LcsMarker();

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "User Deployed";

	/** Name of scheduled style */
	static public final String STYLE_SCHEDULED = "Scheduled";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Test if an LCS array is active */
	static protected boolean isActive(LCSArray proxy) {
		return LCSArrayHelper.isActive(proxy);
	}

	/** Test if an LCS array is failed */
	static protected boolean isFailed(LCSArray proxy) {
		return LCSArrayHelper.isFailed(proxy);
	}

	/** Test if an LCS array is locked */
	static protected boolean isLocked(LCSArray proxy) {
		return proxy.getLcsLock() != null;
	}

	/** Test if an LCS array needs maintenance */
	static protected boolean needsMaintenance(LCSArray proxy) {
		LCSArrayLock lck = LCSArrayLock.fromOrdinal(proxy.getLcsLock());
		return (lck == LCSArrayLock.MAINTENANCE) ||
		       LCSArrayHelper.needsMaintenance(proxy);
	}

	/** Test if an LCS array is deployed */
	static protected boolean isDeployed(LCSArray proxy) {
		if(LCSArrayHelper.isAllFailed(proxy))
			return false;
		return LCSArrayHelper.isDeployed(proxy);
	}

	/** Test if an LCS array is active, not failed and deployed */
	static protected boolean isMessageDeployed(LCSArray proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       isDeployed(proxy);
	}

	/** Test if an LCS has been deployed by a user */
	static protected boolean isUserDeployed(LCSArray proxy) {
		return isMessageDeployed(proxy) &&
		       !LCSArrayHelper.isScheduleDeployed(proxy);
	}

	/** Test if an LCS has been deployed by schedule */
	static protected boolean isScheduleDeployed(LCSArray proxy) {
		return LCSArrayHelper.isScheduleDeployed(proxy);
	}

	/** Get the LCS array cache */
	static protected TypeCache<LCSArray> getCache(Session s) {
		LcsCache cache = s.getSonarState().getLcsCache();
		return cache.getLCSArrays();
	}

	/** Get the LCS cache */
	static protected TypeCache<LCS> getLCSCache(Session s) {
		LcsCache cache = s.getSonarState().getLcsCache();
		return cache.getLCSs();
	}

	/** Simple class to wait until all LCS have been enumerated */
	static protected class LCSWaiter implements ProxyListener<LCS> {
		protected boolean enumerated = false;
		public void proxyAdded(LCS proxy) {}
		public synchronized void enumerationComplete() {
			enumerated = true;
			notify();
		}
		public void proxyRemoved(LCS proxy) {}
		public void proxyChanged(LCS proxy, String a) {}
		protected synchronized void waitForEnumeration() {
			while(!enumerated) {
				try {
					wait();
				}
				catch(InterruptedException e) {
					// uhhh ?
				}
			}
		}
	}

	/** Test if an LCS array is available */
	protected boolean isAvailable(LCSArray proxy) {
		return !isLocked(proxy) &&
		       isActive(proxy) &&
		       !isFailed(proxy) &&
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
		cache.addProxyListener(this);
		waitForLCSEnumeration(s);
	}

	/** Wait for all LCS to be enumerated */
	protected void waitForLCSEnumeration(Session s) {
		TypeCache<LCS> lc = getLCSCache(s);
		LCSWaiter waiter = new LCSWaiter();
		lc.addProxyListener(waiter);
		waiter.waitForEnumeration();
		lc.removeProxyListener(waiter);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "LCS";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a styled theme for LCS arrays */
	protected StyledTheme createTheme() {
		ProxyTheme<LCSArray> theme = new ProxyTheme<LCSArray>(this,
			getProxyType(), MARKER);
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(STYLE_DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_SCHEDULED, ProxyTheme.COLOR_SCHEDULED);
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new LCSArrayCellRenderer();
	}

	/** Comparator for ordering LCS arrays */
	protected final Comparator<LCSArray> comparator =
		new Comparator<LCSArray>()
	{
		// FIXME: if an LCS array is moved, that will break the sort
		//        and lead to unpredictable results.
		public int compare(LCSArray l0, LCSArray l1) {
			GeoLoc g0 = LCSArrayHelper.lookupGeoLoc(l0);
			GeoLoc g1 = LCSArrayHelper.lookupGeoLoc(l1);
			if(g0 != null && g1 != null) {
				Integer c = compare(g0, g1);
				if(c != null)
					return c;
			}
			return l0.getName().compareTo(l1.getName());
		}
		protected Integer compare(GeoLoc g0, GeoLoc g1) {
			String c0 = GeoLocHelper.getCorridorID(g0);
			String c1 = GeoLocHelper.getCorridorID(g1);
			int c = c0.compareTo(c1);
			if(c != 0)
				return c;
			CorridorBase cb =
				session.getR_NodeManager().lookupCorridor(g0);
			if(cb != null) {
				Float f0 = cb.calculateMilePoint(g0);
				Float f1 = cb.calculateMilePoint(g1);
				if(f0 != null && f1 != null) {
					if(f0 < f1)
						return 1;
					else if(f0 > f1)
						return -1;
					else
						return 0;
				}
			}
			return null;
		}
	};

	/** Create a style list model for the given symbol */
	protected StyleListModel<LCSArray> createStyleListModel(Symbol s) {
		return new StyleListModel<LCSArray>(this, s.getLabel(),
			s.getLegend())
		{
			protected TreeSet<LCSArray> createProxySet() {
				return new TreeSet<LCSArray>(comparator);
			}
		};
	}

	/** Create a proxy JList for the given style */
	public ProxyJList<LCSArray> createList(String style) {
		ProxyJList<LCSArray> list = super.createList(style);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(0);
		return list;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, LCSArray proxy) {
		if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isUserDeployed(proxy);
		else if(STYLE_SCHEDULED.equals(s))
			return isScheduleDeployed(proxy);
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
		SmartDesktop desktop = session.getDesktop();
		desktop.show(new LCSArrayProperties(session, la));
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
			for(int i = 1; i <= LCSArray.MAX_LANES; i++) {
				DMS dms = LCSArrayHelper.lookupDMS(la, i);
				if(dms != null)
					p.add(new TeslaAction<DMS>(dms));
			}
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

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 14;
	}
}
