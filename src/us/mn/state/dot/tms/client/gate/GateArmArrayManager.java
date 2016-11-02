/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The GateArmArrayManager class provides proxies for GateArmArray objects.
 *
 * @author Douglas Lau
 */
public class GateArmArrayManager extends DeviceManager<GateArmArray> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<GateArmArray> descriptor(
		final Session s)
	{
		return new ProxyDescriptor<GateArmArray>(
			s.getSonarState().getGateArmArrays(), true
		) {
			@Override
			public GateArmArrayProperties createPropertiesForm(
				GateArmArray ga)
			{
				return new GateArmArrayProperties(s, ga);
			}
		};
	}

	/** Create a new gate arm array manager */
	public GateArmArrayManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 15);
	}

	/** Create a gate arm map tab */
	@Override
	public GateArmTab createTab() {
		return new GateArmTab(session, this);
	}

	/** Check if user can read gate arms + arrays */
	@Override
	public boolean canRead() {
		return super.canRead()
		    && session.canRead(GateArm.SONAR_TYPE);
	}

	/** Create a theme for gate arms */
	@Override
	protected ProxyTheme<GateArmArray> createTheme() {
		ProxyTheme<GateArmArray> theme = new ProxyTheme<GateArmArray>(
			this, new GateArmMarker());
		theme.addStyle(ItemStyle.CLOSED, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.MOVING, ProxyTheme.COLOR_SCHEDULED);
		theme.addStyle(ItemStyle.OPEN, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("gate_arm_array.title") + ": " +
			n_selected));
		p.addSeparator();
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(GateArmArray proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(GateArmArray proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getDescription(getGeoLoc(proxy));
	}
}
