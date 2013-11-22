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
package us.mn.state.dot.tms.client.warning;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A warning sign manager is a container for SONAR warning sign objects.
 *
 * @author Douglas Lau
 */
public class WarningSignManager extends ProxyManager<WarningSign> {

	/** Warning sign map object marker */
	static protected final WarningSignMarker MARKER =
		new WarningSignMarker();

	/** Create a new warning sign manager */
	public WarningSignManager(Session s, GeoLocManager lm) {
		super(s, lm);
		getCache().addProxyListener(this);
	}

	/** Get the proxy type name */
	@Override public String getProxyType() {
		return "warning.sign";
	}

	/** Get the warning sign cache */
	@Override public TypeCache<WarningSign> getCache() {
		return session.getSonarState().getWarningSigns();
	}

	/** Check if user can read warning signs */
	public boolean canRead() {
		return session.canRead(WarningSign.SONAR_TYPE);
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for warning signs */
	protected ProxyTheme<WarningSign> createTheme() {
		ProxyTheme<WarningSign> theme =new ProxyTheme<WarningSign>(this,
			MARKER);
		theme.addStyle(ItemStyle.DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(ItemStyle.AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, WarningSign proxy) {
		switch(is) {
		case DEPLOYED:
			return proxy.getDeployed();
		case AVAILABLE:
			return (!ControllerHelper.isFailed(
			       proxy.getController())) && !proxy.getDeployed();
		case FAILED:
			return ControllerHelper.isFailed(proxy.getController());
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected WarningSignProperties createPropertiesForm(WarningSign ws) {
		return new WarningSignProperties(session, ws);
	}

	/** Create a popup menu for the selected proxy object(s) */
	@Override
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(WarningSign s: s_model.getSelected())
				return createSinglePopup(s);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " +
			I18N.get("warning.signs")));
		p.addSeparator();
		p.add(new DeployAction(s_model));
		p.add(new UndeployAction(s_model));
		return p;
	}

	/** Create a popup menu for a single selection */
	private JPopupMenu createSinglePopup(final WarningSign ws) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(ws)));
		p.addSeparator();
		p.add(new DeployAction(s_model));
		p.add(new UndeployAction(s_model));
		p.addSeparator();
		p.add(new PropertiesAction<WarningSign>(ws) {
			protected void doActionPerformed(ActionEvent e) {
				showPropertiesForm(ws);
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(WarningSign proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 15;
	}
}
