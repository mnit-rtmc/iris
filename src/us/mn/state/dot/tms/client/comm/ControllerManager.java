/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.geo.MapVector;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A controller manager is a container for SONAR Controller objects.
 *
 * @author Douglas Lau
 */
public class ControllerManager extends ProxyManager<Controller> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<Controller> descriptor(final Session s) {
		return new ProxyDescriptor<Controller>(
			s.getSonarState().getConCache().getControllers(), true
		) {
			@Override
			public ControllerForm createPropertiesForm(
				Controller ctrl)
			{
				return new ControllerForm(s, ctrl);
			}
		};
	}

	/** Create a new controller manager */
	public ControllerManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 16, ItemStyle.OFFLINE);
	}


	/** Create a comm map tab */
	@Override
	public CommTab createTab() {
		return new CommTab(session, this);
	}

	/** Check if user can read controllers */
	@Override
	public boolean canRead() {
		return super.canRead()
		    && session.canRead(CommLink.SONAR_TYPE);
	}

	/** Get the normal vector for the given location */
	@Override
	public MapVector getNormalVector(MapGeoLoc loc) {
		// Don't rotate markers by direction-of-travel
		return GeoLocHelper.normalVector(Direction.NORTH.ordinal());
	}

	/** Create a theme for controllers */
	@Override
	protected ProxyTheme<Controller> createTheme() {
		ControllerTheme theme = new ControllerTheme(this,
			new ControllerMarker());
		theme.addStyle(ItemStyle.OFFLINE, ProxyTheme.COLOR_OFFLINE);
		theme.addStyle(ItemStyle.FAULT, ProxyTheme.COLOR_FAULT);
		theme.addStyle(ItemStyle.ACTIVE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("controller.title") + ": " +
			n_selected));
		p.addSeparator();
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(Controller proxy) {
		return (proxy != null) ? proxy.getGeoLoc() : null;
	}

	/** Check if a given attribute affects a proxy style */
	@Override
	public boolean isStyleAttrib(String a) {
		return "failTime".equals(a)
		    || "status".equals(a);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, Controller proxy) {
		switch (is) {
		case ACTIVE:
			return ControllerHelper.isActive(proxy);
		case FAULT:
			return ControllerHelper.hasFaults(proxy);
		case OFFLINE:
			return ControllerHelper.isOffline(proxy);
		case ALL:
			return true;
		default:
			return false;
		}
	}
}
