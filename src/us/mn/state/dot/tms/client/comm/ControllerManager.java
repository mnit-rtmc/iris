/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
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
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
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

	/** Controller map object marker */
	static private final ControllerMarker MARKER = new ControllerMarker();

	/** Create a new controller manager */
	public ControllerManager(Session s, GeoLocManager lm) {
		super(s, lm);
	}

	/** Get the proxy type name */
	@Override
	public String getProxyType() {
		return "controller";
	}

	/** Get the controller cache */
	@Override
	public TypeCache<Controller> getCache() {
		return session.getSonarState().getConCache().getControllers();
	}

	/** Create a comm map tab */
	public CommTab createTab() {
		return new CommTab(session, this);
	}

	/** Check if user can read controllers */
	public boolean canRead() {
		return session.canRead(Controller.SONAR_TYPE) &&
		       session.canRead(CommLink.SONAR_TYPE);
	}

	/** Get the shape for a given proxy */
	@Override
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
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
		ControllerTheme theme = new ControllerTheme(this, MARKER);
		theme.addStyle(ItemStyle.ACTIVE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected ControllerForm createPropertiesForm(Controller ctrl) {
		return new ControllerForm(session, ctrl);
	}

	/** Create a popup menu for the selected proxy object(s) */
	@Override
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(Controller ctrl: s_model.getSelected())
				return createSinglePopup(ctrl);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " +
			I18N.get("controller.plural")));
		p.addSeparator();
		return p;
	}

	/** Create a popup menu for a single controller selection */
	private JPopupMenu createSinglePopup(final Controller ctrl) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(ctrl)));
		p.addSeparator();
		if(TeslaAction.isConfigured())
			p.add(new TeslaAction<Controller>(ctrl));
		p.add(new PropertiesAction<Controller>(ctrl) {
			protected void doActionPerformed(ActionEvent e) {
				showPropertiesForm(ctrl);
			}
		});
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

	/** Get the layer zoom visibility threshold */
	@Override
	protected int getZoomThreshold() {
		return 16;
	}
}
