/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.WorkRequestAction;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * The GateArmManager class provides proxies for GateArm objects.
 *
 * @author Douglas Lau
 */
public class GateArmManager extends DeviceManager<GateArm> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<GateArm> descriptor(final Session s) {
		return new ProxyDescriptor<GateArm>(
			s.getSonarState().getGateArms(), true
		) {
			@Override
			public GateArmProperties createPropertiesForm(
				GateArm ga)
			{
				return new GateArmProperties(s, ga);
			}
			@Override
			public GateArmForm makeTableForm() {
				return new GateArmForm(s);
			}
		};
	}

	/** Create a new gate arm manager */
	public GateArmManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 15);
	}

	/** Check if user can read gate arms */
	@Override
	public boolean canRead() {
		return super.canRead()
		    && session.canRead(GateArm.SONAR_TYPE);
	}

	/** Create a theme for gate arms */
	@Override
	protected ProxyTheme<GateArm> createTheme() {
		ProxyTheme<GateArm> theme = new ProxyTheme<GateArm>(this,
			new GateArmMarker());
		theme.addStyle(ItemStyle.CLOSED, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.MOVING, ProxyTheme.COLOR_MOVING);
		theme.addStyle(ItemStyle.OPEN, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.FAULT, ProxyTheme.COLOR_FAULT);
		theme.addStyle(ItemStyle.OFFLINE, ProxyTheme.COLOR_OFFLINE);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Fill single selection work request popup */
	@Override
	protected void fillPopupWorkReq(JPopupMenu p, GateArm ga) {
		p.add(new WorkRequestAction<GateArm>(ga, ga.getGeoLoc()));
		p.addSeparator();
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("gate_arm.title") + ": " +
			n_selected));
		p.addSeparator();
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(GateArm proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(GateArm proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getLocation(getGeoLoc(proxy));
	}
}
