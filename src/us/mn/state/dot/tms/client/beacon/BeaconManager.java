/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A beacon manager is a container for SONAR beacon objects.
 *
 * @author Douglas Lau
 */
public class BeaconManager extends DeviceManager<Beacon> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Beacon> descriptor(final Session s) {
		return new ProxyDescriptor<Beacon>(
			s.getSonarState().getBeacons(), true
		) {
			@Override
			public BeaconProperties createPropertiesForm(Beacon b) {
				return new BeaconProperties(s, b);
			}
			@Override
			public BeaconForm makeTableForm() {
				return new BeaconForm(s);
			}
		};
	}

	/** Create a new beacon manager */
	public BeaconManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 14);
	}

	/** Create the map tab */
	@Override
	public BeaconTab createTab() {
		return new BeaconTab(session, this);
	}

	/** Create a theme for beacons */
	@Override
	protected ProxyTheme<Beacon> createTheme() {
		ProxyTheme<Beacon> theme = new ProxyTheme<Beacon>(this,
			new BeaconMarker());
		theme.addStyle(ItemStyle.AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.EXTERNAL, ProxyTheme.COLOR_EXTERNAL);
		theme.addStyle(ItemStyle.DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.ALL);
		return theme;
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
		BeaconState bs = BeaconState.fromOrdinal(b.getState());
		JPanel st = makeMenuLabel(bs.toString());
		st.setBackground(Color.LIGHT_GRAY);
		p.add(st);
		p.addSeparator();
		p.add(new DeployAction(getSelectionModel()));
		p.add(new UndeployAction(getSelectionModel()));
		p.addSeparator();
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " + I18N.get("beacons")));
		p.addSeparator();
		p.add(new DeployAction(getSelectionModel()));
		p.add(new UndeployAction(getSelectionModel()));
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(Beacon proxy) {
		return proxy.getGeoLoc();
	}
}
