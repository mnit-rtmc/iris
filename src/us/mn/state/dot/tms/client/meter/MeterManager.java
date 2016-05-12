/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapAction;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The MeterManager class provides proxies for RampMeter objects.
 *
 * @author Douglas Lau
 */
public class MeterManager extends ProxyManager<RampMeter> {

	/** Create a new meter manager */
	public MeterManager(Session s, GeoLocManager lm) {
		super(s, lm, 15);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return RampMeter.SONAR_TYPE;
	}

	/** Get the ramp meter cache */
	@Override
	public TypeCache<RampMeter> getCache() {
		return session.getSonarState().getRampMeters();
	}

	/** Create a ramp meter map tab */
	@Override
	public RampMeterTab createTab() {
		return new RampMeterTab(session, this);
	}

	/** Create a theme for ramp meters */
	@Override
	protected ProxyTheme<RampMeter> createTheme() {
		return new MeterTheme(this);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, RampMeter proxy) {
		long styles = proxy.getStyles();
		for (ItemStyle s: ItemStyle.toStyles(styles)) {
			if (s == is)
				return true;
		}
		return false;
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected RampMeterProperties createPropertiesForm(RampMeter meter) {
		return new RampMeterProperties(session, meter);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, RampMeter meter) {
		if (session.isUpdatePermitted(meter)) {
			if (meter.getRate() != null) {
				p.add(new ShrinkQueueAction(meter, true));
				p.add(new GrowQueueAction(meter, true));
				p.add(new TurnOffAction(meter, true));
			} else
				p.add(new TurnOnAction(meter, true));
			p.addSeparator();
		}
		if (TeslaAction.isConfigured()) {
			p.add(new TeslaAction<RampMeter>(meter));
			p.addSeparator();
		}
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("ramp_meter.title") + ": " +
			n_selected));
		p.addSeparator();
		// FIXME: add turn on/off all actions
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(RampMeter proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(RampMeter proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getOnRampDescription(getGeoLoc(proxy));
	}
}
