/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.beacon;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A beacon manager is a container for SONAR beacon objects.
 *
 * @author Douglas Lau
 */
public class BeaconManager extends ProxyManager<Beacon> {

	/** Beacon tab */
	private final BeaconTab tab;

	/** Create a new beacon manager */
	public BeaconManager(Session s, GeoLocManager lm) {
		super(s, lm, true, 14);
		tab = new BeaconTab(s, this);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return Beacon.SONAR_TYPE;
	}

	/** Get the beacon cache */
	@Override
	public TypeCache<Beacon> getCache() {
		return session.getSonarState().getBeacons();
	}

	/** Create the map tab */
	@Override
	public BeaconTab createTab() {
		return tab;
	}

	/** Create a theme for beacons */
	@Override
	protected ProxyTheme<Beacon> createTheme() {
		ProxyTheme<Beacon> theme = new ProxyTheme<Beacon>(this,
			new BeaconMarker());
		theme.addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, Beacon proxy) {
		long styles = proxy.getStyles();
		for (ItemStyle s: ItemStyle.toStyles(styles)) {
			if (s == is)
				return true;
		}
		return false;
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected BeaconProperties createPropertiesForm(Beacon b) {
		return new BeaconProperties(session, b);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, Beacon b) {
		for (String m: b.getMessage().split("\n")) {
			JPanel pnl = makeMenuLabel(m);
			pnl.setBackground(Color.WHITE);
			p.add(pnl);
		}
		p.addSeparator();
		p.add(new DeployAction(s_model));
		p.add(new UndeployAction(s_model));
		p.addSeparator();
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " + I18N.get("beacons")));
		p.addSeparator();
		p.add(new DeployAction(s_model));
		p.add(new UndeployAction(s_model));
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(Beacon proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the description of a beacon */
	@Override
	public String getDescription(Beacon proxy) {
		String d = super.getDescription(proxy);
		GeoLoc l = getGeoLoc(proxy);
		String n = proxy.getNotes();
		if (l != null && l.getCrossStreet() == null &&
		    !"".equals(n.trim()))
			return d + " (" + n + ")";
		else
			return d;
	}
}
