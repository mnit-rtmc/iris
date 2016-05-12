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
package us.mn.state.dot.tms.client.comm;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A controller manager is a container for SONAR Controller objects.
 *
 * @author Douglas Lau
 */
public class ControllerManager extends ProxyManager<Controller> {

	/** Create a new controller manager */
	public ControllerManager(Session s, GeoLocManager lm) {
		super(s, lm, 16, ItemStyle.FAILED);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return Controller.SONAR_TYPE;
	}

	/** Get the controller cache */
	@Override
	public TypeCache<Controller> getCache() {
		return session.getSonarState().getConCache().getControllers();
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

	/** Get the tangent angle for the given location */
	@Override
	public Double getTangentAngle(MapGeoLoc loc) {
		// Don't rotate markers by direction-of-travel
		return MapGeoLoc.northTangent();
	}

	/** Create a theme for controllers */
	@Override
	protected ProxyTheme<Controller> createTheme() {
		ControllerTheme theme = new ControllerTheme(this,
			new ControllerMarker());
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.ACTIVE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected ControllerForm createPropertiesForm(Controller ctrl) {
		return new ControllerForm(session, ctrl);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, Controller ctrl) {
		if (TeslaAction.isConfigured()) {
			p.add(new TeslaAction<Controller>(ctrl));
			p.addSeparator();
		}
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
		Cabinet cab = proxy.getCabinet();
		if(cab != null)
			return cab.getGeoLoc();
		else
			return null;
	}

	/** Check if a given attribute affects a proxy style */
	@Override
	public boolean isStyleAttrib(String a) {
		return "failTime".equals(a) || "maint".equals(a);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, Controller proxy) {
		switch(is) {
		case ACTIVE:
			return ControllerHelper.isActive(proxy);
		case MAINTENANCE:
			return ControllerHelper.needsMaintenance(proxy);
		case FAILED:
			return ControllerHelper.isFailed(proxy);
		case ALL:
			return true;
		default:
			return false;
		}
	}
}
